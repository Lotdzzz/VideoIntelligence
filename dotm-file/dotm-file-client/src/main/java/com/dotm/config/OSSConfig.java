package com.dotm.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.dotm.properties.OSSProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dotm
 * description 阿里云OSS配置类
 */
@Configuration
public class OSSConfig {

    /**
     * OSS 客户端是线程安全的，全局单例即可。
     * destroyMethod = "shutdown" 保证容器关闭时优雅释放连接池。
     */
    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(OSSProperties props) {
        ClientBuilderConfiguration cfg = new ClientBuilderConfiguration();
        return new OSSClientBuilder().build(
                props.getEndpoint(),
                props.getAccessKeyId(),
                props.getAccessKeySecret(),
                cfg
        );
    }
}
