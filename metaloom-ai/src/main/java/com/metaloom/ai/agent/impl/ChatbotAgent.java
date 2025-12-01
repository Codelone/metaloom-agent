package com.metaloom.ai.agent.impl;

import com.metaloom.ai.agent.AgentType;
import com.metaloom.ai.agent.ChatAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 通用聊天机器人Agent（默认）
 */
@Slf4j
@Component
public class ChatbotAgent implements ChatAgent {

    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个有帮助的AI助手。请根据用户的问题提供准确、清晰的回答。";

    @Override
    public AgentType getAgentType() {
        return AgentType.CHATBOT;
    }

    @Override
    public String getSystemPrompt(String sessionId) {
        return DEFAULT_SYSTEM_PROMPT;
    }

    @Override
    public String process(String sessionId, String userPrompt, ChatClient chatClient) {
        log.debug("ChatbotAgent processing: sessionId={}", sessionId);

        return chatClient.prompt()
                .system(getSystemPrompt(sessionId))
                .user(userPrompt)
                .call()
                .content();
    }

    @Override
    public Flux<String> processStream(String sessionId, String userPrompt, ChatClient chatClient) {
        log.debug("ChatbotAgent streaming: sessionId={}", sessionId);

        return chatClient.prompt()
                .system(getSystemPrompt(sessionId))
                .user(userPrompt)
                .stream()
                .content();
    }
}
