package com.framework.config;

import com.framework.constants.AIConstants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * @author dotm
 * AI文本客户端构造
 * 因为ChatClient是引入依赖后就自动存在的 所以不需要在方法中MissingBean了
 */
@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
public class ChatClientConfig {

    /**
     * 菜单管理页面用来生成SQL的AI
     */
    @Bean(name = AIConstants.MCP_SQL)
    public ChatClient getChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
