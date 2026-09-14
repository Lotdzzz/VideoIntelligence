package com.framework.config.auth;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author dotm
 */
@Component
public class AuthLogExchange {

    /**
     * topic交换机
     */
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(RabbitMQConstants.AUTH_LOG_EXCHANGE);
    }
}
