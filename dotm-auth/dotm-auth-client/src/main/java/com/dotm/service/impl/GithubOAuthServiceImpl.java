package com.dotm.service.impl;

import com.dotm.config.security.github.properties.GithubProviderProperties;
import com.dotm.config.security.SafeComponent;
import com.dotm.constants.OAuthConstants;
import com.dotm.entity.dto.oauth.GithubOAuthCodeDTO;
import com.dotm.entity.dto.oauth.GithubOAuthDTO;
import com.framework.model.GithubAuthenticationToken;
import com.dotm.entity.model.oauth.SysAuthOauth;
import com.dotm.entity.vo.GithubCallBackVO;
import com.dotm.entity.vo.GithubTokenVO;
import com.dotm.entity.vo.GithubUserVO;
import com.dotm.entity.vo.SysAuthOauthVO;
import com.dotm.service.GithubOAuthService;
import com.dotm.service.SysAuthOauthService;
import com.dotm.utils.BuildRequestUtil;
import com.dotm.utils.RequestResponseUtil;
import com.framework.exception.login.LoginAuthError;
import com.framework.model.LoginBodyAuthentication;
import com.framework.model.LoginBodyModel;
import com.framework.properties.JwtProperties;
import com.framework.service.AuthTokenService;
import com.framework.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
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
@Slf4j
public class GithubOAuthServiceImpl implements GithubOAuthService {

    private final SafeComponent safeComponent;

    private final AuthTokenService authTokenService;

    private final BuildRequestUtil buildRequestUtil;

    private final RedisCacheService redisCacheService;

    private final JwtProperties jwtProperties;

    private final RequestResponseUtil requestResponseUtil;

    private final SysAuthOauthService sysAuthOauthService;

    private final GithubProviderProperties provider;

    private final AuthenticationManager authenticationManager;

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
    public GithubCallBackVO githubCallBackHandler(GithubOAuthCodeDTO githubOAuthCodeDTO) {
        // 校验state值 防止CSRF攻击
        safeComponent.checkCRSF(githubOAuthCodeDTO.getState());

        // 通过code获取access_token
        GithubTokenVO githubTokenVO = getAccessToken(githubOAuthCodeDTO.getCode());

        // 校验结果
        if (githubTokenVO == null || !githubTokenVO.isSuccess()) {
            String msg = githubTokenVO == null ? "empty response" : githubTokenVO.errorDescription();
            throw new LoginAuthError(msg);
        }

        // 发请求获取用户信息
        GithubUserVO githubUserVO = getUserInfo(githubTokenVO.accessToken());

        // 获取第三方账号绑定信息
        SysAuthOauthVO oauthUser = sysAuthOauthService.getByProviderAndOpenId(OAuthConstants.GITHUB, githubUserVO.id().toString());

        // 将第三方用户信息转换为系统第三方对象
        SysAuthOauth sysAuthOauth = sysAuthOauthService.toSysAuthOauth(
                githubUserVO,
                oauthUser == null || oauthUser.getUserId() == null ? null : oauthUser.getUserId());

        // 进行系统第三方账户与系统用户的绑定处理 这里必定有userId
        SysAuthOauth bindHandlerObject = bindThirdPartyAccount(sysAuthOauth);
        if (bindHandlerObject == null) {
            throw new LoginAuthError("Failed to bind third-party account");
        }

        // 进行Authentication认证 检测用户状态
        Authentication authenticate = null;
        try {
            authenticate = authenticationManager.authenticate(new GithubAuthenticationToken(bindHandlerObject.getUserId()));
        } catch (Exception ex) {
            log.error("authenticate 失败, 类型={}", ex.getClass().getName(), ex);
            log.error("cause 类型={}", ex.getCause() == null ? "null" : ex.getCause().getClass().getName());
            throw ex;
        }

        // 拿到认证用户信息
        LoginBodyAuthentication user = (LoginBodyAuthentication) authenticate.getPrincipal();

        // 设置基本信息
        Objects.requireNonNull(user).setUserAgent(githubOAuthCodeDTO.getUserAgent());
        user.setIp(githubOAuthCodeDTO.getIp());

        // 获取token
        String token = createToken(Objects.requireNonNull(user));

        // 返回结果
        return GithubCallBackVO.builder()
                .token(token)
                .githubUserVO(githubUserVO)
                .build();
    }

    /**
     * 拿到access_token的专属方法
     */
    @Override
    public GithubTokenVO getAccessToken(String code) {
        // 构造请求体
        HttpEntity<MultiValueMap<String, String>> request = buildRequestUtil
                .buildRequestForGithubFetchAccessToken(code);

        // 发请求获取响应体
        return requestResponseUtil.post(request,
                Objects.requireNonNull(provider.getTokenUri()), GithubTokenVO.class);
    }

    /**
     * 获取github用户信息
     */
    @Override
    public GithubUserVO getUserInfo(String accessToken) {
        // 构造请求体
        HttpEntity<MultiValueMap<String, String>> requestUserInfo =
                buildRequestUtil.buildRequestForGithubFetchUserInfo(accessToken);

        // 发请求获取响应体
        return requestResponseUtil.get(requestUserInfo, Objects.requireNonNull(provider.getUserInfoUri()), GithubUserVO.class);
    }

    /**
     * 生成jwt token的抽离方法
     *
     * @param user      认证用户信息
     * @return jwt
     */
    private String createToken(LoginBodyAuthentication user) {
        //生成jwt返回前端 这里会存入redis用户数据
        return authTokenService.createJwtToken(user);
    }

    /**
     * 绑定第三方账号 已存在同平台同openId的记录时更新为最新信息
     */
    @Override
    public SysAuthOauth bindThirdPartyAccount(SysAuthOauth sysAuthOauth) {
        return sysAuthOauth.getUserId() != null ?
                (sysAuthOauthService.update(sysAuthOauth, null) ? sysAuthOauth : null) :
                (sysAuthOauthService.bindAuthOauth(sysAuthOauth) ? sysAuthOauth : null);
    }
}
