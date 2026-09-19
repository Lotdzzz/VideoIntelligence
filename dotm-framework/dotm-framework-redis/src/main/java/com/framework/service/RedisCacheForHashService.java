package com.framework.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author dotm
 * Redis 缓存服务接口，专门为 Hash 类型提供操作能力。
 */
public interface RedisCacheForHashService {
    /**
     * 缓存 Map。
     *
     * @param key     缓存键值
     * @param dataMap 缓存数据
     */
    <T> void setCacheMap(String key, Map<String, T> dataMap);

    /**
     * 缓存 Map。
     *
     * @param key     缓存键值
     * @param dataMap 缓存数据
     * @param expire  过期时间
     * @param timeUnit 时间单位
     */
    <T> void setCacheMap(String key, Map<String, T> dataMap, long expire, java.util.concurrent.TimeUnit timeUnit);

    /**
     * 获得缓存的 Map。
     *
     * @param key 缓存键值
     * @return 缓存数据
     */
    <T> Map<String, T> getCacheMap(String key);

    /**
     * 往 Hash 中存入数据。
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    <T> void setCacheMapValue(String key, String hKey, T value);

    /**
     * 获取 Hash 中的数据。
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    <T> T getCacheMapValue(String key, String hKey);

    /**
     * 获取多个 Hash 中的数据。
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    <T> List<T> getMultiCacheMapValue(String key, Collection<Object> hKeys);

    /**
     * 删除 Hash 中的某条数据。
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return 是否成功
     */
    boolean deleteCacheMapValue(String key, String hKey);

    /**
     * 增加 Hash 中的某个字段的值。
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param delta 增加的值
     */
    void incrementCacheMapValue(String key, String hKey, int delta);
}
