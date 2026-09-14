package com.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author dotm
 */
@AutoConfiguration
@Slf4j
@ConditionalOnMissingBean(RabbitTemplate.class)
public class RabbitMQProducerTemplate {

    @Bean
    @SuppressWarnings("all")
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        // 发布确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息确认成功: {}", correlationData);
            } else {
                log.error("消息确认失败: {}, 原因: {}", correlationData, cause);
                // 可进行重发或记录日志
            }
        });

        // 发布返回回调（消息无法路由到队列时触发）
        template.setReturnsCallback(returnedMessage -> {
            Message message = returnedMessage.getMessage();
            int replyCode = returnedMessage.getReplyCode();
            String replyText = returnedMessage.getReplyText();
            String exchange = returnedMessage.getExchange();
            String routingKey = returnedMessage.getRoutingKey();
            log.error("消息被退回: {}, 路由键: {}, 回复码: {}, 原因: {}", message, routingKey, replyCode, replyText);
            // 处理无法路由的消息
        });

        // 设置消息转换器 对象-对象类型
        template.setMessageConverter(messageConverter);

        return template;
    }
}
