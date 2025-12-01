package com.metaloom.ai.agent;

import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

/**
 * Agent接口
 * 所有智能体必须实现此接口
 */
public interface ChatAgent {

    /**
     * 获取Agent类型
     */
    AgentType getAgentType();

    /**
     * 获取Agent的系统提示词
     * 
     * @param sessionId 会话ID
     * @return 系统提示词
     */
    String getSystemPrompt(String sessionId);

    /**
     * 处理用户消息（同步）
     * 
     * @param sessionId  会话ID
     * @param userPrompt 用户输入（已包含历史上下文）
     * @param chatClient 聊天客户端
     * @return Agent响应内容
     */
    String process(String sessionId, String userPrompt, ChatClient chatClient);

    /**
     * 处理用户消息（流式）
     * 
     * @param sessionId  会话ID
     * @param userPrompt 用户输入（已包含历史上下文）
     * @param chatClient 聊天客户端
     * @return Agent响应流
     */
    Flux<String> processStream(String sessionId, String userPrompt, ChatClient chatClient);

    /**
     * Agent初始化回调
     * 用于加载资源、初始化配置等
     */
    default void initialize() {
        // 默认空实现
    }
}
