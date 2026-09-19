package com.vi.config;

import com.vi.properties.MinioProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * S3配置类
 *
 * @author dotm
 */
@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final MinioProperties minioProperties;

    /**
     * 配置S3Client Bean，用于与S3兼容的存储服务（如MinIO）进行交互
     *
     * @return S3Client实例
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(minioProperties.getEndpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials.create(
                                        minioProperties.getAccessKey(),
                                        minioProperties.getSecretKey()
                                )
                        )
                )
                .forcePathStyle(true)
                .build();
    }

    /**
     * 配置S3Presigner Bean，用于生成预签名URL，以便客户端可以直接上传或下载文件
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(minioProperties.getEndpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        minioProperties.getAccessKey(),
                                        minioProperties.getSecretKey()
                                )
                        )
                )
                .build();
    }
}
