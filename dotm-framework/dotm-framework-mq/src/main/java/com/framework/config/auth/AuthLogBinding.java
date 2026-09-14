package com.framework.config.auth;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author dotm
 */
@Component
public class AuthLogBinding {

    /**
     * 绑定认证中心日志交换机和队列
     */
    @Bean
    @SuppressWarnings("all")
    public Binding authLoginBinding(TopicExchange topicExchange, Queue topicLoginQueue) {
        return BindingBuilder.bind(topicLoginQueue).to(topicExchange).with(RabbitMQConstants.AUTH_LOG_ROUTING_KEY);
    }
}
