package com.framework.service.impl;

import com.framework.constants.RedisSerializerConstants;
import com.framework.service.RedisCacheForHashService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class RedisCacheForHashServiceImpl implements RedisCacheForHashService {

    @Resource(name = RedisSerializerConstants.STRING_HASH_REDIS_TEMPLATE_BEAN_NAME)
    private final RedisTemplate redisTemplate;

    /**
     * 缓存 Map。
     *
     * @param key     缓存键值
     * @param dataMap 缓存数据
     * @param <T>     Map 值类型
     */
    @Override
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 缓存 Map。
     *
     * @param key     缓存键值
     * @param dataMap 缓存数据
     * @param expire  过期时间
     * @param timeUnit 时间单位
     */
    @Override
    public <T> void setCacheMap(String key, Map<String, T> dataMap, long expire, TimeUnit timeUnit) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
            redisTemplate.expire(key, expire, timeUnit);
        }
    }

    /**
     * 获得缓存的 Map。
     *
     * @param key 缓存键值
     * @param <T> 缓存值类型
     * @return 缓存数据
     */
    @Override
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往 Hash 中存入数据。
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     * @param <T>   Hash 值类型
     */
    @Override
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取 Hash 中的数据。
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @param <T>  Hash 值类型
     * @return Hash中的对象
     */
    @Override
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个 Hash 中的数据。
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @param <T>   Hash 值类型
     * @return Hash对象集合
     */
    @Override
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 删除 Hash 中的某条数据。
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return 是否成功
     */
    @Override
    public boolean deleteCacheMapValue(final String key, final String hKey) {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    /**
     * 增加 Hash 中的某个字段的值。
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param delta 增加的值
     */
    @Override
    public void incrementCacheMapValue(String key, String hKey, int delta) {
        redisTemplate.opsForHash().increment(key, hKey, delta);
    }
}
