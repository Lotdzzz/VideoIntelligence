package com.framework.service;

import org.springframework.data.redis.core.BoundSetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author dotm
 * Redis 缓存服务接口。
 *
 * <p>封装常用的 Redis 操作能力，包含 Value、List、Set、Map(Hash) 以及 Key 管理。
 * 该接口的方法设计与 `RedisCache` 工具类保持一致，便于业务层统一调用。</p>
 */
public interface RedisCacheService {

    /**
     * 缓存基本的对象，Integer、String、实体类等。
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    <T> void setCacheObject(String key, T value);

    /**
     * 缓存基本的对象，Integer、String、实体类等。
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    <T> void setCacheObject(String key, T value, Integer timeout, TimeUnit timeUnit);

    /**
     * 设置有效时间。
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    boolean expire(String key, long timeout);

    /**
     * 设置有效时间。
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 获取有效时间。
     *
     * @param key Redis键
     * @return 有效时间
     */
    long getExpire(String key);

    /**
     * 判断 key 是否存在。
     *
     * @param key 键
     * @return true 存在 false 不存在
     */
    Boolean hasKey(String key);

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    <T> T getCacheObject(String key);

    /**
     * 删除单个对象。
     *
     * @param key 键
     * @return 是否删除成功
     */
    boolean deleteObject(String key);

    /**
     * 删除集合对象。
     *
     * @param collection 多个对象
     * @return 是否删除成功
     */
    boolean deleteObject(Collection collection);

    /**
     * 缓存 List 数据。
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的 List 数据
     * @return 缓存的对象数量
     */
    <T> long setCacheList(String key, List<T> dataList);

    /**
     * 获得缓存的 list 对象。
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    <T> List<T> getCacheList(String key);

    /**
     * 缓存 Set。
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    <T> BoundSetOperations<String, T> setCacheSet(String key, Set<T> dataSet);

    /**
     * 获得缓存的 set。
     *
     * @param key 缓存键值
     * @return 缓存数据集合
     */
    <T> Set<T> getCacheSet(String key);

    /**
     * 缓存 Map。
     *
     * @param key     缓存键值
     * @param dataMap 缓存数据
     */
    <T> void setCacheMap(String key, Map<String, T> dataMap);

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
     * 获得匹配指定模式的 Redis Key 集合。
     *
     * @param pattern 字符串前缀
     * @return Key 集合
     */
    Collection<String> keys(String pattern);
}

