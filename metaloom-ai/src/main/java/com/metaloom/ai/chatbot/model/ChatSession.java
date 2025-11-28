package com.metaloom.ai.chatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天会话模型
 * 代表一个用户与AI之间的完整会话
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {
    /**
     * 会话ID（唯一标识）
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话描述
     */
    private String description;

    /**
     * 会话状态：active(活跃)、archived(归档)、deleted(已删除)
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 会话创建时间
     */
    private LocalDateTime lastAccessAt;

    /**
     * 对话消息列表
     */
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 会话配置（如模型类型、温度等参数）
     */
    private SessionConfig config;

    /**
     * 会话元数据
     */
    private String metadata;

    /**
     * 消息总数
     */
    private Integer messageCount;

    /**
     * 总令牌数
     */
    private Long totalTokens;
}
