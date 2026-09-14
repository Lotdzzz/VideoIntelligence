package com.framework.service.impl;

import com.framework.constants.TokenConstants;
import com.framework.model.LoginBodyAuthentication;
import com.framework.model.LoginBodyModel;
import com.framework.properties.JwtProperties;
import com.framework.security.TokenStore;
import com.framework.service.GatewayTokenService;
import com.framework.utils.TokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author dotm
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GatewayTokenServiceImpl implements GatewayTokenService {

    private final JwtProperties jwtProperties;

    private final TokenStore tokenStore;

    /**
     * 验证 token 令牌。
     *
     * @param token jwt 本体
     * @return 结果
     */
    @Override
    @SuppressWarnings("all")
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(TokenUtil.getSecretKey(jwtProperties.getSecretKey()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌格式：{}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT令牌格式错误：{}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌参数为空：{}", e.getMessage());
        }
        return false;
    }

    /**
     * 解析 Token 所有载荷（不验证签名和过期时间）。
     *
     * @param secret JWT 密钥
     * @param token  jwt 本体
     * @return Token 载荷内容
     */
    @Override
    public Claims getClaimsFromToken(String secret, String token) {
        return Jwts.parser()
                .verifyWith(TokenUtil.getSecretKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从请求头中提取 Token（去除前缀）。
     *
     * @param header 请求头中的 token 值
     * @param prefix token 前缀
     * @return 去除前缀后的 token；如果格式不匹配则返回 null
     */
    @Override
    public String extractTokenFromHeader(String header, String prefix) {
        if (header == null || !header.startsWith(prefix + " ")) {
            return null;
        }
        return header.substring(prefix.length() + 1);
    }

    /**
     * 指纹校验与令牌刷新
     *
     * @param jwt 令牌
     */
    @Override
    public LoginBodyModel detailsValidateRefreshToken(String jwt) {
        //获取UUID
        Claims claims = getClaimsFromToken(jwtProperties.getSecretKey(), jwt);

        //拿到用户唯一标识UUID
        String uuid = (String) claims.get(TokenConstants.UUID);

        //从redis获取用户指纹
        LoginBodyAuthentication sysUser = tokenStore.getCacheObject(uuid);

        if (sysUser == null) {
            return null;
        }

        //设置uuid
        sysUser.setUuid(uuid);

        //刷新过期时间
        refreshToken(sysUser, jwtProperties.getTtl());
        return sysUser;
    }

    /**
     * 刷新令牌过期时间
     */
    @Override
    public void refreshToken(LoginBodyModel sysUser, Integer ttl) {
        // 剩余过期时间（秒），key 不存在返回 -2，无过期返回 -1
        long remaining = tokenStore.getExpire(sysUser.getUuid());

        // 只有剩余时间低于总 TTL 的三分之一时才续期，避免每个请求都写 Redis
        long thresholdSeconds = ttl.longValue() * 60L / 3;
        if (remaining < thresholdSeconds) {
            tokenStore.setCacheObject(sysUser.getUuid(), sysUser, ttl, TimeUnit.MINUTES);
        }
    }
}
