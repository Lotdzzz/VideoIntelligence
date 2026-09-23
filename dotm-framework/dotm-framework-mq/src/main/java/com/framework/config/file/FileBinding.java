package com.framework.config.file;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 文件模块交换机与队列的绑定关系
 */
@Component
public class FileBinding {

    /**
     * 交换机与文件链接队列的绑定
     */
    @Bean
    public Binding fileURLbinding(TopicExchange fileTopicExchange, Queue urlQueue) {
        return BindingBuilder.bind(urlQueue).to(fileTopicExchange).with(RabbitMQConstants.URL_ROUTING_KEY);
    }
}
