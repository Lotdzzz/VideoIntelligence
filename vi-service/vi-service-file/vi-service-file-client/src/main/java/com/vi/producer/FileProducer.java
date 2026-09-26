package com.vi.producer;

import com.framework.constants.RabbitMQConstants;
import com.vi.entity.dto.ViFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @author dotm
 * 将视频实体通过消息队列发给python微服务进行视频分析处理
 */
@Component
@RequiredArgsConstructor
public class FileProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * send生产方法
     *
     * @param data 视频实体
     */
    public void send(ViFileDTO data) {
        // 发送消息到 RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConstants.FILE_EXCHANGE, RabbitMQConstants.URL_ROUTING_KEY, data);
    }

    /**
     * send生产方法
     *
     * @param data 视频实体json字符串
     */
    public void send(String data) {
        // 发送消息到 RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConstants.FILE_EXCHANGE, RabbitMQConstants.URL_ROUTING_KEY, data);
    }
}
