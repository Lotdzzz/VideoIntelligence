package com.vi.service.impl;

import com.framework.exception.upload.UploadMultipartCompeteFailedException;
import com.vi.entity.dto.PartUploadCompleteDTO;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.model.VideoUploadProgress;
import com.vi.entity.vo.VideoSliceMissionVo;
import com.vi.producer.FileProducer;
import com.vi.properties.MinioProperties;
import com.vi.service.*;
import com.vi.utils.VideoChunkAllocatorUtil;
import com.vi.utils.VideoUploadProgressConverterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 视频上传服务实现类
 *
 * @author dotm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IVideoUploadServiceImpl implements IVideoUploadService {

    private final MinioService minioService;

    private final FilePreFilterService filePreFilterService;

    private final ViFileService fileService;

    private final RedisService redisService;

    private final MinioProperties minioProperties;

    private final FileProducer fileProducer;

    /**
     * 接收视频信息，返回minIO的预签名集合 用于分片上传
     *
     * @param videoDetailsInfoDTO 视频信息
     * @return 返回minIO的预签名集合 用于分片上传
     */
    @Override
    public VideoSliceMissionVo receiveVideoInfo(ViFileDTO videoDetailsInfoDTO) {
        // 进行合法性检查
        String original = filePreFilterService.preCheckVideoInfo(videoDetailsInfoDTO);

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
        redisService.saveVideoInfoToRedis(VideoUploadProgressConverterUtil.toProgressObject(videoDetailsInfoDTO));

        // 根据分片数生成预签名url
        return minioService.generatePresignedUrls(videoDetailsInfoDTO);
    }

    /**
     * 上传视频分片
     * 上传视频分片并且文件合并完成后将通过消息队列发送给python微服务进行视频分析
     *
     * @param partUploadCompleteDTO 分片上传完成信息
     * @return true 如果上传成功，false 如果上传失败
     */
    @Override
    public boolean sliceUpload(PartUploadCompleteDTO partUploadCompleteDTO) {
        // 获取总记录数
        VideoUploadProgress videoInfo = redisService.getTotalPartsRecord(partUploadCompleteDTO);

        if (videoInfo == null) {
            return false;
        }

        // 判断uploadId是否一致 如果不一致则代表上传任务已经过期
        if (!videoInfo.getUploadId().equals(partUploadCompleteDTO.getUploadId())) {
            return false;
        }

        // 从redis获取对应的part信息 如果没有则代表还没上传 保持幂等性
        if (redisService.getRepeatPartFromRedis(partUploadCompleteDTO)) {
            return true;
        }

        // 用redis记录分片上传完成的状态
        redisService.recordPartUploadComplete(partUploadCompleteDTO);


        // 更新已完成分片数 幂等性更新
        Long slices = redisService.updateUploadVideoStatus(videoInfo);

        // 判断是否所有分片都上传完成
        if (!slices.equals(Long.parseLong(videoInfo.getTotalParts()))) {
            return true;
        }

        // 进行合并操作
        try {
            minioService.complete(videoInfo.getFileName(), videoInfo.getUploadId(), redisService.getParts(videoInfo.getFileName()));
        } catch (Exception e) {
            throw new UploadMultipartCompeteFailedException(e.getMessage());
        }

        ViFileDTO viFileDTO = VideoUploadProgressConverterUtil.toVIFileDTO(
                videoInfo,
                minioProperties.getVideoUploadBucketName());

        // 保存文件持久化到数据库
        boolean isAdded = fileService.addFile(viFileDTO);

        // 将视频实体通过消息队列发给python微服务进行视频分析处理
        if (isAdded) {
            fileProducer.send(viFileDTO);
            return true;
        }

        return false;
    }

    /**
     * 保存视频封面
     *
     * @param file 视频封面文件
     * @return 封面url
     */
    @Override
    public String saveVideoCover(MultipartFile file) throws IOException {
        // 进行文件检查
        String original = filePreFilterService.preCheckVideoCover(file);

        // 使用UUID去重文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "-" + original;

        // 上传文件到minIO
        return minioService.uploadImage(newFileName, file);
    }
}
