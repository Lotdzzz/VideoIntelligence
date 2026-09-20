package com.vi.service.impl;

import com.framework.exception.upload.UploadEmptyException;
import com.framework.exception.upload.UploadMultipartCompeteFailedException;
import com.framework.exception.upload.UploadNameException;
import com.framework.service.RedisCacheForHashService;
import com.framework.service.RedisCacheService;
import com.vi.constants.FileConstants;
import com.vi.entity.dto.PartUploadCompleteDTO;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.model.SlicePartInfo;
import com.vi.entity.model.VideoUploadProgress;
import com.vi.entity.vo.VideoSliceMissionVo;
import com.vi.properties.UploadWhiteList;
import com.vi.service.IVideoUploadService;
import com.vi.service.ViFileService;
import com.vi.utils.FileWhiteFilterUtil;
import com.vi.utils.VideoChunkAllocatorUtil;
import com.vi.utils.VideoUploadProgressConverterUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    private final ViFileService fileService;

    /**
     * 接收视频信息，返回minIO的预签名集合 用于分片上传
     *
     * @param videoDetailsInfoDTO 视频信息
     * @return 返回minIO的预签名集合 用于分片上传
     */
    @Override
    public VideoSliceMissionVo receiveVideoInfo(ViFileDTO videoDetailsInfoDTO) {
        // 进行合法性检查
        String original = preCheckVideoInfo(videoDetailsInfoDTO);

        // 分析视频信息 生成分片数
        VideoChunkAllocatorUtil.ChunkPlan allocate =
                VideoChunkAllocatorUtil.allocate(videoDetailsInfoDTO.getFileSize());
        long chunkCount = allocate.getChunkCount();
        videoDetailsInfoDTO.setChunkCount(chunkCount);
        videoDetailsInfoDTO.setChunkSize(allocate.getChunkSize());

        // 生成防重名的文件名 最终写入minIO的文件名件名
        String filename = UUID.randomUUID() + "_" + original;
        videoDetailsInfoDTO.setObjectName(filename);
        videoDetailsInfoDTO.setOriginalName(original);

        // 告知minIO创建一个分片上传任务并获取任务凭证uploadId
        String uploadId = minioService.initiateMultipartUpload(filename);
        videoDetailsInfoDTO.setUploadId(uploadId);

        // 将文件信息存入redis中
        saveVideoInfoToRedis(VideoUploadProgressConverterUtil.toProgressObject(videoDetailsInfoDTO));

        // 根据分片数生成预签名url
        return minioService.generatePresignedUrls(videoDetailsInfoDTO);
    }

    /**
     * 将视频信息存入redis中
     *
     * @param info 视频信息
     */
    private void saveVideoInfoToRedis(VideoUploadProgress info) {
        // 构造map
        Map<String, Object> videoInfoMap = VideoUploadProgressConverterUtil.toMap(info);

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
    public String preCheckVideoInfo(ViFileDTO videoDetailsInfoDTO) {
        //检测文件非空性
        if (videoDetailsInfoDTO == null
                || videoDetailsInfoDTO.getOriginalName() == null
                || videoDetailsInfoDTO.getFileSize() == null) {
            throw new UploadEmptyException(null);
        }

        //路径穿越攻击防御 文件名非空 如果为空用空串防空指针
        String original = StringUtils.cleanPath(videoDetailsInfoDTO.getOriginalName());
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
     * @return true 如果上传成功，false 如果上传失败
     */
    @Override
    public boolean sliceUpload(PartUploadCompleteDTO partUploadCompleteDTO) {
        // 从redis获取对应的part信息 如果没有则代表还没上传 保持幂等性
        if (getRepeatPartFromRedis(partUploadCompleteDTO)) {
            return true;
        }

        // 用redis记录分片上传完成的状态
        recordPartUploadComplete(partUploadCompleteDTO);

        // 获取总记录数
        VideoUploadProgress videoInfo = getTotalPartsRecord(partUploadCompleteDTO);

        if (videoInfo == null) {
            return false;
        }

        // 更新已完成分片数 幂等性更新
        Long slices = updateUploadVideoStatus(videoInfo);

        // 判断是否所有分片都上传完成
        if (!slices.equals(Long.parseLong(videoInfo.getTotalParts()))) {
            return true;
        }

        // 进行合并操作
        try {
            minioService.complete(videoInfo.getFileName(), videoInfo.getUploadId(), getParts(videoInfo.getFileName()));
        } catch (Exception e) {
            throw new UploadMultipartCompeteFailedException(e.getMessage());
        }

        // 保存文件持久化到数据库
        return fileService.addFile(VideoUploadProgressConverterUtil.toVIFileDTO(videoInfo, minioService.getBucketName()));
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
                .sorted(Comparator.comparingInt(CompletedPart::partNumber))
                .toList();
    }

    /**
     * 更新已完成分片数 幂等性更新
     *
     * @param videoInfo 视频信息
     * @return 更新后的已完成分片数
     */
    private Long updateUploadVideoStatus(VideoUploadProgress videoInfo) {
        return redisCacheForHashService.incrementCacheMapValue(
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
            return VideoUploadProgressConverterUtil.fromMap(videoInfoMap);
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
