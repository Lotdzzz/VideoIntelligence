package com.framework.service;

import com.framework.model.LoginBodyModel;
import io.jsonwebtoken.Claims;

/**
 * @author dotm
 * JWT 服务接口。
 *
 * <p>封装 JWT 令牌的校验、解析以及从请求头中提取 Token 的能力，
 * 由实现类负责具体加密和解析逻辑。</p>
 */
public interface GatewayTokenService {

    /**
     * 验证 token 令牌。
     *
     * @param token  jwt 本体
     * @return 结果
     */
    boolean validateToken(String token);

    /**
     * 解析 Token 所有载荷（不验证签名和过期时间）。
     *
     * @param secret JWT 密钥
     * @param token  jwt 本体
     * @return Token 载荷内容
     */
    Claims getClaimsFromToken(String secret, String token);

    /**
     * 从请求头中提取 Token（去除前缀）。
     *
     * @param header 请求头中的 token 值
     * @param prefix token 前缀
     * @return 去除前缀后的 token；如果格式不匹配则返回 null
     */
    String extractTokenFromHeader(String header, String prefix);

    /**
     * 指纹校验与令牌刷新
     * @param jwt 令牌
     */
    LoginBodyModel detailsValidateRefreshToken(String jwt);

    /**
     * 刷新令牌过期时间
     */
    void refreshToken(LoginBodyModel sysUser, Integer ttl);
}
