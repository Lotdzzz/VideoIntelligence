package com.dotm.config.security.github.properties;

import com.dotm.constants.OAuthConstants;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 用来存储github的提供商配置参数
 *
 * @author dotm
 */
@Component
public class GithubProviderProperties {

    /**
     * 用来获取github的提供商信息 例如tokenUri authorizationUri userInfoUri等
     */
    private final OAuth2ClientProperties.Provider provider;

    public GithubProviderProperties(OAuth2ClientProperties oAuth2ClientProperties) {
        this.provider = Objects.requireNonNull(
                oAuth2ClientProperties.getProvider().get(OAuthConstants.GITHUB),
                "未配置 spring.security.oauth2.client.provider.github"
        );
    }

    /**
     * 获取tokenUri
     */
    public String getTokenUri() {
        return provider.getTokenUri();
    }

    /**
     * 获取authorizationUri
     */
    public String getAuthorizationUri() {
        return provider.getAuthorizationUri();
    }

    /**
     * 获取userInfoUri
     */
    public String getUserInfoUri() {
        return provider.getUserInfoUri();
    }

    /**
     * 获取user-name-attribute
     */
    public String getUserNameAttribute() {
        return provider.getUserNameAttribute();
    }
}
