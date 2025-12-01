package com.metaloom.ai.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent工厂类
 * 负责创建和管理各种智能体实例
 */
@Slf4j
@Component
public class AgentFactory {

    private final ApplicationContext applicationContext;
    private final Map<AgentType, ChatAgent> agentRegistry = new HashMap<>();

    @Autowired
    public AgentFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化时注册所有Agent
     */
    @PostConstruct
    public void initialize() {
        log.info("初始化AgentFactory，注册所有智能体...");

        // 扫描所有ChatAgent实现并注册
        Map<String, ChatAgent> agents = applicationContext.getBeansOfType(ChatAgent.class);

        for (ChatAgent agent : agents.values()) {
            registerAgent(agent);
            agent.initialize(); // 调用Agent的初始化方法
        }

        log.info("AgentFactory初始化完成，共注册 {} 个智能体", agentRegistry.size());
    }

    /**
     * 注册Agent
     */
    private void registerAgent(ChatAgent agent) {
        AgentType type = agent.getAgentType();
        agentRegistry.put(type, agent);
        log.info("注册智能体: {} - {}", type.getCode(), type.getDescription());
    }

    /**
     * 根据类型获取Agent
     */
    public ChatAgent getAgent(AgentType agentType) {
        ChatAgent agent = agentRegistry.get(agentType);

        if (agent == null) {
            log.warn("未找到Agent: {}, 使用默认ChatbotAgent", agentType);
            agent = agentRegistry.get(AgentType.CHATBOT);
        }

        if (agent == null) {
            throw new IllegalStateException("系统错误: 未找到默认ChatbotAgent");
        }

        return agent;
    }

    /**
     * 根据code获取Agent
     */
    public ChatAgent getAgent(String agentCode) {
        AgentType type = AgentType.fromCode(agentCode);
        return getAgent(type);
    }

    /**
     * 获取所有可用Agent类型
     */
    public java.util.List<java.util.Map<String, String>> getAvailableAgentTypes() {
        java.util.List<java.util.Map<String, String>> types = new java.util.ArrayList<>();
        for (AgentType type : AgentType.values()) {
            java.util.Map<String, String> map = new java.util.HashMap<>();
            map.put("code", type.getCode());
            map.put("description", type.getDescription());
            types.add(map);
        }
        return types;
    }
}
