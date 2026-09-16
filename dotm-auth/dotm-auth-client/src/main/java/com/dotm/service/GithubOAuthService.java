package com.dotm.service;

import com.dotm.entity.dto.oauth.GithubOAuthCodeDTO;

/**
 * github的业务接口
 *
 * @author dotm
 */
public interface GithubOAuthService {

    /**
     * 获取github授权登录url
     *
     * @return github授权登录url
     */
    String getAuthorizationURL();

    /**
     * 处理github回调逻辑
     *
     * @param githubOAuthCodeDTO github返回的授权信息
     */
    String githubCallBackHandler(GithubOAuthCodeDTO githubOAuthCodeDTO);
}
