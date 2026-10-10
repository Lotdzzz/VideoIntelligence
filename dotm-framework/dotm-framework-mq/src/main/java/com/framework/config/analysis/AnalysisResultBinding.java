package com.framework.config.analysis;

import com.framework.constants.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 分析结果绑定
 *
 * @author dotm
 */
@Component
public class AnalysisResultBinding {

    /**
     * 分析结果绑定
     */
    @Bean
    @SuppressWarnings("all")
    public Binding analysisResultBinding(TopicExchange analysisResultExchange, Queue topicAnalysisResultQueue) {
        return BindingBuilder
                .bind(topicAnalysisResultQueue)
                .to(analysisResultExchange)
                .with(RabbitMQConstants.ANALYSIS_RESULT_ROUTING_KEY);
    }
}
