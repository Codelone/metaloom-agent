package com.metaloom.ai.chatbot.service;

import com.metaloom.ai.chatbot.constant.ChatBotConstants;
import com.metaloom.ai.chatbot.model.ChatSession;
import com.metaloom.ai.chatbot.model.SessionConfig;
import com.metaloom.ai.chatbot.config.ChatBotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 聊天会话管理服务
 * 负责会话的生命周期管理：创建、获取、更新、删除
 */
@Slf4j
@Service
public class ChatSessionService {

    private final ChatBotConfig chatBotConfig;

    /**
     * 会话存储：sessionId -> ChatSession
     */
    private final ConcurrentHashMap<String, ChatSession> sessions = new ConcurrentHashMap<>();

    /**
     * 用户会话索引：userId -> List<sessionId>
     */
    private final ConcurrentHashMap<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    @Autowired
    public ChatSessionService(ChatBotConfig chatBotConfig) {
        this.chatBotConfig = chatBotConfig;
    }

    /**
     * 创建新会话
     */
    public ChatSession createSession(String userId, String title, SessionConfig config) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.INVALID_USER_ID);
        }

        String sessionId = generateSessionId();
        LocalDateTime now = LocalDateTime.now();

        SessionConfig sessionConfig = config != null ? config : buildDefaultConfig();

        ChatSession session = ChatSession.builder()
            .sessionId(sessionId)
            .userId(userId)
            .title(title != null ? title : "新会话_" + now.format(java.time.format.DateTimeFormatter.ofPattern("HHmmss")))
            .status(ChatBotConstants.SessionStatus.ACTIVE)
            .createdAt(now)
            .updatedAt(now)
            .lastAccessAt(now)
            .config(sessionConfig)
            .messages(new ArrayList<>())
            .messageCount(0)
            .totalTokens(0L)
            .build();

        // 检查用户会话数量限制
        Set<String> userSessionIds = userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        if (userSessionIds.size() >= chatBotConfig.getMaxSessionsPerUser()) {
            log.warn("用户会话数已达到上限: userId={}, maxSessions={}", userId, chatBotConfig.getMaxSessionsPerUser());
            // 可选：删除最旧的会话或抛出异常
        }

        sessions.put(sessionId, session);
        userSessionIds.add(sessionId);

        log.info("会话已创建: sessionId={}, userId={}", sessionId, userId);
        return session;
    }

    /**
     * 获取会话
     */
    public Optional<ChatSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * 获取用户的所有活跃会话
     */
    public List<ChatSession> getUserActiveSessions(String userId) {
        Set<String> sessionIds = userSessions.getOrDefault(userId, Collections.emptySet());
        return sessionIds.stream()
            .map(sessionId -> sessions.get(sessionId))
            .filter(Objects::nonNull)
            .filter(session -> ChatBotConstants.SessionStatus.ACTIVE.equals(session.getStatus()))
            .sorted(Comparator.comparing(ChatSession::getLastAccessAt).reversed())
            .collect(Collectors.toList());
    }

    /**
     * 获取用户的所有会话
     */
    public List<ChatSession> getUserAllSessions(String userId) {
        Set<String> sessionIds = userSessions.getOrDefault(userId, Collections.emptySet());
        return sessionIds.stream()
            .map(sessionId -> sessions.get(sessionId))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(ChatSession::getLastAccessAt).reversed())
            .collect(Collectors.toList());
    }

    /**
     * 更新会话最后访问时间
     */
    public void updateSessionAccessTime(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        if (session != null) {
            session.setLastAccessAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * 更新会话配置
     */
    public void updateSessionConfig(String sessionId, SessionConfig config) {
        ChatSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.SESSION_NOT_FOUND);
        }

        session.setConfig(config);
        session.setUpdatedAt(LocalDateTime.now());
        log.info("会话配置已更新: sessionId={}", sessionId);
    }

    /**
     * 更新会话消息计数和令牌数
     */
    public void updateSessionStats(String sessionId, int messageCount, long tokenCount) {
        ChatSession session = sessions.get(sessionId);
        if (session != null) {
            session.setMessageCount(messageCount);
            session.setTotalTokens(tokenCount);
            session.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * 归档会话
     */
    public void archiveSession(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        if (session != null) {
            session.setStatus(ChatBotConstants.SessionStatus.ARCHIVED);
            session.setUpdatedAt(LocalDateTime.now());
            log.info("会话已归档: sessionId={}", sessionId);
        }
    }

    /**
     * 恢复会话
     */
    public void restoreSession(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        if (session != null) {
            session.setStatus(ChatBotConstants.SessionStatus.ACTIVE);
            session.setUpdatedAt(LocalDateTime.now());
            session.setLastAccessAt(LocalDateTime.now());
            log.info("会话已恢复: sessionId={}", sessionId);
        }
    }

    /**
     * 删除会话
     */
    public void deleteSession(String sessionId) {
        ChatSession session = sessions.remove(sessionId);
        if (session != null) {
            Set<String> userSessionIds = userSessions.getOrDefault(session.getUserId(), Collections.emptySet());
            userSessionIds.remove(sessionId);
            log.info("会话已删除: sessionId={}, userId={}", sessionId, session.getUserId());
        }
    }

    /**
     * 会话是否存在
     */
    public boolean sessionExists(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    /**
     * 清除用户的所有会话
     */
    public void clearUserSessions(String userId) {
        Set<String> sessionIds = userSessions.remove(userId);
        if (sessionIds != null) {
            sessionIds.forEach(sessions::remove);
            log.info("用户会话已清除: userId={}, count={}", userId, sessionIds.size());
        }
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().substring(0, 12);
    }

    /**
     * 构建默认配置
     */
    private SessionConfig buildDefaultConfig() {
        return SessionConfig.builder()
            .provider(chatBotConfig.getDefaultProvider())
            .model(chatBotConfig.getDefaultModel())
            .temperature(chatBotConfig.getDefaultTemperature())
            .maxTokens(chatBotConfig.getDefaultMaxTokens())
            .memoryMode(chatBotConfig.getDefaultMemoryMode())
            .enableStreaming(chatBotConfig.isEnableStreaming())
            .contextWindowSize(chatBotConfig.getDefaultContextWindowSize())
            .systemPrompt(chatBotConfig.getSystemPrompt())
            .build();
    }

    /**
     * 获取所有活跃会话数
     */
    public int getActiveSessionCount() {
        return (int) sessions.values().stream()
            .filter(session -> ChatBotConstants.SessionStatus.ACTIVE.equals(session.getStatus()))
            .count();
    }

    /**
     * 获取总会话数
     */
    public int getTotalSessionCount() {
        return sessions.size();
    }
}

