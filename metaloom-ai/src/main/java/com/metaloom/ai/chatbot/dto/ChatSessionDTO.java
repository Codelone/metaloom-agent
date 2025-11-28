package com.metaloom.ai.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话摘要DTO
 * 用于列表展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionDTO {
    /**
     * 会话ID
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
     * 会话状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessAt;

    /**
     * 消息总数
     */
    private Integer messageCount;

    /**
     * 总令牌数
     */
    private Long totalTokens;
}
