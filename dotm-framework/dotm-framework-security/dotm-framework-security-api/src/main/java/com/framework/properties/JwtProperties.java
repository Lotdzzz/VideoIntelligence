package com.framework.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author dotm
 */
@Component
@ConfigurationProperties(prefix = "web.jwt")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtProperties {

    /**
     * 设置jwt签名加密秘钥
     */
    private String secretKey;

    /**
     * 设置过期时间 单位分钟
     */
    private Integer ttl;

    /**
     * 设置前端发送请求请求头中存储jwt的字段
     */
    private String tokenName;

    /**
     * jwt生成的前缀默认的 后端解析时需要去掉
     */
    private String prefix;

    /**
     * 用来做Oauth的github的state过期时间
     */
    private String stateTtl;

}
