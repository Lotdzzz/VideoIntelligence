package com.vi.service;

import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.vo.VideoSliceMissionVo;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.List;

/**
 * MinioService类用于处理与MinIO服务相关的业务逻辑
 *
 * @author dotm
 */
public interface MinioService {

    /**
     * 上传文件预签名方法
     * 前端调用接口先获取预签名URL，然后使用该URL上传文件到MinIO
     * 该方法可以根据需要生成预签名URL，并返回给前端
     */
    public String generateUploadPresignedUrl(String objectName);

    /**
     * 下载文件预签名方法
     * 前端调用接口先获取预签名URL，然后使用该URL下载文件从MinIO
     * 该方法可以根据需要生成预签名URL，并返回给前端
     */
    public String generateDownloadPresignedUrl(String objectName);

    /**
     * 根据分片数生成预签名url
     *
     * @return 预签名url列表
     */
    public String initiateMultipartUpload(String objectName);

    /**
     * 根据分片数生成预签名url
     *
     * @return 预签名url列表
     */
    public VideoSliceMissionVo generatePresignedUrls(ViFileDTO videoDetailsInfoDTO);


    /**
     * 生成分片上传的预签名URL
     *
     * @param objectName 最终文件名
     * @param uploadId   分片上传任务的ID
     * @param partNumber 分片编号
     * @return 预签名URL
     */
    public String s3ProtocolGeneratePartUrl(String objectName, String uploadId, int partNumber);


    /**
     * 完成分片上传
     *
     * @param objectName 最终文件名
     * @param uploadId   分片上传任务的ID
     * @param parts      分片信息
     */
    public void complete(String objectName, String uploadId, List<CompletedPart> parts);

    /**
     * 获取MinIO桶名称
     *
     * @return MinIO桶名称
     */
    public String getBucketName();
}
