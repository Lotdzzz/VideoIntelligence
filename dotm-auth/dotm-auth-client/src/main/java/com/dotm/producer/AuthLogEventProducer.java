package com.dotm.producer;

import com.framework.constants.RabbitMQConstants;
import com.dotm.entity.dto.AuthLogDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @author dotm
 * AuthLogEventProducer 认证日志事件生产者
 * 用于在登录和登出方法执行前进行认证逻辑
 * 获取到目标数据后通过mq发往日志服务进行记录
 */
@Component
@RequiredArgsConstructor
public class AuthLogEventProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * send生产方法 针对日志
     */
    public void send(AuthLogDTO data) {
        // 发送消息到 RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConstants.AUTH_LOG_EXCHANGE, RabbitMQConstants.AUTH_LOG_ROUTING_KEY, data);
    }

    /**
     * send生产方法
     */
    public void send(String data) {
        // 发送消息到 RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConstants.AUTH_LOG_EXCHANGE, RabbitMQConstants.AUTH_LOG_ROUTING_KEY, data);
    }
}
