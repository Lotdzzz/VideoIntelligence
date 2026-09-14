package com.framework.config.auth;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author dotm
 */
@Component
public class AuthLogQueue {

    /**
     * 认证中心日志记录队列
     */
    @Bean
    public Queue topicLoginQueue() {
        return new Queue(RabbitMQConstants.AUTH_LOG_ROUTING_KEY, true);
    }
}
