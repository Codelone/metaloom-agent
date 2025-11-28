package com.metaloom.ai.chatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ChatBot配置属性
 * 对应application.yml中的metaloom.chatbot配置项
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "metaloom.chatbot")
public class ChatBotConfig {

    /**
     * 是否启用ChatBot功能
     */
    private boolean enabled = true;

    /**
     * 会话超时时间（秒）
     */
    private long sessionTimeout = 3600;

    /**
     * 最大会话数（单个用户）
     */
    private int maxSessionsPerUser = 50;

    /**
     * 默认提供者
     */
    private String defaultProvider = "openai";

    /**
     * 默认模型
     */
    private String defaultModel = "deepseek-v3";

    /**
     * 默认温度
     */
    private double defaultTemperature = 0.7;

    /**
     * 默认最大令牌数
     */
    private int defaultMaxTokens = 2000;

    /**
     * 默认上下文窗口大小
     */
    private int defaultContextWindowSize = 10;

    /**
     * 默认记忆模式
     */
    private String defaultMemoryMode = "short_term";

    /**
     * 是否启用流式响应
     */
    private boolean enableStreaming = true;

    /**
     * 是否启用记忆功能
     */
    private boolean enableMemory = true;

    /**
     * 是否启用对话历史持久化
     */
    private boolean enablePersistence = true;

    /**
     * 持久化实现类型：memory(内存)、database(数据库)
     */
    private String persistenceType = "memory";

    /**
     * 系统提示词
     */
    private String systemPrompt;
}
