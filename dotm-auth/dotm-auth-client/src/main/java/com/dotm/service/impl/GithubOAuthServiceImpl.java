package com.dotm.service.impl;

import com.dotm.constants.OAuthConstants;
import com.dotm.entity.dto.oauth.GithubOAuthCodeDTO;
import com.dotm.entity.dto.oauth.GithubOAuthDTO;
import com.dotm.entity.dto.oauth.SysAuthOauthDTO;
import com.dotm.entity.model.SysAuthOauth;
import com.dotm.entity.vo.GithubTokenVO;
import com.dotm.entity.vo.GithubUserVO;
import com.dotm.entity.vo.SysAuthOauthVO;
import com.dotm.service.GithubOAuthService;
import com.dotm.service.SysAuthOauthService;
import com.dotm.utils.BuildRequestUtil;
import com.dotm.utils.RequestResponseUtil;
import com.framework.exception.login.LoginAuthError;
import com.framework.exception.login.LoginExpireOut;
import com.framework.model.LoginBodyModel;
import com.framework.properties.JwtProperties;
import com.framework.service.AuthTokenService;
import com.framework.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * github业务实现类
 *
 * @author dotm
 */
@Service
@RequiredArgsConstructor
public class GithubOAuthServiceImpl implements GithubOAuthService {

    private final AuthTokenService authTokenService;

    private final BuildRequestUtil buildRequestUtil;

    private final RedisCacheService redisCacheService;

    private final JwtProperties jwtProperties;

    private final OAuth2ClientProperties oAuth2ClientProperties;

    private final RequestResponseUtil requestResponseUtil;

    private final SysAuthOauthService sysAuthOauthService;

    /**
     * 获取github授权登录url
     *
     * @return github授权登录url
     */
    @Override
    public String getAuthorizationURL() {
        // 构造当前用户的state值 防止CSRF伪造回调
        String state = UUID.randomUUID().toString();

        // 获取请求相关信息
        GithubOAuthDTO githubOAuthDTO = new GithubOAuthDTO();
        githubOAuthDTO.setState(state);

        // 将state存入redis便于后面验证用户归属
        redisCacheService.setCacheObject(
                OAuthConstants.STATE_PREFIX + state,
                state,
                Integer.valueOf(jwtProperties.getStateTtl()),
                TimeUnit.MINUTES);

        // 构造url
        return buildRequestUtil.buildUrlAuthForGithub(githubOAuthDTO);
    }

    /**
     * 处理github回调逻辑
     *
     * @param githubOAuthCodeDTO github返回的授权信息
     * @return github用户信息
     */
    @Override
    public String githubCallBackHandler(GithubOAuthCodeDTO githubOAuthCodeDTO) {
        // 获取要发送到目标github的地址
        OAuth2ClientProperties.Provider pov = oAuth2ClientProperties.getProvider().get(OAuthConstants.GITHUB);
        OAuth2ClientProperties.Registration reg = oAuth2ClientProperties.getRegistration().get(OAuthConstants.GITHUB);

        // 验证state值是否存在
        String state = githubOAuthCodeDTO.getState();
        String redisState = redisCacheService.getCacheObject(OAuthConstants.STATE_PREFIX + state);
        if (redisState == null || redisState.isEmpty()) {
            throw new LoginExpireOut(null);
        }

        // 删除redis中的state值
        redisCacheService.deleteObject(OAuthConstants.STATE_PREFIX + state);

        // 构造请求体
        HttpEntity<MultiValueMap<String, String>> request = buildRequestUtil
                .buildRequestForGithubFetchAccessToken(githubOAuthCodeDTO.getCode());

        // 发请求获取响应体
        GithubTokenVO githubTokenVO = requestResponseUtil.post(request,
                Objects.requireNonNull(pov.getTokenUri()), GithubTokenVO.class);

        // 校验结果
        if (githubTokenVO == null || !githubTokenVO.isSuccess()) {
            String msg = githubTokenVO == null ? "empty response" : githubTokenVO.errorDescription();
            throw new LoginAuthError(msg);
        }

        // 构造请求体
        HttpEntity<MultiValueMap<String, String>> requestUserInfo =
                buildRequestUtil.buildRequestForGithubFetchUserInfo(githubTokenVO.accessToken());

        // 发请求获取用户信息
        GithubUserVO githubUserVO = requestResponseUtil.get(requestUserInfo, Objects.requireNonNull(pov.getUserInfoUri()), GithubUserVO.class);

        // 获取第三方账号绑定信息
        SysAuthOauthVO oauthUser = sysAuthOauthService.getByProviderAndOpenId(OAuthConstants.GITHUB, githubUserVO.id().toString());

        // 判断用户是否绑定
        boolean isSave = false;
        boolean isUpdate = false;
        SysAuthOauth sysAuthOauth = null;
        if (oauthUser == null) {
            // 未绑定 进行绑定
            sysAuthOauth = sysAuthOauthService.toSysAuthOauth(githubUserVO, null);
            SysAuthOauthDTO sysAuthOauthDTO = new SysAuthOauthDTO();
            BeanUtils.copyProperties(sysAuthOauth, sysAuthOauthDTO);
            isSave = sysAuthOauthService.bindAuthOauth(sysAuthOauthDTO);
        } else {
            // 已绑定 更新为最新信息
            sysAuthOauth = sysAuthOauthService.toSysAuthOauth(githubUserVO, Objects.requireNonNull(oauthUser).getUserId());
            isUpdate = sysAuthOauthService.update(sysAuthOauth, null);
        }

        //生成jwt返回前端 之所以在这里不返回前端是因为重定向适合发小数据
        String token = authTokenService.createJwtToken(
                LoginBodyModel.builder()
                        .username(sysAuthOauth.getOauthName())
                        .userAgent(githubOAuthCodeDTO.getUserAgent())
                        .build());

        return (isSave || isUpdate) ? token : null;
    }
}
