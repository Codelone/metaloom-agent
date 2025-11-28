package com.metaloom.ai.chatbot.constant;

/**
 * ChatBot常量定义
 */
public class ChatBotConstants {

    /**
     * 会话状态常量
     */
    public static class SessionStatus {
        public static final String ACTIVE = "active";
        public static final String ARCHIVED = "archived";
        public static final String DELETED = "deleted";
    }

    /**
     * 消息角色常量
     */
    public static class MessageRole {
        public static final String USER = "user";
        public static final String ASSISTANT = "assistant";
        public static final String SYSTEM = "system";
    }

    /**
     * 记忆模式常量
     */
    public static class MemoryMode {
        public static final String NONE = "none";
        public static final String SHORT_TERM = "short_term";
        public static final String LONG_TERM = "long_term";
    }

    /**
     * API响应状态
     */
    public static class ResponseStatus {
        public static final String SUCCESS = "success";
        public static final String ERROR = "error";
        public static final String PENDING = "pending";
    }

    /**
     * 默认配置值
     */
    public static class Defaults {
        public static final String DEFAULT_MODEL = "deepseek-v3";
        public static final String DEFAULT_PROVIDER = "openai";
        public static final double DEFAULT_TEMPERATURE = 0.7;
        public static final int DEFAULT_MAX_TOKENS = 2000;
        public static final int DEFAULT_CONTEXT_WINDOW = 10;
        public static final String DEFAULT_MEMORY_MODE = MemoryMode.SHORT_TERM;
        public static final boolean DEFAULT_STREAMING = true;
    }

    /**
     * 错误消息
     */
    public static class ErrorMessages {
        public static final String SESSION_NOT_FOUND = "会话不存在";
        public static final String INVALID_SESSION_ID = "无效的会话ID";
        public static final String INVALID_USER_ID = "无效的用户ID";
        public static final String INVALID_MESSAGE = "消息内容不能为空";
        public static final String LLM_ERROR = "LLM调用失败";
        public static final String SESSION_EXPIRED = "会话已过期";
    }
}
