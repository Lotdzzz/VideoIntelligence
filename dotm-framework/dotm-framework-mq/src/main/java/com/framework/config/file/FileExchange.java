package com.framework.config.file;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 文件模块的交换机
 */
@Component
public class FileExchange {

    /**
     * 文件模块交换机实例
     */
    @Bean
    public TopicExchange fileTopicExchange() {
        return new TopicExchange(RabbitMQConstants.FILE_EXCHANGE);
    }
}
