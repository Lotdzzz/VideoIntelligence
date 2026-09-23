package com.vi.producer;

import com.framework.constants.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @author dotm
 * 视频链接生产者
 * 把用户上传的视频链接发送到指定的队列
 */
@Component
@RequiredArgsConstructor
public class FileURLProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * send生产方法
     */
    public void send(String data) {
        // 发送消息到 RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConstants.FILE_EXCHANGE, RabbitMQConstants.URL_ROUTING_KEY, data);
    }
}
