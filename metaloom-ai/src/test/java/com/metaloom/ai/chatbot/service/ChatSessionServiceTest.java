package com.metaloom.ai.chatbot.service;

import com.metaloom.ai.chatbot.config.ChatBotConfig;
import com.metaloom.ai.chatbot.constant.ChatBotConstants;
import com.metaloom.ai.chatbot.model.ChatMessage;
import com.metaloom.ai.chatbot.model.ChatSession;
import com.metaloom.ai.chatbot.model.SessionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 聊天会话管理服务单元测试
 */
@DisplayName("ChatSessionService 单元测试")
class ChatSessionServiceTest {

    private ChatSessionService sessionService;
    private ChatBotConfig chatBotConfig;

    @BeforeEach
    void setUp() {
        chatBotConfig = new ChatBotConfig();
        sessionService = new ChatSessionService(chatBotConfig);
    }

    @Test
    @DisplayName("应该成功创建新会话")
    void testCreateSession() {
        // Arrange
        String userId = "user_123";
        String title = "测试会话";

        // Act
        ChatSession session = sessionService.createSession(userId, title, null);

        // Assert
        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertEquals(userId, session.getUserId());
        assertEquals(title, session.getTitle());
        assertEquals(ChatBotConstants.SessionStatus.ACTIVE, session.getStatus());
        assertNotNull(session.getCreatedAt());
    }

