package com.dotm.controller;

import com.framework.constants.AIConstants;
import com.framework.entity.vo.AiPromptVO;
import com.framework.model.Result;
import com.framework.service.AiPromptService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dotm
 * MCP客户端接口
 * 让AI大模型操作数据库
 */
@RequestMapping("/auth/mcp")
@RestController
@RequiredArgsConstructor
public class MCPController {

    @Resource(name = AIConstants.MCP_SQL)
    private final ChatClient chatClient;

    private final AiPromptService aiPromptService;

    /**
     * AI接口
     */
    @PostMapping( "/chat")
    @PreAuthorize("@ss.hasPermission('system:menu:list')")
    public Result<Object> chat(@RequestParam(value = "prompt") String message) {
        String prompt = null;

        //获取提示词
        AiPromptVO entity = aiPromptService.getAiPromptByKey(AIConstants.MENU_MANAGE_SQL_PROMPT);

        if (entity == null) {
            prompt = AIConstants.PROMPT_NOT_FOUND;
        } else {
            prompt = entity.getContent();
        }

        String content = chatClient.prompt()
                .system(prompt)
                .user(message)
                .call()
                .content();

        return Result.success(content);
    }
}
