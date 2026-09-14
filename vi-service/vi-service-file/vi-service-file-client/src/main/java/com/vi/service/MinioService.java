package com.vi.service;

import com.framework.exception.upload.UploadPreSignException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * MinioService类用于处理与MinIO服务相关的业务逻辑
 *
 * @author dotm
 */
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.expiry-time:30}") // 默认过期时间为3600秒（1小时）
    private Integer expiryTime;

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
}
