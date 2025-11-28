package com.metaloom.ai.chatbot.integration;

import com.metaloom.ai.chatbot.config.ChatBotConfig;
import com.metaloom.ai.chatbot.constant.ChatBotConstants;
import com.metaloom.ai.chatbot.memory.ChatMemoryStore;
import com.metaloom.ai.chatbot.memory.InMemoryChatMemoryStore;
import com.metaloom.ai.chatbot.model.ChatMessage;
import com.metaloom.ai.chatbot.model.ChatSession;
import com.metaloom.ai.chatbot.model.SessionConfig;
import com.metaloom.ai.chatbot.service.ChatSessionService;
import com.metaloom.ai.chatbot.service.ChatService;
import com.metaloom.model.llm.ChatClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ChatBot集成测试
 * 测试会话管理、消息存储、对话流程的完整集成
 */
@DisplayName("ChatBot 集成测试")
class ChatBotIntegrationTest {

    private ChatSessionService sessionService;
    private ChatMemoryStore memoryStore;
    private ChatService chatService;

    @Mock
    private ChatClientFactory chatClientFactory;

    @Mock
    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ChatBotConfig chatBotConfig = new ChatBotConfig();
        sessionService = new ChatSessionService(chatBotConfig);
        memoryStore = new InMemoryChatMemoryStore();
        chatService = new ChatService(sessionService, memoryStore, chatClientFactory, chatBotConfig);

