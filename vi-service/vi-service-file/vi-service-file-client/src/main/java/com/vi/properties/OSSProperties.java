package com.vi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author dotm
 * description 阿里云OSS配置类
 */
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class OSSProperties {

    /**
     * 阿里云OSS的Access Key ID
     */
    private String accessKeyId;

    /**
     * 阿里云OSS的Access Key Secret
     */
    private String accessKeySecret;

    /**
     * 阿里云OSS的Bucket Name
     */
    private String bucketName;

    /**
     * 阿里云OSS的Endpoint
     */
    private String endpoint;
}