package com.metaloom.ai.chatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息模型
 * 记录单条对话消息的内容、角色、时间戳等信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    /**
     * 消息ID（唯一标识）
     */
    private String messageId;

    /**
     * 消息角色：user(用户)、assistant(助手)、system(系统)
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息创建时间
     */
    private LocalDateTime timestamp;

    /**
     * 消息元数据（扩展信息）
     */
    private String metadata;

    /**
     * 令牌数量（用于统计成本）
     */
    private Integer tokenCount;
}
