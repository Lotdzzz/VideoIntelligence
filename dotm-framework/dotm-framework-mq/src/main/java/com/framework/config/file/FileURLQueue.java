package com.framework.config.file;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.Queue;
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
    @Bean
    public Queue urlQueue() {
        return new Queue(RabbitMQConstants.URL_ROUTING_KEY, true);
    }
}
