package com.metaloom.ai.agent;

/**
 * Agent类型枚举
 * 定义系统支持的所有智能体类型
 */
public enum AgentType {
    /**
     * 通用聊天机器人
     */
    CHATBOT("chatbot", "通用聊天机器人"),

    /**
     * NL2SQL智能体
     */
    NL2SQL("nl2sql", "自然语言转SQL");

    private final String code;
    private final String description;

    AgentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取AgentType
     */
    public static AgentType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return CHATBOT; // 默认返回通用聊天机器人
        }

        for (AgentType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }

        return CHATBOT; // 未找到则返回默认
    }
}
