package com.framework.service.impl;

import com.framework.constants.RedisSerializerConstants;
import com.framework.service.RedisCacheService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author dotm
 * Redis 缓存服务实现类。
 *
 * <p>功能与 `RedisCache` 工具类保持一致，供业务层通过依赖注入直接使用。</p>
 */
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    /**
     * Spring 注入的 Redis 操作模板。
     */
    @Resource(name = RedisSerializerConstants.OBJECT_REDIS_TEMPLATE_BEAN_NAME)
    private RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等。
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     * @param <T>   缓存值类型
     */
    @Override
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等。
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     * @param <T>      缓存值类型
     */
    @Override
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置有效时间。
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    @Override
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间。
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    @Override
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取有效时间。
     *
     * @param key Redis键
     * @return 有效时间
     */
    @Override
    public long getExpire(final String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 判断 key 是否存在。
     *
     * @param key 键
     * @return true 存在 false 不存在
     */
    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @param <T> 缓存值类型
     * @return 缓存键值对应的数据
     */
    @Override
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象。
     *
     * @param key 键
     * @return 是否删除成功
     */
    @Override
    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象。
     *
     * @param collection 多个对象
     * @return 是否删除成功
     */
    @Override
    public boolean deleteObject(final Collection collection) {
        return redisTemplate.delete(collection) > 0;
    }

    /**
     * 缓存 List 数据。
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的 List 数据
     * @param <T>      List 元素类型
     * @return 缓存的对象数量
     */
    @Override
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的 list 对象。
     *
     * @param key 缓存的键值
     * @param <T> 缓存值类型
     * @return 缓存键值对应的数据
     */
    @Override
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存 Set。
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @param <T>     Set 元素类型
     * @return 缓存数据的对象
     */
    @Override
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        for (T item : dataSet) {
            setOperation.add(item);
        }
        return setOperation;
    }

    /**
     * 获得缓存的 set。
     *
     * @param key 缓存键值
     * @param <T> 缓存值类型
     * @return 缓存数据集合
     */
    @Override
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 获得匹配指定模式的 Redis Key 集合。
     *
     * @param pattern 字符串前缀
     * @return Key 集合
     */
    @Override
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 批量获取缓存的基本对象
     *
     * @param keys 缓存键集合
     * @param <T>  缓存值类型
     * @return 缓存数据集合 (注意：如果某个key不存在，对应位置会返回null)
     */
    @Override
    public <T> List<T> multiGetCacheObject(final Collection<String> keys) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.multiGet(keys);
    }
}



