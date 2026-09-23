package com.framework.constants;

/**
 * @author dotm
 * 记录 RabbitMQ 相关常量
 */
public class RabbitMQConstants {

    /**
     * 认证中心模块
     */
    public static final String AUTH_LOG_ROUTING_KEY = "auth.log";

    /**
     * 交换机名称
     */
    public static final String AUTH_LOG_EXCHANGE = "auth.log.exchange";

    /**
     * 文件模块的视频链接key
     */
    public static final String URL_ROUTING_KEY = "file.url";

    /**
     * 文件模块交换机
     */
    public static final String FILE_EXCHANGE = "file.exchange";
}
