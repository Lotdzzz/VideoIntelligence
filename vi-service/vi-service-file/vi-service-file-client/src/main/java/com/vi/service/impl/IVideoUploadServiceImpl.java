package com.vi.service.impl;

import com.framework.exception.upload.UploadEmptyException;
import com.framework.exception.upload.UploadNameException;
import com.framework.service.RedisCacheForHashService;
import com.framework.service.RedisCacheService;
import com.vi.constants.FileConstants;
import com.vi.entity.dto.PartUploadCompleteDTO;
import com.vi.entity.dto.VideoDetailsInfoDTO;
import com.vi.entity.model.SlicePartInfo;
import com.vi.entity.model.VideoUploadProgress;
import com.vi.entity.vo.VideoReturnInfoVO;
import com.vi.entity.vo.VideoSliceMissionVo;
import com.vi.properties.UploadWhiteList;
import com.vi.service.IVideoUploadService;
import com.vi.status.UploadStatus;
import com.vi.utils.FileWhiteFilterUtil;
import com.vi.utils.VideoChunkAllocatorUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 视频上传服务实现类
 *
 * @author dotm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IVideoUploadServiceImpl implements IVideoUploadService {

    private final RedisCacheService redisCacheService;

    private final MinioService minioService;

    private final UploadWhiteList uploadWhiteList;

    private final RedisCacheForHashService redisCacheForHashService;

    /**
     * 接收视频信息，返回minIO的预签名集合 用于分片上传
     *
     * @param videoDetailsInfoDTO 视频信息
     * @return 返回minIO的预签名集合 用于分片上传
     */
    @Override
    public VideoSliceMissionVo receiveVideoInfo(VideoDetailsInfoDTO videoDetailsInfoDTO) {
        // 进行合法性检查
        String original = preCheckVideoInfo(videoDetailsInfoDTO);

        // 分析视频信息 生成分片数
        VideoChunkAllocatorUtil.ChunkPlan allocate =
                VideoChunkAllocatorUtil.allocate(videoDetailsInfoDTO.getFileSize());
        long chunkCount = allocate.getChunkCount();

        // 生成防重名的文件名 最终写入minIO的文件名件名
        String filename = UUID.randomUUID() + "_" + original;

        // 告知minIO创建一个分片上传任务并获取任务凭证uploadId
        String uploadId = minioService.initiateMultipartUpload(filename);

        // 将文件信息存入redis中
        saveVideoInfoToRedis(VideoUploadProgress.builder()
                .fileName(filename)
                .totalParts(String.valueOf(chunkCount))
                .completedParts("0")
                .fileSize(videoDetailsInfoDTO.getFileSize().toString())
                .partSize(String.valueOf(allocate.getChunkSize()))
                .status(UploadStatus.PENDING.name())
                .uploadId(uploadId)
                .build());

        // 根据分片数生成预签名url
        return generatePresignedUrls(filename, (int) chunkCount, uploadId);
    }

    /**
     * 根据分片数生成预签名url
     *
     * @param filename   文件名
     * @param totalParts 分片数
     * @param uploadId   上传任务的唯一标识符
     * @return 预签名url列表
     */
    private VideoSliceMissionVo generatePresignedUrls(String filename, int totalParts, String uploadId) {
        // 创建结果集
        List<VideoReturnInfoVO> result = new ArrayList<>();

        // 生成每个分片的预签名url
        for (int i = 0; i < totalParts; i++) {
            VideoReturnInfoVO videoReturnInfoVO = new VideoReturnInfoVO();
            videoReturnInfoVO.setPartNumber((long) i);
            videoReturnInfoVO.setPreSignedUrl(minioService.s3ProtocolGeneratePartUrl(filename, uploadId, i));
            videoReturnInfoVO.setFilename(filename);
            result.add(videoReturnInfoVO);
        }

        VideoSliceMissionVo videoSliceMissionVo = new VideoSliceMissionVo();
        videoSliceMissionVo.setVideoReturnInfoVOList(result);
        videoSliceMissionVo.setUploadId(uploadId);
        return videoSliceMissionVo;
    }

    /**
     * 将视频信息存入redis中
     *
     * @param info 视频信息
     */
    private void saveVideoInfoToRedis(VideoUploadProgress info) {
        // 构造map
        Map<String, Object> videoInfoMap = Map.of(
                VideoUploadProgress.FILE_NAME_KEY, info.getFileName(),
                VideoUploadProgress.TOTAL_PARTS_KEY, info.getTotalParts(),
                VideoUploadProgress.COMPLETED_PARTS_KEY, info.getCompletedParts(),
                VideoUploadProgress.FILE_SIZE_KEY, info.getFileSize(),
                VideoUploadProgress.PART_SIZE_KEY, info.getPartSize(),
                VideoUploadProgress.STATUS_KEY, info.getStatus(),
                VideoUploadProgress.UPLOAD_ID_KEY, info.getUploadId()
        );

        redisCacheForHashService.setCacheMap(
                FileConstants.VIDEO_SLICE_REDIS_PREFIX + info.getFileName(),
                videoInfoMap,
                FileConstants.VIDEO_SLICE_REDIS_EXPIRE,  // 设置过期时间为24小时
                TimeUnit.HOURS
        );
    }

    /**
     * 预检查视频信息
     *
     * @param videoDetailsInfoDTO 视频信息
     */
    @Override
    public String preCheckVideoInfo(VideoDetailsInfoDTO videoDetailsInfoDTO) {
        //检测文件非空性
        if (videoDetailsInfoDTO == null
                || videoDetailsInfoDTO.getFileName() == null
                || videoDetailsInfoDTO.getFileSize() == null) {
            throw new UploadEmptyException(null);
        }

        //路径穿越攻击防御 文件名非空 如果为空用空串防空指针
        String original = StringUtils.cleanPath(videoDetailsInfoDTO.getFileName());
        if (original.isBlank() || original.contains("..")) {
            throw new UploadNameException(null);
        }

        // 白名单过滤器
        if (!FileWhiteFilterUtil.filter(uploadWhiteList.getList(), original)) {
            throw new UploadNameException(null);
        }
        return original;
    }

    /**
     * 上传视频分片
     *
     * @param partUploadCompleteDTO 分片上传完成信息
     */
    @Override
    public void sliceUpload(PartUploadCompleteDTO partUploadCompleteDTO) {
        // 从redis获取对应的part信息 如果没有则代表还没上传 保持幂等性
        if (getRepeatPartFromRedis(partUploadCompleteDTO)) {
            return;
        }

        // 用redis记录分片上传完成的状态
        recordPartUploadComplete(partUploadCompleteDTO);

        // 获取总记录数
        VideoUploadProgress videoInfo = getTotalPartsRecord(partUploadCompleteDTO);

        if (videoInfo == null) {
            return;
        }

        // 更新已完成分片数 幂等性更新
        updateUploadVideoStatus(videoInfo);

        // 判断是否所有分片都上传完成
        if (videoInfo.getCompletedParts().equals(videoInfo.getTotalParts())) {
            // 进行合并操作
            log.info("所有分片上传完成，开始合并文件: {}", videoInfo.getFileName());
            minioService.complete(videoInfo.getFileName(), videoInfo.getUploadId(), getParts(videoInfo.getFileName()));
            return;
        }

        // 保存文件持久化到数据库
    }

    /**
     * 获取所有已上传的分片信息
     *
     * @param fileName 文件名
     * @return 已上传的分片信息列表
     */
    private List<CompletedPart> getParts(String fileName) {
        Collection<String> keys = redisCacheService.keys(FileConstants.VIDEO_SLICE_PREFIX + fileName + ":*");
        List<SlicePartInfo> cacheObject = redisCacheService.multiGetCacheObject(keys);
        return cacheObject.stream()
                .map(part ->
                        CompletedPart.builder()
                                .partNumber(part.getPartNumber())
                                .eTag(part.getETag())
                                .build()
                )
                .toList();
    }

    /**
     * 更新已完成分片数 幂等性更新
     *
     * @param videoInfo 视频信息
     */
    private void updateUploadVideoStatus(VideoUploadProgress videoInfo) {
        redisCacheForHashService.incrementCacheMapValue(
                FileConstants.VIDEO_SLICE_REDIS_PREFIX + videoInfo.getFileName(),
                VideoUploadProgress.COMPLETED_PARTS_KEY,
                1
        );
    }

    /**
     * 获取总记录数
     */
    private VideoUploadProgress getTotalPartsRecord(PartUploadCompleteDTO partUploadCompleteDTO) {
        // hash key
        String hashKey = FileConstants.VIDEO_SLICE_REDIS_PREFIX + partUploadCompleteDTO.getFilename();

        // 从redis获取视频信息
        Map<String, Object> videoInfoMap = redisCacheForHashService.getCacheMap(hashKey);

        if (videoInfoMap != null) {
            String fileName = (String) videoInfoMap.get(VideoUploadProgress.FILE_NAME_KEY);
            String totalParts = (String) videoInfoMap.get(VideoUploadProgress.TOTAL_PARTS_KEY);
            String completedParts = (String) videoInfoMap.get(VideoUploadProgress.COMPLETED_PARTS_KEY);
            String fileSize = (String) videoInfoMap.get(VideoUploadProgress.FILE_SIZE_KEY);
            String partSize = (String) videoInfoMap.get(VideoUploadProgress.PART_SIZE_KEY);
            String status = (String) videoInfoMap.get(VideoUploadProgress.STATUS_KEY);
            String uploadId = (String) videoInfoMap.get(VideoUploadProgress.UPLOAD_ID_KEY);

            return VideoUploadProgress.builder()
                    .fileName(fileName)
                    .totalParts(String.valueOf(totalParts))
                    .completedParts(String.valueOf(completedParts))
                    .fileSize(String.valueOf(fileSize))
                    .partSize(String.valueOf(partSize))
                    .status(String.valueOf(status))
                    .uploadId(uploadId)
                    .build();
        }
        return null;
    }

    /**
     * 用redis记录分片上传完成的状态
     *
     * @param partUploadCompleteDTO 分片上传完成信息
     */
    private void recordPartUploadComplete(PartUploadCompleteDTO partUploadCompleteDTO) {
        redisCacheService.setCacheObject(
                FileConstants.VIDEO_SLICE_PREFIX + partUploadCompleteDTO.getFilename() + ":" + partUploadCompleteDTO.getPartNumber(),
                SlicePartInfo.builder()
                        .partNumber(partUploadCompleteDTO.getPartNumber())
                        .eTag(partUploadCompleteDTO.getETag())
                        .build(),
                FileConstants.VIDEO_SLICE_REDIS_EXPIRE_SECONDS,  // 设置过期时间为24小时
                TimeUnit.HOURS
        );
    }

    /**
     * 从redis获取对应的part信息 如果没有则代表还没上传 保持幂等性
     *
     * @param partUploadCompleteDTO 分片上传完成信息
     * @return true 如果已经上传过该分片，false 如果没有上传过
     */
    private boolean getRepeatPartFromRedis(@NotNull PartUploadCompleteDTO partUploadCompleteDTO) {
        // 从redis获取已完成的分片数量
        SlicePartInfo completedParts = redisCacheService.getCacheObject(
                FileConstants.VIDEO_SLICE_PREFIX + partUploadCompleteDTO.getFilename() + ":" + partUploadCompleteDTO.getPartNumber());
        return completedParts != null;
    }
}
