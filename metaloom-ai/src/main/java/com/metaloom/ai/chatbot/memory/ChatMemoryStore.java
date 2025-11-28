package com.metaloom.ai.chatbot.memory;

import com.metaloom.ai.chatbot.model.ChatMessage;
import java.util.List;
import java.util.Optional;

/**
 * 聊天记忆存储接口
 * 定义记忆存储的核心操作
 * 支持多种实现：内存存储、数据库存储、向量数据库等
 */
public interface ChatMemoryStore {

    /**
     * 存储聊天消息
     */
    void saveMessage(String sessionId, ChatMessage message);

    /**
     * 批量存储消息
     */
    void saveMessages(String sessionId, List<ChatMessage> messages);

    /**
     * 获取指定会话的所有消息
     */
    List<ChatMessage> getMessages(String sessionId);

    /**
     * 获取指定会话的最近N条消息
     */
    List<ChatMessage> getRecentMessages(String sessionId, int count);

    /**
     * 获取指定会话的消息数量
     */
    int getMessageCount(String sessionId);

    /**
     * 获取单条消息
     */
    Optional<ChatMessage> getMessage(String messageId);

    /**
     * 清除指定会话的所有消息
     */
    void clearMessages(String sessionId);

    /**
     * 删除单条消息
     */
    void deleteMessage(String messageId);

    /**
     * 更新消息
     */
    void updateMessage(ChatMessage message);

    /**
     * 判断消息是否存在
     */
    boolean messageExists(String messageId);

    /**
     * 保存对话摘要（用于长期记忆）
     */
    void saveSummary(String sessionId, String summary);

    /**
     * 获取对话摘要
     */
    Optional<String> getSummary(String sessionId);

    /**
     * 保存关键信息
     */
    void saveKeyInformation(String sessionId, String key, String value);

    /**
     * 获取关键信息
     */
    Optional<String> getKeyInformation(String sessionId, String key);

    /**
     * 获取会话的所有关键信息
     */
    java.util.Map<String, String> getAllKeyInformation(String sessionId);

    /**
     * 清除会话数据
     */
    void clearSessionData(String sessionId);
}
