package com.framework.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * @author dotm
 * Web配置类
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnClass(WebMvcConfigurer.class)
public class WebConfig implements WebMvcConfigurer {

    /**
     * 将本地静态资源目录映射成http资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:D:/uploadDir/");
    }
}
