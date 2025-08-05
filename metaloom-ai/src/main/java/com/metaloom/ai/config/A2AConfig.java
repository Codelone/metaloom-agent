package com.metaloom.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * A2A智能体配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "metaloom.a2a")
public class A2AConfig {

    /**
     * 是否启用A2A架构
     */
    private boolean enabled = true;

    /**
     * 最大迭代次数
     */
    private int maxIterations = 5;

    /**
     * 超时时间（毫秒）
     */
    private long timeout = 30000;

    /**
     * 可用的智能体列表
     */
    private List<String> availableAgents = List.of("lineage_agent", "metadata_agent");

    /**
     * 智能体配置
     */
    private Map<String, AgentConfig> agents;

    /**
     * LLM配置
     */
    private LLMConfig llm;

    /**
     * 智能体配置
     */
    @Data
    public static class AgentConfig {
        private String name;
        private String description;
        private String endpoint;
        private boolean enabled = true;
        private int timeout = 10000;
    }

    /**
     * LLM配置
     */
    @Data
    public static class LLMConfig {
        private String provider = "openai";
        private String model = "deepseek-v3";
        private double temperature = 0.7;
        private int maxTokens = 2000;
        private String systemPrompt;
    }
} 