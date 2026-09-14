package com.dotm.consumer;

import com.rabbitmq.client.Channel;
import com.framework.constants.RabbitMQConstants;
import com.dotm.entity.dto.AuthLogDTO;
import com.dotm.entity.model.SysAuthLog;
import com.dotm.service.SysAuthLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author dotm
 * AuthLogResultConsumer 认证日志结果消费者
 * 用于接收日志服务返回的认证日志结果
 * 进行后续处理，如记录日志、发送通知等
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthLogResultConsumer {

    private final SysAuthLogService sysAuthLogService;

    /**
     * 监听数据
     */
    @RabbitListener(queues = RabbitMQConstants.AUTH_LOG_ROUTING_KEY)
    public void receive(@Payload AuthLogDTO authLogDTO, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        SysAuthLog sysAuthLog = new SysAuthLog();
        BeanUtils.copyProperties(authLogDTO, sysAuthLog);
        sysAuthLog.setLoginTime(LocalDateTime.now());
        //调用 Service 层处理认证日志结果
        boolean save = sysAuthLogService.save(sysAuthLog);

        if (save) {
            // 处理成功，确认消息
            try {
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                log.error("确认消息失败: {}", e.getMessage(), e);
            }
        } else {
            // 处理失败，拒绝消息并重新入队
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (Exception e) {
                log.error("拒绝消息失败: {}", e.getMessage(), e);
            }
        }
    }

}
