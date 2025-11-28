package com.metaloom.ai.chatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话上下文
 * 用于保留多轮对话的上下文信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationContext {
    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 当前问题
     */
    private String currentQuestion;

    /**
     * 对话历史摘要（用于长期记忆）
     */
    private String conversationSummary;

    /**
     * 关键信息存储（用于记忆增强）
     */
    private String keyInformation;

    /**
     * 用户意图
     */
    private String userIntent;

    /**
     * 实体识别结果
     */
    private String entities;

    /**
     * 状态机状态
     */
    private String state;
}
