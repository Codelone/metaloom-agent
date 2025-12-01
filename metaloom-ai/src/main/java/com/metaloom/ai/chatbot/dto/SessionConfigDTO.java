package com.metaloom.ai.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话配置DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionConfigDTO {
    /**
     * LLM提供者
     */
    private String provider;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 最大令牌数
     */
    private Integer maxTokens;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 记忆模式
     */
    private String memoryMode;

    /**
     * 是否启用流式响应
     */
    private Boolean enableStreaming;

    /**
     * 上下文窗口大小
     */
    private Integer contextWindowSize;

    /**
     * 智能体类型：chatbot、nl2sql等
     */
    private String agentType;
}
