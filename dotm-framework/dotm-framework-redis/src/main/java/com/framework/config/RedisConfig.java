package com.framework.config;

import com.framework.constants.RedisSerializerConstants;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * @author dotm
 * Redis 公共配置。
 *
 * <p>同时配置两种使用方式：</p>
 * <ul>
 *     <li>RedisTemplate：在业务代码中主动读写 Redis。</li>
 *     <li>RedisCacheManager：支持 @Cacheable、@CachePut、@CacheEvict。</li>
 * </ul>
 */
@EnableCaching
@AutoConfiguration
@ConditionalOnClass(RedisTemplate.class)
public class RedisConfig {

    /**
     * 规定序列化规则
     * 创建 Redis Value 使用的 Jackson 3 JSON 序列化器。
     *
     * <p>启用类型信息后，对象写入 JSON 时会携带实际类型，读取时才能恢复成原来的 DTO，
     * 而不是变成 LinkedHashMap。</p>
     */
    @Bean
    public RedisSerializer<Object> redisValueSerializer() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                // 允许项目自身的 DTO、VO、record 等业务对象。
                .allowIfSubType(RedisSerializerConstants.TRUSTED_PACKAGE_PREFIX)
                // 允许 List、Map、Set 及其常见实现类。
                .allowIfSubType("java.util.")
                // 允许以上受信任类型组成的数组。
                .allowIfSubTypeIsArray()
                .build();

        return GenericJacksonJsonRedisSerializer.builder()
                // 写入类型信息，并通过上面的白名单限制可以恢复的类型。
                .enableDefaultTyping(typeValidator)
                .build();
    }

    /**
     * 业务主动操作 Redis 时使用的模板。
     *
     * <p>例如操作普通 Value、Hash、List、Set 和 ZSet。</p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplateCache")
    public RedisTemplate<String, Object> redisTemplateCache(
            RedisConnectionFactory connectionFactory,
            RedisSerializer<Object> redisValueSerializer) {

        // Key 使用字符串格式，方便直接在 Redis 客户端中查看和检索。
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 连接工厂由 Spring Boot 根据 spring.data.redis 和 Lettuce 连接池配置自动创建。
        template.setConnectionFactory(connectionFactory);

        // 普通 Key 和 Hash Key 都保存为可读字符串。
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 普通 Value 和 Hash Value 都保存为带类型信息的 JSON。
        template.setValueSerializer(redisValueSerializer);
        template.setHashValueSerializer(redisValueSerializer);

        // 没有单独指定序列化器的 Redis 数据类型使用该默认序列化器。
        template.setDefaultSerializer(redisValueSerializer);

        // 完成 RedisTemplate 的属性检查和内部组件初始化。
        template.afterPropertiesSet();
        return template;
    }
}

