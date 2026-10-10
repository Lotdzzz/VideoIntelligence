package com.framework.config.file;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 这是一个转运用户视频链接的队列
 *
 * @author dotm
 */
@Component
public class FileURLQueue {

    /**
     * 链接的队列
     */
    @Bean(name = "urlQueue")
    public Queue urlQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.URL_ROUTING_KEY)
                .deadLetterExchange(
                        RabbitMQConstants.DEAD_LETTER_EXCHANGE
                )
                .deadLetterRoutingKey(
                        RabbitMQConstants.FILE_DEAD_LETTER_ROUTING_KEY
                )
                .build();
    }
}
