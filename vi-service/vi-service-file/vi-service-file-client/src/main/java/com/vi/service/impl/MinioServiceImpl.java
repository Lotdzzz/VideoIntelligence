package com.vi.service.impl;

import com.framework.exception.upload.UploadPreSignException;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.vo.VideoReturnInfoVO;
import com.vi.entity.vo.VideoSliceMissionVo;
import com.vi.service.MinioService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinioService类用于处理与MinIO服务相关的业务逻辑
 *
 * @author dotm
 */
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    @Value("${minio.expiry-time:30}") // 默认过期时间为3600秒（1小时）
    private Integer expiryTime;

    @Getter
    @Value("${minio.bucket-name}") // 获取MinIO桶名称
    private String bucketName;

    /**
     * 上传文件预签名方法
     * 前端调用接口先获取预签名URL，然后使用该URL上传文件到MinIO
     * 该方法可以根据需要生成预签名URL，并返回给前端
     */
    public String generateUploadPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .expiry(expiryTime, TimeUnit.MINUTES)
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
                 ServerException e) {
            throw new UploadPreSignException(e.getMessage());
        }
    }

    /**
     * 下载文件预签名方法
     * 前端调用接口先获取预签名URL，然后使用该URL下载文件从MinIO
     * 该方法可以根据需要生成预签名URL，并返回给前端
     */
    public String generateDownloadPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .expiry(expiryTime, TimeUnit.MINUTES)
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
                 ServerException e) {
            throw new UploadPreSignException(e.getMessage());
        }
    }

    /**
     * 告知minIO创建一个分片上传任务并获取任务凭证uploadId
     * 该方法可以根据需要生成分片上传任务，并返回给前端
     *
     * @param objectName 最终文件名
     */
    public String initiateMultipartUpload(String objectName) {
        return s3Client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build()).uploadId();
    }

    /**
     * 根据分片数生成预签名url
     *
     * @return 预签名url列表
     */
    public VideoSliceMissionVo generatePresignedUrls(ViFileDTO videoDetailsInfoDTO) {
        // 创建结果集
        List<VideoReturnInfoVO> result = new ArrayList<>();
        String filename = videoDetailsInfoDTO.getObjectName();
        String uploadId = videoDetailsInfoDTO.getUploadId();

        // 生成每个分片的预签名url
        for (int i = 1; i <= videoDetailsInfoDTO.getChunkCount(); i++) {
            VideoReturnInfoVO videoReturnInfoVO = new VideoReturnInfoVO();
            videoReturnInfoVO.setPartNumber((long) i);
            videoReturnInfoVO.setPreSignedUrl(s3ProtocolGeneratePartUrl(filename, uploadId, i));
            videoReturnInfoVO.setFilename(filename);
            result.add(videoReturnInfoVO);
        }

        VideoSliceMissionVo videoSliceMissionVo = new VideoSliceMissionVo();
        videoSliceMissionVo.setVideoReturnInfoVOList(result);
        videoSliceMissionVo.setUploadId(uploadId);
        videoSliceMissionVo.setPartSize(videoDetailsInfoDTO.getChunkSize());
        videoSliceMissionVo.setTotalParts(videoDetailsInfoDTO.getChunkCount().intValue());
        return videoSliceMissionVo;
    }

    /**
     * 生成分片上传的预签名URL
     *
     * @param objectName 最终文件名
     * @param uploadId   分片上传任务的ID
     * @param partNumber 分片编号
     * @return 预签名URL
     */
    public String s3ProtocolGeneratePartUrl(String objectName, String uploadId, int partNumber) {
        UploadPartRequest uploadPartRequest =
                UploadPartRequest.builder()
                        .bucket(bucketName)
                        .key(objectName)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .build();

        PresignedUploadPartRequest presigned =
                s3Presigner.presignUploadPart(UploadPartPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(30))
                        .uploadPartRequest(uploadPartRequest)
                        .build()
                );

        return presigned.url().toString();
    }

    /**
     * 完成分片上传
     *
     * @param objectName 最终文件名
     * @param uploadId   分片上传任务的ID
     * @param parts      分片信息
     */
    public void complete(String objectName, String uploadId, List<CompletedPart> parts) {
        CompletedMultipartUpload completed =
                CompletedMultipartUpload.builder()
                        .parts(parts)
                        .build();

        CompleteMultipartUploadRequest request =
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucketName)
                        .key(objectName)
                        .uploadId(uploadId)
                        .multipartUpload(completed)
                        .build();

        s3Client.completeMultipartUpload(request);
    }
}
