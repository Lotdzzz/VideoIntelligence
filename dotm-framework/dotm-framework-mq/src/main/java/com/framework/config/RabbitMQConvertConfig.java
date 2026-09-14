package com.framework.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author dotm
 * RabbitMQConvertConfig 配置类
 * 用于配置 RabbitMQ 消息转换器
 */
@AutoConfiguration
@ConditionalOnMissingBean(MessageConverter.class)
public class RabbitMQConvertConfig {

    /**
     * 消息转换器
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
