package com.dotm.service;

import com.dotm.entity.dto.oauth.GithubOAuthCodeDTO;
import com.dotm.entity.model.oauth.SysAuthOauth;
import com.dotm.entity.vo.GithubCallBackVO;
import com.dotm.entity.vo.GithubTokenVO;
import com.dotm.entity.vo.GithubUserVO;

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
     * @return github用户信息和token
     */
    GithubCallBackVO githubCallBackHandler(GithubOAuthCodeDTO githubOAuthCodeDTO);

    /**
     * 拿到access_token的专属方法
     */
    GithubTokenVO getAccessToken(String code);

    /**
     * 获取github用户信息
     */
    GithubUserVO getUserInfo(String accessToken);

    /**
     * 绑定第三方账号 已存在同平台同openId的记录时更新为最新信息
     */
    SysAuthOauth bindThirdPartyAccount(SysAuthOauth sysAuthOauth);
}
