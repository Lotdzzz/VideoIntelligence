package com.framework.utils;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * @author dotm
 * token工具类
 */
public class TokenUtil {

    /**
     * 密钥转码加密
     */
    public static SecretKey getSecretKey(String secretKey) {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
