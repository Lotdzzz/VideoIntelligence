package com.framework.config.deadline;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 这是一个死信交换机的配置类，用于定义死信交换机的相关属性和行为。
 *
 * @author dotm
 */
@Component
public class DeadLineExchange {

    /**
     * 死信交换机实例
     */
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(RabbitMQConstants.DEAD_LETTER_EXCHANGE);
    }
}