        // Mock ChatClient行为
        when(chatClientFactory.getClient(anyString(), anyString())).thenReturn(chatClient);
    }

    @Test
    @DisplayName("完整的多轮对话流程")
    void testMultiTurnConversationFlow() {
        // 1. 创建会话
        String userId = "user_001";
        ChatSession session = sessionService.createSession(userId, "测试会话", null);

        assertNotNull(session);
        assertEquals(ChatBotConstants.SessionStatus.ACTIVE, session.getStatus());

        // 2. 保存消息到记忆存储
        ChatMessage msg1 = ChatMessage.builder()
            .messageId("msg_001")
            .role(ChatBotConstants.MessageRole.USER)
            .content("你好，请介绍一下你自己")
            .build();

        memoryStore.saveMessage(session.getSessionId(), msg1);

        ChatMessage msg2 = ChatMessage.builder()
            .messageId("msg_002")
            .role(ChatBotConstants.MessageRole.ASSISTANT)
            .content("我是一个AI助手，很高兴认识你！")
            .build();

        memoryStore.saveMessage(session.getSessionId(), msg2);

        // 3. 验证消息被正确保存
        List<ChatMessage> messages = memoryStore.getMessages(session.getSessionId());
        assertEquals(2, messages.size());

        // 4. 更新会话统计
        sessionService.updateSessionStats(session.getSessionId(), 2, 100L);

        // 5. 获取更新后的会话
        var updatedSession = sessionService.getSession(session.getSessionId());
        assertTrue(updatedSession.isPresent());
        assertEquals(2, updatedSession.get().getMessageCount());
        assertEquals(100L, updatedSession.get().getTotalTokens());
    }

    @Test
    @DisplayName("会话状态转换流程")
    void testSessionStateTransition() {
        // 创建会话
        ChatSession session = sessionService.createSession("user_002", "测试", null);
        assertEquals(ChatBotConstants.SessionStatus.ACTIVE, session.getStatus());

        // 添加消息
        for (int i = 1; i <= 3; i++) {
            ChatMessage msg = ChatMessage.builder()
                .messageId("msg_" + i)
                .role(ChatBotConstants.MessageRole.USER)
                .content("问题 " + i)
                .build();
            memoryStore.saveMessage(session.getSessionId(), msg);
        }

        // 归档会话
        sessionService.archiveSession(session.getSessionId());
        var archivedSession = sessionService.getSession(session.getSessionId());
        assertEquals(ChatBotConstants.SessionStatus.ARCHIVED, archivedSession.get().getStatus());

        // 恢复会话
        sessionService.restoreSession(session.getSessionId());
        var restoredSession = sessionService.getSession(session.getSessionId());
        assertEquals(ChatBotConstants.SessionStatus.ACTIVE, restoredSession.get().getStatus());

        // 验证消息仍然存在
        assertEquals(3, memoryStore.getMessageCount(session.getSessionId()));
    }

    @Test
    @DisplayName("用户多个会话管理")
    void testMultipleSessionsPerUser() {
        String userId = "user_003";

        // 创建多个会话
        ChatSession session1 = sessionService.createSession(userId, "会话1", null);
        ChatSession session2 = sessionService.createSession(userId, "会话2", null);
        ChatSession session3 = sessionService.createSession(userId, "会话3", null);

        // 添加消息到不同的会话
        ChatMessage msg1 = ChatMessage.builder()
            .messageId("msg_101")
            .role(ChatBotConstants.MessageRole.USER)
            .content("会话1的消息")
            .build();
        memoryStore.saveMessage(session1.getSessionId(), msg1);

        ChatMessage msg2 = ChatMessage.builder()
            .messageId("msg_102")
            .role(ChatBotConstants.MessageRole.USER)
            .content("会话2的消息")
            .build();
        memoryStore.saveMessage(session2.getSessionId(), msg2);

        // 获取用户的所有活跃会话
        List<ChatSession> userSessions = sessionService.getUserActiveSessions(userId);
        assertEquals(3, userSessions.size());

        // 验证每个会话的消息是隔离的
        assertEquals(1, memoryStore.getMessageCount(session1.getSessionId()));
        assertEquals(1, memoryStore.getMessageCount(session2.getSessionId()));
        assertEquals(0, memoryStore.getMessageCount(session3.getSessionId()));

        // 归档一个会话
        sessionService.archiveSession(session1.getSessionId());

        // 获取活跃会话（应该只有2个）
        List<ChatSession> activeSessions = sessionService.getUserActiveSessions(userId);
        assertEquals(2, activeSessions.size());
    }

    @Test
    @DisplayName("会话记忆功能完整流程")
    void testSessionMemoryFlow() {
        ChatSession session = sessionService.createSession("user_004", "记忆测试", null);

        // 保存对话摘要
        String summary = "用户询问了关于天气和交通的信息";
        memoryStore.saveSummary(session.getSessionId(), summary);

        // 保存关键信息
        memoryStore.saveKeyInformation(session.getSessionId(), "location", "北京");
        memoryStore.saveKeyInformation(session.getSessionId(), "preference", "实时天气");
        memoryStore.saveKeyInformation(session.getSessionId(), "interest", "交通信息");

        // 验证摘要
        var retrievedSummary = memoryStore.getSummary(session.getSessionId());
        assertTrue(retrievedSummary.isPresent());
        assertEquals(summary, retrievedSummary.get());

        // 验证关键信息
        var location = memoryStore.getKeyInformation(session.getSessionId(), "location");
        assertTrue(location.isPresent());
        assertEquals("北京", location.get());

        var allKeyInfo = memoryStore.getAllKeyInformation(session.getSessionId());
        assertEquals(3, allKeyInfo.size());
        assertEquals("北京", allKeyInfo.get("location"));
        assertEquals("实时天气", allKeyInfo.get("preference"));
        assertEquals("交通信息", allKeyInfo.get("interest"));
    }

    @Test
    @DisplayName("会话配置生效流程")
    void testSessionConfigurationFlow() {
        // 创建自定义配置的会话
        SessionConfig customConfig = SessionConfig.builder()
            .provider("ollama")
            .model("custom_model")
            .temperature(0.5)
            .maxTokens(1500)
            .memoryMode(ChatBotConstants.MemoryMode.LONG_TERM)
            .enableStreaming(false)
            .contextWindowSize(20)
            .build();

        ChatSession session = sessionService.createSession("user_005", "自定义配置", customConfig);

        // 验证配置被正确应用
        assertEquals("ollama", session.getConfig().getProvider());
        assertEquals("custom_model", session.getConfig().getModel());
        assertEquals(0.5, session.getConfig().getTemperature());
        assertEquals(1500, session.getConfig().getMaxTokens());
        assertEquals(ChatBotConstants.MemoryMode.LONG_TERM, session.getConfig().getMemoryMode());
        assertFalse(session.getConfig().getEnableStreaming());
        assertEquals(20, session.getConfig().getContextWindowSize());

        // 更新配置
        SessionConfig newConfig = SessionConfig.builder()
            .provider("openai")
            .model("gpt-4")
            .temperature(0.8)
            .build();

        sessionService.updateSessionConfig(session.getSessionId(), newConfig);

        // 验证新配置
        var updatedSession = sessionService.getSession(session.getSessionId());
        assertTrue(updatedSession.isPresent());
        assertEquals("openai", updatedSession.get().getConfig().getProvider());
        assertEquals("gpt-4", updatedSession.get().getConfig().getModel());
        assertEquals(0.8, updatedSession.get().getConfig().getTemperature());
    }

    @Test
    @DisplayName("会话清理流程")
    void testSessionCleanupFlow() {
        String userId = "user_006";

        // 创建多个会话
        ChatSession session1 = sessionService.createSession(userId, "会话1", null);
        ChatSession session2 = sessionService.createSession(userId, "会话2", null);

        // 添加消息
        for (int i = 1; i <= 5; i++) {
            ChatMessage msg = ChatMessage.builder()
                .messageId("msg_" + i)
                .role(ChatBotConstants.MessageRole.USER)
                .content("消息 " + i)
                .build();
            memoryStore.saveMessage(session1.getSessionId(), msg);
        }

        // 验证消息存在
        assertEquals(5, memoryStore.getMessageCount(session1.getSessionId()));

        // 清除消息
        memoryStore.clearMessages(session1.getSessionId());
        assertEquals(0, memoryStore.getMessageCount(session1.getSessionId()));

        // 删除会话
        sessionService.deleteSession(session1.getSessionId());
        assertTrue(sessionService.getSession(session1.getSessionId()).isEmpty());

        // 验证另一个会话仍然存在
        assertTrue(sessionService.getSession(session2.getSessionId()).isPresent());

        // 清除用户的所有会话
        sessionService.clearUserSessions(userId);
        assertTrue(sessionService.getUserAllSessions(userId).isEmpty());
    }

    @Test
    @DisplayName("上下文窗口管理")
    void testContextWindowManagement() {
        ChatSession session = sessionService.createSession("user_007", "上下文测试", null);

        // 添加15条消息（超过默认的10条窗口大小）
        for (int i = 1; i <= 15; i++) {
            ChatMessage msg = ChatMessage.builder()
                .messageId("msg_" + String.format("%03d", i))
                .role(i % 2 == 0 ? ChatBotConstants.MessageRole.ASSISTANT : ChatBotConstants.MessageRole.USER)
                .content("消息 " + i)
                .build();
            memoryStore.saveMessage(session.getSessionId(), msg);
        }

        // 验证所有消息都被保存
        assertEquals(15, memoryStore.getMessageCount(session.getSessionId()));

        // 获取最近10条消息（上下文窗口）
        List<ChatMessage> recentMessages = memoryStore.getRecentMessages(session.getSessionId(), 10);
        assertEquals(10, recentMessages.size());

        // 验证是最后的10条消息
        assertEquals("msg_006", recentMessages.get(0).getMessageId());
        assertEquals("msg_015", recentMessages.get(9).getMessageId());
    }
}
