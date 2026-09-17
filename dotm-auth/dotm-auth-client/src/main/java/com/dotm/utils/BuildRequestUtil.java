package com.dotm.utils;

import com.dotm.config.security.github.properties.GithubProviderProperties;
import com.dotm.config.security.github.properties.GithubRegistrationProperties;
import com.dotm.entity.dto.oauth.GithubOAuthDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

/**
 * 构造请求体的方法
 *
 * @author dotm
 */
@Component
@RequiredArgsConstructor
public class BuildRequestUtil {

    private final GithubProviderProperties provider;

    private final GithubRegistrationProperties registration;

    /**
     * 此处构建请求体用于github获取access_token
     *
     * @param code 授权码
     * @return 构造好的请求体
     */
    public HttpEntity<MultiValueMap<String, String>> buildRequestForGithubFetchAccessToken(String code) {
        // form-urlencoded 必须用 MultiValueMap
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", registration.getClientId());
        body.add("client_secret", registration.getClientSecret());
        body.add("code", code);
        body.add("redirect_uri", registration.getRedirectUri());

        // Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return new HttpEntity<>(body, headers);
    }

    /**
     * 构造github授权登录的url
     *
     * @param githubOAuthDTO github授权登录入参对象
     * @return 构造后的url
     */
    public String buildUrlAuthForGithub(GithubOAuthDTO githubOAuthDTO) {
        return UriComponentsBuilder
                .fromUriString(Objects.requireNonNull(provider.getAuthorizationUri()))
                .queryParam("client_id", registration.getClientId())
                .queryParam("client_secret", registration.getClientSecret())
                .queryParam("redirect_uri", registration.getRedirectUri())
                .queryParam("scope", registration.getScope())
                .queryParam("state", githubOAuthDTO.getState())
                .queryParam("response_type", "code")
                .toUriString();
    }

    /**
     * 构造获取github用户信息的请求体
     *
     * @param accessToken access token
     * @return 构造好的请求体
     */
    public HttpEntity<MultiValueMap<String, String>> buildRequestForGithubFetchUserInfo(String accessToken) {
        // Header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.parseMediaType("application/vnd.github+json")));
        headers.set("User-Agent", "DOTM");                       // ★ GitHub 强制要求
        return new HttpEntity<>(headers);
    }
}
