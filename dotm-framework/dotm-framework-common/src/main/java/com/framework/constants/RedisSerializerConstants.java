package com.framework.constants;

/**
 * @author dotm
 */
public class RedisSerializerConstants {

    /*
     * 只允许反序列化指定包下的业务类型，避免 Redis 中被写入任意类名后触发不安全反序列化。
     * 如果存入 Redis 的 DTO、VO、record 不在 com 包下，需要修改或补充允许的包前缀。
     */
    public static final String TRUSTED_PACKAGE_PREFIX = "com";

    public static final String CACHE_VALID_TIME = "${spring.cache.redis.time-to-live:30m}";
}
