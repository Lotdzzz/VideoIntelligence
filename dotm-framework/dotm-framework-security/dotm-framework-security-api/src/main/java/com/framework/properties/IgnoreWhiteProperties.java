package com.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dotm
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.ignore")
public class IgnoreWhiteProperties {
    /**
     * 白名单列表
     */
    private List<String> urls = new ArrayList<>();
}
