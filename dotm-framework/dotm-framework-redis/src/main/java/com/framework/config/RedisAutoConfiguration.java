package com.framework.config;

import com.framework.security.TokenStore;
import com.framework.service.RedisTokenStoreService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author dotm
 * Redis 自动配置类。
 * 用来横向模块之间解耦合
 */
@AutoConfiguration
@ConditionalOnClass(RedisTemplate.class)
public class RedisAutoConfiguration {

    /**
     * 创建 TokenStore Bean，使用 Redis 作为存储。
     * 只有容器中不存在这个bean时才创建
     *
     * @param redisTemplate RedisTemplate 实例，用于操作 Redis。
     * @return TokenStore 实例。
     */
    @Bean
    @ConditionalOnMissingBean(TokenStore.class)
    public TokenStore tokenStore(RedisTemplate<String, Object> redisTemplate) {
        return new RedisTokenStoreService(redisTemplate);
    }
}
