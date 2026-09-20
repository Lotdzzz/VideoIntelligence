package com.vi.service.impl;

import com.framework.service.RedisCacheForHashService;
import com.framework.service.RedisCacheService;
import com.vi.constants.FileConstants;
import com.vi.entity.dto.PartUploadCompleteDTO;
import com.vi.entity.model.SlicePartInfo;
import com.vi.entity.model.VideoUploadProgress;
import com.vi.service.RedisService;
import com.vi.utils.VideoUploadProgressConverterUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RedisServiceImpl类是RedisService接口的实现类，用于处理与Redis相关的业务逻辑。
 *
 * @author dotm
 */
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisCacheService redisCacheService;

    private final RedisCacheForHashService redisCacheForHashService;

    /**
     * 将视频信息存入redis中
     *
     * @param info 视频信息
     */
    @Override
    public void saveVideoInfoToRedis(VideoUploadProgress info) {
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
     * 获取所有已上传的分片信息
     *
     * @param fileName 文件名
     * @return 已上传的分片信息列表
     */
    @Override
    public List<CompletedPart> getParts(String fileName) {
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
    @Override
    public Long updateUploadVideoStatus(VideoUploadProgress videoInfo) {
        return redisCacheForHashService.incrementCacheMapValue(
                FileConstants.VIDEO_SLICE_REDIS_PREFIX + videoInfo.getFileName(),
                VideoUploadProgress.COMPLETED_PARTS_KEY,
                1
        );
    }

    /**
     * 获取总记录数
     */
    @Override
    public VideoUploadProgress getTotalPartsRecord(PartUploadCompleteDTO partUploadCompleteDTO) {
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
    @Override
    public void recordPartUploadComplete(PartUploadCompleteDTO partUploadCompleteDTO) {
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
    @Override
    public boolean getRepeatPartFromRedis(@NotNull PartUploadCompleteDTO partUploadCompleteDTO) {
        // 从redis获取已完成的分片数量
        SlicePartInfo completedParts = redisCacheService.getCacheObject(
                FileConstants.VIDEO_SLICE_PREFIX + partUploadCompleteDTO.getFilename() + ":" + partUploadCompleteDTO.getPartNumber());
        return completedParts != null;
    }
}
