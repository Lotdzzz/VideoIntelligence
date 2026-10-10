package com.framework.config.file;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 文件死信队列
 *
 * @author dotm
 */
@Component
public class FileDeadLineQueue {

    @Bean(name = "fileDeadLineQueue")
    public Queue fileDeadLineQueue() {
        return new Queue(RabbitMQConstants.FILE_DEAD_LETTER_ROUTING_KEY, true);
    }
}
