package com.framework.service;

import com.framework.model.LoginBodyModel;

/**
 * @author dotm
 * JWT 服务接口。
 *
 * <p>封装 JWT 令牌的创建 Token 的能力，
 * 由实现类负责具体加密和解析逻辑。</p>
 */
public interface AuthTokenService {

    /**
     * 创建 JWT 令牌。
     */
    String createJwtToken(LoginBodyModel sysUser);

    /**
     * 存入redis
     */
    void saveToken(LoginBodyModel loginBodyModel, Integer ttl);

    /**
     * 删除redis
     */
    void deleteToken(String uuid);
}

