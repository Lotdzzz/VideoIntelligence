package com.vi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO属性类
 *
 * @author dotm
 */
@Data
@ConfigurationProperties(prefix = "minio")
@Component
public class MinioProperties {

    /**
     * MinIO服务的端点URL
     */
    private String endpoint;

    /**
     * MinIO服务的访问密钥
     */
    private String accessKey;

    /**
     * MinIO服务的秘密密钥
     */
    private String secretKey;

    /**
     * 用于获取预签名url的持续时间
     */
    private Integer expiryTime;

    /**
     * 视频文件桶名称
     */
    private String videoUploadBucketName;

    /**
     * 封面文件桶名称
     */
    private String coverUploadBucketName;
}
