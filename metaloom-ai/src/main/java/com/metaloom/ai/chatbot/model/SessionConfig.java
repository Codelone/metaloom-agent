package com.metaloom.ai.chatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话配置
 * 存储与特定会话相关的AI配置参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionConfig {
    /**
     * LLM提供者：openai、ollama等
     */
    @Builder.Default
    private String provider = "openai";

    /**
     * 模型名称
     */
    @Builder.Default
    private String model = "deepseek-v3";

    /**
     * 温度参数（0-1），控制输出的随机性
     */
    @Builder.Default
    private Double temperature = 0.7;

    /**
     * 最大生成令牌数
     */
    @Builder.Default
    private Integer maxTokens = 2000;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 记忆模式：none(无)、short_term(短期)、long_term(长期)
     */
    @Builder.Default
    private String memoryMode = "short_term";

    /**
     * 是否启用流式响应
     */
    @Builder.Default
    private Boolean enableStreaming = true;

    /**
     * 上下文窗口大小（保留的历史消息数）
     */
    @Builder.Default
    private Integer contextWindowSize = 10;

    /**
     * 智能体类型：chatbot(通用聊天)、nl2sql(自然语言转SQL)
     */
    @Builder.Default
    private String agentType = "chatbot";
}