    @Test
    @DisplayName("创建会话时用户ID为空应抛出异常")
    void testCreateSessionWithEmptyUserId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            sessionService.createSession("", "测试", null);
        });
    }

    @Test
    @DisplayName("应该成功获取会话")
    void testGetSession() {
        // Arrange
        ChatSession createdSession = sessionService.createSession("user_123", "测试", null);

        // Act
        var retrievedSession = sessionService.getSession(createdSession.getSessionId());

        // Assert
        assertTrue(retrievedSession.isPresent());
        assertEquals(createdSession.getSessionId(), retrievedSession.get().getSessionId());
    }

    @Test
    @DisplayName("获取不存在的会话应返回空Optional")
    void testGetNonExistentSession() {
        // Act
        var session = sessionService.getSession("non_existent_id");

        // Assert
        assertTrue(session.isEmpty());
    }

    @Test
    @DisplayName("应该成功获取用户的所有活跃会话")
    void testGetUserActiveSessions() {
        // Arrange
        String userId = "user_123";
        sessionService.createSession(userId, "会话1", null);
        sessionService.createSession(userId, "会话2", null);
        sessionService.createSession(userId, "会话3", null);

        // Act
        var sessions = sessionService.getUserActiveSessions(userId);

        // Assert
        assertEquals(3, sessions.size());
        assertTrue(sessions.stream().allMatch(s -> ChatBotConstants.SessionStatus.ACTIVE.equals(s.getStatus())));
    }

    @Test
    @DisplayName("应该成功更新会话访问时间")
    void testUpdateSessionAccessTime() {
        // Arrange
        ChatSession session = sessionService.createSession("user_123", "测试", null);
        var createdAt = session.getLastAccessAt();

        // Act
        sessionService.updateSessionAccessTime(session.getSessionId());

        // Assert
        var updatedSession = sessionService.getSession(session.getSessionId());
        assertTrue(updatedSession.isPresent());
        assertTrue(updatedSession.get().getLastAccessAt().isAfter(createdAt) || 
                   updatedSession.get().getLastAccessAt().isEqual(createdAt));
    }

    @Test
    @DisplayName("应该成功更新会话配置")
    void testUpdateSessionConfig() {
        // Arrange
        ChatSession session = sessionService.createSession("user_123", "测试", null);
        SessionConfig newConfig = SessionConfig.builder()
            .provider("ollama")
            .model("custom_model")
            .temperature(0.5)
            .build();

        // Act
        sessionService.updateSessionConfig(session.getSessionId(), newConfig);

        // Assert
        var updatedSession = sessionService.getSession(session.getSessionId());
        assertTrue(updatedSession.isPresent());
        assertEquals("ollama", updatedSession.get().getConfig().getProvider());
        assertEquals("custom_model", updatedSession.get().getConfig().getModel());
    }

    @Test
    @DisplayName("应该成功更新会话统计信息")
    void testUpdateSessionStats() {
        // Arrange
        ChatSession session = sessionService.createSession("user_123", "测试", null);

        // Act
        sessionService.updateSessionStats(session.getSessionId(), 5, 1000L);

        // Assert
        var updatedSession = sessionService.getSession(session.getSessionId());
        assertTrue(updatedSession.isPresent());
        assertEquals(5, updatedSession.get().getMessageCount());
        assertEquals(1000L, updatedSession.get().getTotalTokens());
    }

    @Test
    @DisplayName("应该成功归档会话")
    void testArchiveSession() {
        // Arrange
        ChatSession session = sessionService.createSession("user_123", "测试", null);

        // Act
        sessionService.archiveSession(session.getSessionId());

        // Assert
        var archivedSession = sessionService.getSession(session.getSessionId());
        assertTrue(archivedSession.isPresent());
        assertEquals(ChatBotConstants.SessionStatus.ARCHIVED, archivedSession.get().getStatus());
    }

    @Test
    @DisplayName("应该成功恢复会话")
    void testRestoreSession() {
        // Arrange
        ChatSession session = sessionService.createSession("user_123", "测试", null);
        sessionService.archiveSession(session.getSessionId());

        // Act
        sessionService.restoreSession(session.getSessionId());

        // Assert
        var restoredSession = sessionService.getSession(session.getSessionId());
        assertTrue(restoredSession.isPresent());
        assertEquals(ChatBotConstants.SessionStatus.ACTIVE, restoredSession.get().getStatus());
    }

    @Test
    @DisplayName("应该成功删除会话")
    void testDeleteSession() {
        // Arrange
        ChatSession session = sessionService.createSession("user_123", "测试", null);

        // Act
        sessionService.deleteSession(session.getSessionId());

        // Assert
        var deletedSession = sessionService.getSession(session.getSessionId());
        assertTrue(deletedSession.isEmpty());
    }

    @Test
    @DisplayName("会话存在检查应正确")
    void testSessionExists() {
        // Arrange
        ChatSession session = sessionService.createSession("user_123", "测试", null);

        // Act & Assert
        assertTrue(sessionService.sessionExists(session.getSessionId()));
        assertFalse(sessionService.sessionExists("non_existent_id"));
    }

    @Test
    @DisplayName("应该成功清除用户的所有会话")
    void testClearUserSessions() {
        // Arrange
        String userId = "user_123";
        sessionService.createSession(userId, "会话1", null);
        sessionService.createSession(userId, "会话2", null);

        // Act
        sessionService.clearUserSessions(userId);

        // Assert
        var sessions = sessionService.getUserAllSessions(userId);
        assertEquals(0, sessions.size());
    }

    @Test
    @DisplayName("应该返回正确的活跃会话数")
    void testGetActiveSessionCount() {
        // Arrange
        ChatSession session1 = sessionService.createSession("user_123", "会话1", null);
        ChatSession session2 = sessionService.createSession("user_123", "会话2", null);
        sessionService.archiveSession(session1.getSessionId());

        // Act
        int count = sessionService.getActiveSessionCount();

        // Assert
        assertEquals(1, count);
    }

    @Test
    @DisplayName("应该返回正确的总会话数")
    void testGetTotalSessionCount() {
        // Arrange
        sessionService.createSession("user_123", "会话1", null);
        sessionService.createSession("user_123", "会话2", null);
        sessionService.createSession("user_456", "会话3", null);

        // Act
        int count = sessionService.getTotalSessionCount();

        // Assert
        assertEquals(3, count);
    }
}
