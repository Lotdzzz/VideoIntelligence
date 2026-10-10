package com.framework.config.analysis;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 分析结果队列
 *
 * @author dotm
 */
@Component
public class AnalysisResultQueue {

    /**
     * 分析结果队列
     */
    @Bean
    public Queue topicAnalysisResultQueue() {
        return new Queue(RabbitMQConstants.ANALYSIS_RESULT_ROUTING_KEY, true);
    }
}
