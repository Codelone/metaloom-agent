package com.metaloom.ai.chatbot.memory;

import com.metaloom.ai.chatbot.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存聊天记忆存储实现
 * 使用HashMap存储消息，适合开发测试环境
 * 注意：此实现在服务重启时会丢失数据，生产环境建议使用数据库或Redis
 */
@Slf4j
@Component
public class InMemoryChatMemoryStore implements ChatMemoryStore {

    /**
     * 消息存储：sessionId -> List<ChatMessage>
     */
    private final ConcurrentHashMap<String, List<ChatMessage>> sessionMessages = new ConcurrentHashMap<>();

    /**
     * 对话摘要存储：sessionId -> summary
     */
    private final ConcurrentHashMap<String, String> summaries = new ConcurrentHashMap<>();

    /**
     * 关键信息存储：sessionId -> (key -> value)
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> keyInformation = new ConcurrentHashMap<>();

    /**
     * 消息查找索引：messageId -> sessionId
     */
    private final ConcurrentHashMap<String, String> messageIndex = new ConcurrentHashMap<>();

    @Override
    public void saveMessage(String sessionId, ChatMessage message) {
        if (sessionId == null || message == null) {
            throw new IllegalArgumentException("sessionId和message不能为空");
        }

        List<ChatMessage> messages = sessionMessages.computeIfAbsent(sessionId, k -> new ArrayList<>());
        messages.add(message);
        messageIndex.put(message.getMessageId(), sessionId);

        log.debug("消息已保存: sessionId={}, messageId={}, role={}", 
            sessionId, message.getMessageId(), message.getRole());
    }

    @Override
    public void saveMessages(String sessionId, List<ChatMessage> messages) {
        if (sessionId == null || messages == null) {
            throw new IllegalArgumentException("sessionId和messages不能为空");
        }

        List<ChatMessage> sessionMessageList = sessionMessages.computeIfAbsent(sessionId, k -> new ArrayList<>());
        for (ChatMessage message : messages) {
            sessionMessageList.add(message);
            messageIndex.put(message.getMessageId(), sessionId);
        }

        log.debug("批量保存消息: sessionId={}, count={}", sessionId, messages.size());
    }

    @Override
    public List<ChatMessage> getMessages(String sessionId) {
        List<ChatMessage> messages = sessionMessages.getOrDefault(sessionId, Collections.emptyList());
        return new ArrayList<>(messages);
    }

    @Override
    public List<ChatMessage> getRecentMessages(String sessionId, int count) {
        List<ChatMessage> allMessages = sessionMessages.getOrDefault(sessionId, Collections.emptyList());
        int size = allMessages.size();
        int start = Math.max(0, size - count);
        
        return new ArrayList<>(allMessages.subList(start, size));
    }

    @Override
    public int getMessageCount(String sessionId) {
        List<ChatMessage> messages = sessionMessages.getOrDefault(sessionId, Collections.emptyList());
        return messages.size();
    }

    @Override
    public Optional<ChatMessage> getMessage(String messageId) {
        String sessionId = messageIndex.get(messageId);
        if (sessionId == null) {
            return Optional.empty();
        }

        List<ChatMessage> messages = sessionMessages.getOrDefault(sessionId, Collections.emptyList());
        return messages.stream()
            .filter(msg -> msg.getMessageId().equals(messageId))
            .findFirst();
    }

    @Override
    public void clearMessages(String sessionId) {
        List<ChatMessage> messages = sessionMessages.getOrDefault(sessionId, Collections.emptyList());
        messages.forEach(msg -> messageIndex.remove(msg.getMessageId()));
        sessionMessages.remove(sessionId);

        log.info("会话消息已清除: sessionId={}", sessionId);
    }

    @Override
    public void deleteMessage(String messageId) {
        String sessionId = messageIndex.get(messageId);
        if (sessionId == null) {
            return;
        }

        List<ChatMessage> messages = sessionMessages.getOrDefault(sessionId, Collections.emptyList());
        messages.removeIf(msg -> msg.getMessageId().equals(messageId));
        messageIndex.remove(messageId);

        log.debug("消息已删除: messageId={}, sessionId={}", messageId, sessionId);
    }

    @Override
    public void updateMessage(ChatMessage message) {
        String sessionId = messageIndex.get(message.getMessageId());
        if (sessionId == null) {
            throw new IllegalArgumentException("消息不存在: messageId=" + message.getMessageId());
        }

        List<ChatMessage> messages = sessionMessages.getOrDefault(sessionId, Collections.emptyList());
        messages.replaceAll(msg -> msg.getMessageId().equals(message.getMessageId()) ? message : msg);

        log.debug("消息已更新: messageId={}", message.getMessageId());
    }

    @Override
    public boolean messageExists(String messageId) {
        return messageIndex.containsKey(messageId);
    }

    @Override
    public void saveSummary(String sessionId, String summary) {
        if (sessionId == null || summary == null) {
            throw new IllegalArgumentException("sessionId和summary不能为空");
        }

        summaries.put(sessionId, summary);
        log.debug("摘要已保存: sessionId={}", sessionId);
    }

    @Override
    public Optional<String> getSummary(String sessionId) {
        return Optional.ofNullable(summaries.get(sessionId));
    }

    @Override
    public void saveKeyInformation(String sessionId, String key, String value) {
        if (sessionId == null || key == null || value == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        ConcurrentHashMap<String, String> sessionKeyInfo = keyInformation.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>());
        sessionKeyInfo.put(key, value);

        log.debug("关键信息已保存: sessionId={}, key={}", sessionId, key);
    }

    @Override
    public Optional<String> getKeyInformation(String sessionId, String key) {
        ConcurrentHashMap<String, String> sessionKeyInfo = keyInformation.get(sessionId);
        if (sessionKeyInfo == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessionKeyInfo.get(key));
    }

    @Override
    public Map<String, String> getAllKeyInformation(String sessionId) {
        ConcurrentHashMap<String, String> sessionKeyInfo = keyInformation.getOrDefault(sessionId, new ConcurrentHashMap<>());
        return new HashMap<>(sessionKeyInfo);
    }

    @Override
    public void clearSessionData(String sessionId) {
        List<ChatMessage> messages = sessionMessages.getOrDefault(sessionId, Collections.emptyList());
        messages.forEach(msg -> messageIndex.remove(msg.getMessageId()));
        
        sessionMessages.remove(sessionId);
        summaries.remove(sessionId);
        keyInformation.remove(sessionId);

        log.info("会话数据已清除: sessionId={}", sessionId);
    }
}

