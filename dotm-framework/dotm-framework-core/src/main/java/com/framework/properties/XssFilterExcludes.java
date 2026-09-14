package com.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author dotm
 * Xss过滤器排除配置
 */
@Component
@ConfigurationProperties(prefix = "excludes.xss")
@Data
public class XssFilterExcludes {

    private String[] urls;

}
