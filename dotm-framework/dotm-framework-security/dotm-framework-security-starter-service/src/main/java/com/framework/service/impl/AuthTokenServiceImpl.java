package com.framework.service.impl;

import com.framework.constants.Constants;
import com.framework.constants.TokenConstants;
import com.framework.model.LoginBodyModel;
import com.framework.properties.JwtProperties;
import com.framework.security.TokenStore;
import com.framework.service.AuthTokenService;
import com.framework.utils.TokenUtil;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author dotm
 * JWT 服务实现类。
 *
 * <p>负责 JWT 令牌的创建、校验、解析和请求头 Token 提取，
 * 行为与原有静态工具类保持一致。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTokenServiceImpl implements AuthTokenService {

    private final JwtProperties jwtProperties;

    private final TokenStore TokenStore;

    /**
     * jwt 生成过程，用于用户登录成功后。
     * 根据uuid+username+userAgent设置token
     * 在根据uuid为key sysUser为value为value存入redis设置过期时间
     */
    @Override
    public String createJwtToken(LoginBodyModel sysUser) {
        //生成uuid
        UUID uuid = UUID.randomUUID();
        sysUser.setUuid(uuid.toString());

        //拼装claims
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(Constants.USERNAME, sysUser.getUsername()); //用户名
        claims.put(TokenConstants.USER_AGENT, sysUser.getUserAgent()); //设备指纹
        claims.put(TokenConstants.UUID, sysUser.getUuid()); //uuid

        //存入redis刷新过期时间
        saveToken(sysUser, jwtProperties.getTtl());

        return Jwts.builder()
                .claims(claims)
                .signWith(TokenUtil.getSecretKey(jwtProperties.getSecretKey()))
                .compact();
    }

    /**
     * 存入缓存
     */
    @Override
    public void saveToken(LoginBodyModel user, Integer ttl) {
        TokenStore.setCacheObject(user.getUuid(), user, ttl, TimeUnit.MINUTES);
    }

    /**
     * 删除redis
     */
    @Override
    public void deleteToken(String uuid) {
        TokenStore.deleteObject(uuid);
    }
}


