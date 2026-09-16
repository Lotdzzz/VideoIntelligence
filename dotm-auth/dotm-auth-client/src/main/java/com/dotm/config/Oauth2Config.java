package com.dotm.config;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 这是一个oauth适配器 用于github weChat qq等第三方登录
 *
 * @author dotm
 */
@Configuration
public class Oauth2Config {

    /**
     * 专用于调用 GitHub 等第三方服务的请求响应器
     */
    @Bean
    public RestTemplate githubRestTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                // 配置连接池工厂
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .build();
    }
}
