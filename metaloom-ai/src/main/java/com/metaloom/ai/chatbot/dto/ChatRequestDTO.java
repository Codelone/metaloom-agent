package com.metaloom.ai.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天请求DTO
 * 前端发送聊天请求时使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequestDTO {
    /**
     * 会话ID（若为空则创建新会话）
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户消息内容
     */
    private String message;

    /**
     * 是否流式响应
     */
    @Builder.Default
    private Boolean streaming = true;

    /**
     * 会话配置（可选，用于覆盖默认配置）
     */
    private SessionConfigDTO config;

    /**
     * 会话标题（新建会话时可指定）
     */
    private String sessionTitle;
}
