package com.framework.config.deadline;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author dotm
 * 这是一个死信绑定的配置类
 */
@Component
public class DeadLineBinding {

    /**
     * 死信交换机与死信队列的绑定
     */
    @Bean
    public Binding deadlineBinding(TopicExchange deadLetterExchange, Queue fileDeadLineQueue) {
        return BindingBuilder.bind(fileDeadLineQueue).to(deadLetterExchange).with(RabbitMQConstants.FILE_DEAD_LETTER_ROUTING_KEY);
    }
}
