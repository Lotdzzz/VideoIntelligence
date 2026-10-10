package com.vi.consumer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.framework.constants.RabbitMQConstants;
import com.rabbitmq.client.Channel;
import com.vi.entity.model.AnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 分析结果的消费者
 *
 * @author dotm
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AnalysisResultConsumer {

    public static final List<AnalysisResult> analysisResults = new ArrayList<>();

    @RabbitListener(queues = RabbitMQConstants.ANALYSIS_RESULT_ROUTING_KEY)
    public void receiveAnalysisResult(@Payload AnalysisResult analysisResult, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        analysisResults.add(analysisResult);
        log.info("Received analysis result: {}", analysisResult);

        // 处理成功，确认消息
//        try {
//            channel.basicAck(deliveryTag, false);
//        } catch (Exception e) {
//            log.error("确认消息失败: {}", e.getMessage(), e);
//        }
    }
}
