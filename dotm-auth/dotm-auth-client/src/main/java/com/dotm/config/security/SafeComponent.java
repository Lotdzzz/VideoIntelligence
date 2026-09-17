package com.dotm.config.security;

import com.dotm.constants.OAuthConstants;
import com.framework.exception.login.LoginCSRFError;
import com.framework.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 安全组件，用于处理各类安全问题
 */
@Component
@RequiredArgsConstructor
public class SafeComponent {

    private final RedisCacheService redisCacheService;

    /**
     * 检查CRSF攻击
     *
     * @param state 前端传来的state值
     */
    public void checkCRSF(String state) {
        // 验证state值是否存在
        String redisState = redisCacheService.getCacheObject(OAuthConstants.STATE_PREFIX + state);
        if (redisState == null || redisState.isEmpty()) {
            throw new LoginCSRFError(null);
        }

        // 删除redis中的state值
        redisCacheService.deleteObject(OAuthConstants.STATE_PREFIX + state);
    }
}
