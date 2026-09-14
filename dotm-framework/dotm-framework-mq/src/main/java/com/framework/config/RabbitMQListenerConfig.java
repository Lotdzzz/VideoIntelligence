package com.framework.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author dotm
 * RabbitMQListenerConfig 配置类
 */
@AutoConfiguration
@ConditionalOnClass({RabbitTemplate.class})
public class RabbitMQListenerConfig {

    // 注入 Spring Boot 自动配置好的容器工厂属性
    @Bean
    @SuppressWarnings("all")
    // 检测如果没有自定义的 rabbitListenerContainerFactory Bean，则使用此配置
    @ConditionalOnMissingBean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {

        // 1. 让 Spring Boot 用 YAML 配置帮生成一个基础工厂
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        // 3. 设置致命异常策略（无法用 YAML 表达）
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(
                t -> t instanceof IllegalArgumentException  // 参数错误直接丢弃，不重试
        ));

        return factory;
    }
}
