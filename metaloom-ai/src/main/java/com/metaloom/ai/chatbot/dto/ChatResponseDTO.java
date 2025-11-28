package com.metaloom.ai.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天响应DTO
 * 返回给前端的聊天结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponseDTO {
    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 助手响应内容
     */
    private String content;

    /**
     * 请求状态：success、error、pending
     */
    @Builder.Default
    private String status = "success";

    /**
     * 错误信息（如有）
     */
    private String errorMessage;

    /**
     * 消息令牌数
     */
    private Integer tokenCount;

    /**
     * 会话消息总数
     */
    private Integer messageCount;

    /**
     * 会话总令牌数
     */
    private Long totalTokens;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;
}
