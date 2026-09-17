package com.dotm.config.security.github.properties;

import com.dotm.constants.OAuthConstants;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 用来存储github的注册参数
 *
 * @author dotm
 */
@Component
public class GithubRegistrationProperties {

    /**
     * 用来获取github的注册信息 例如clientId clientSecret redirectUri等
     */
    private final OAuth2ClientProperties.Registration registration;

    public GithubRegistrationProperties(OAuth2ClientProperties oAuth2ClientProperties) {
        this.registration = Objects.requireNonNull(
                oAuth2ClientProperties.getRegistration().get(OAuthConstants.GITHUB),
                "未配置 spring.security.oauth2.client.registration.github"
        );
    }

    /**
     * 获取clientId
     */
    public String getClientId() {
        return registration.getClientId();
    }

    /**
     * 获取clientSecret
     */
    public String getClientSecret() {
        return registration.getClientSecret();
    }

    /**
     * 获取redirectUri
     */
    public String getRedirectUri() {
        return registration.getRedirectUri();
    }

    /**
     * 获取scope
     */
    public String getScope() {
        return String.join(" ", Objects.requireNonNull(registration.getScope()));
    }

    /**
     * 获取authorization-grant-type
     */
    public String getAuthorizationGrantType() {
        return registration.getAuthorizationGrantType();
    }

    /**
     * 获取clientName
     */
    public String getClientName() {
        return registration.getClientName();
    }
}
