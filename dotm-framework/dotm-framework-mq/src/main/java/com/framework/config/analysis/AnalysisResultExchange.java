package com.framework.config.analysis;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 分析结果交换机
 *
 * @author dotm
 */
@Component
public class AnalysisResultExchange {

    /**
     * 分析结果交换机
     */
    @Bean
    public TopicExchange analysisResultExchange() {
        return new TopicExchange(RabbitMQConstants.ANALYSIS_RESULT_EXCHANGE);
    }
}
