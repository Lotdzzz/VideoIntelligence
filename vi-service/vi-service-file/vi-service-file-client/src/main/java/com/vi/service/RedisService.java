package com.vi.service;

import com.vi.entity.dto.PartUploadCompleteDTO;
import com.vi.entity.model.VideoUploadProgress;
import jakarta.validation.constraints.NotNull;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.List;

/**
 * 文件模块封装的redis接口
 *
 * @author dotm
 */
public interface RedisService {

    /**
     * 将视频信息存入redis中
     *
     * @param info 视频信息
     */
    void saveVideoInfoToRedis(VideoUploadProgress info);

    /**
     * 获取所有已上传的分片信息
     *
     * @param fileName 文件名
     * @return 已上传的分片信息列表
     */
    List<CompletedPart> getParts(String fileName);

    /**
     * 更新已完成分片数 幂等性更新
     *
     * @param videoInfo 视频信息
     * @return 更新后的已完成分片数
     */
    Long updateUploadVideoStatus(VideoUploadProgress videoInfo);

    /**
     * 获取总记录数
     */
    VideoUploadProgress getTotalPartsRecord(PartUploadCompleteDTO partUploadCompleteDTO);

    /**
     * 用redis记录分片上传完成的状态
     *
     * @param partUploadCompleteDTO 分片上传完成信息
     */
    void recordPartUploadComplete(PartUploadCompleteDTO partUploadCompleteDTO);

    /**
     * 从redis获取对应的part信息 如果没有则代表还没上传 保持幂等性
     *
     * @param partUploadCompleteDTO 分片上传完成信息
     * @return true 如果已经上传过该分片，false 如果没有上传过
     */
    boolean getRepeatPartFromRedis(@NotNull PartUploadCompleteDTO partUploadCompleteDTO);
}
