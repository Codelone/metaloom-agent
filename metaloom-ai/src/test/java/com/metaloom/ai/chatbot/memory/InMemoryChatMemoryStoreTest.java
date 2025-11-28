package com.metaloom.ai.chatbot.memory;

import com.metaloom.ai.chatbot.constant.ChatBotConstants;
import com.metaloom.ai.chatbot.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 内存聊天记忆存储单元测试
 */
@DisplayName("InMemoryChatMemoryStore 单元测试")
class InMemoryChatMemoryStoreTest {

    private ChatMemoryStore memoryStore;

    @BeforeEach
    void setUp() {
        memoryStore = new InMemoryChatMemoryStore();
    }

    @Test
    @DisplayName("应该成功保存单条消息")
    void testSaveMessage() {
        // Arrange
        String sessionId = "session_123";
        ChatMessage message = ChatMessage.builder()
            .messageId("msg_001")
            .role(ChatBotConstants.MessageRole.USER)
            .content("你好")
            .timestamp(LocalDateTime.now())
            .build();

        // Act
        memoryStore.saveMessage(sessionId, message);

        // Assert
        assertEquals(1, memoryStore.getMessageCount(sessionId));
    }

    @Test
    @DisplayName("保存消息时参数为空应抛出异常")
    void testSaveMessageWithNullParams() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            memoryStore.saveMessage(null, new ChatMessage());
        });

        assertThrows(IllegalArgumentException.class, () -> {
            memoryStore.saveMessage("session_123", null);
        });
    }

    @Test
    @DisplayName("应该成功批量保存消息")
    void testSaveMessages() {
        // Arrange
        String sessionId = "session_123";
        List<ChatMessage> messages = List.of(
            ChatMessage.builder()
                .messageId("msg_001")
                .role(ChatBotConstants.MessageRole.USER)
                .content("消息1")
                .timestamp(LocalDateTime.now())
                .build(),
            ChatMessage.builder()
                .messageId("msg_002")
                .role(ChatBotConstants.MessageRole.ASSISTANT)
                .content("回复1")
                .timestamp(LocalDateTime.now())
                .build()
        );

        // Act
        memoryStore.saveMessages(sessionId, messages);

        // Assert
        assertEquals(2, memoryStore.getMessageCount(sessionId));
    }

    @Test
    @DisplayName("应该成功获取指定会话的所有消息")
    void testGetMessages() {
        // Arrange
        String sessionId = "session_123";
        ChatMessage msg1 = ChatMessage.builder()
            .messageId("msg_001")
            .role(ChatBotConstants.MessageRole.USER)
            .content("消息1")
            .timestamp(LocalDateTime.now())
            .build();

        ChatMessage msg2 = ChatMessage.builder()
            .messageId("msg_002")
            .role(ChatBotConstants.MessageRole.ASSISTANT)
            .content("消息2")
            .timestamp(LocalDateTime.now())
            .build();

        memoryStore.saveMessage(sessionId, msg1);
        memoryStore.saveMessage(sessionId, msg2);

        // Act
        List<ChatMessage> messages = memoryStore.getMessages(sessionId);

        // Assert
        assertEquals(2, messages.size());
    }

    @Test
    @DisplayName("获取不存在的会话应返回空列表")
    void testGetMessagesForNonExistentSession() {
        // Act
        List<ChatMessage> messages = memoryStore.getMessages("non_existent");

        // Assert
        assertEquals(0, messages.size());
    }

    @Test
    @DisplayName("应该成功获取最近N条消息")
    void testGetRecentMessages() {
        // Arrange
        String sessionId = "session_123";
        for (int i = 1; i <= 5; i++) {
            ChatMessage msg = ChatMessage.builder()
                .messageId("msg_" + String.format("%03d", i))
                .role(ChatBotConstants.MessageRole.USER)
                .content("消息" + i)
                .timestamp(LocalDateTime.now())
                .build();
            memoryStore.saveMessage(sessionId, msg);
        }

        // Act
        List<ChatMessage> recentMessages = memoryStore.getRecentMessages(sessionId, 3);

        // Assert
        assertEquals(3, recentMessages.size());
        assertEquals("msg_003", recentMessages.get(0).getMessageId());
        assertEquals("msg_005", recentMessages.get(2).getMessageId());
    }

    @Test
    @DisplayName("应该正确计算消息数量")
    void testGetMessageCount() {
        // Arrange
        String sessionId = "session_123";
        int expectedCount = 5;

        for (int i = 1; i <= expectedCount; i++) {
            ChatMessage msg = ChatMessage.builder()
                .messageId("msg_" + i)
                .role(ChatBotConstants.MessageRole.USER)
                .content("消息" + i)
                .timestamp(LocalDateTime.now())
                .build();
            memoryStore.saveMessage(sessionId, msg);
        }

        // Act
        int count = memoryStore.getMessageCount(sessionId);

        // Assert
        assertEquals(expectedCount, count);
    }

    @Test
    @DisplayName("应该成功获取单条消息")
    void testGetMessage() {
        // Arrange
        String sessionId = "session_123";
        ChatMessage message = ChatMessage.builder()
            .messageId("msg_001")
            .role(ChatBotConstants.MessageRole.USER)
            .content("测试消息")
            .timestamp(LocalDateTime.now())
            .build();

        memoryStore.saveMessage(sessionId, message);

        // Act
        var retrievedMessage = memoryStore.getMessage("msg_001");

        // Assert
        assertTrue(retrievedMessage.isPresent());
        assertEquals("msg_001", retrievedMessage.get().getMessageId());
        assertEquals("测试消息", retrievedMessage.get().getContent());
    }

    @Test
    @DisplayName("获取不存在的消息应返回空Optional")
    void testGetNonExistentMessage() {
        // Act
        var message = memoryStore.getMessage("non_existent_msg");

        // Assert
        assertTrue(message.isEmpty());
    }

    @Test
    @DisplayName("应该成功清除会话消息")
    void testClearMessages() {
        // Arrange
        String sessionId = "session_123";
        for (int i = 1; i <= 3; i++) {
            ChatMessage msg = ChatMessage.builder()
                .messageId("msg_" + i)
                .role(ChatBotConstants.MessageRole.USER)
                .content("消息" + i)
                .timestamp(LocalDateTime.now())
                .build();
            memoryStore.saveMessage(sessionId, msg);
        }

        // Act
        memoryStore.clearMessages(sessionId);

        // Assert
        assertEquals(0, memoryStore.getMessageCount(sessionId));
    }

    @Test
    @DisplayName("应该成功删除单条消息")
    void testDeleteMessage() {
        // Arrange
        String sessionId = "session_123";
        ChatMessage msg1 = ChatMessage.builder()
            .messageId("msg_001")
            .role(ChatBotConstants.MessageRole.USER)
            .content("消息1")
            .timestamp(LocalDateTime.now())
            .build();

        ChatMessage msg2 = ChatMessage.builder()
            .messageId("msg_002")
            .role(ChatBotConstants.MessageRole.ASSISTANT)
            .content("消息2")
            .timestamp(LocalDateTime.now())
            .build();

        memoryStore.saveMessage(sessionId, msg1);
        memoryStore.saveMessage(sessionId, msg2);

        // Act
        memoryStore.deleteMessage("msg_001");

        // Assert
        assertEquals(1, memoryStore.getMessageCount(sessionId));
        assertFalse(memoryStore.messageExists("msg_001"));
        assertTrue(memoryStore.messageExists("msg_002"));
    }

    @Test
    @DisplayName("应该成功更新消息")
    void testUpdateMessage() {
        // Arrange
        String sessionId = "session_123";
        ChatMessage originalMsg = ChatMessage.builder()
            .messageId("msg_001")
            .role(ChatBotConstants.MessageRole.USER)
            .content("原始内容")
            .timestamp(LocalDateTime.now())
            .build();

        memoryStore.saveMessage(sessionId, originalMsg);

        ChatMessage updatedMsg = ChatMessage.builder()
            .messageId("msg_001")
            .role(ChatBotConstants.MessageRole.USER)
            .content("更新后的内容")
            .timestamp(LocalDateTime.now())
            .build();

        // Act
        memoryStore.updateMessage(updatedMsg);

        // Assert
        var retrieved = memoryStore.getMessage("msg_001");
        assertTrue(retrieved.isPresent());
        assertEquals("更新后的内容", retrieved.get().getContent());
    }

    @Test
    @DisplayName("消息存在检查应正确")
    void testMessageExists() {
        // Arrange
        String sessionId = "session_123";
        ChatMessage msg = ChatMessage.builder()
            .messageId("msg_001")
            .role(ChatBotConstants.MessageRole.USER)
            .content("测试")
            .timestamp(LocalDateTime.now())
            .build();

        memoryStore.saveMessage(sessionId, msg);

        // Act & Assert
        assertTrue(memoryStore.messageExists("msg_001"));
        assertFalse(memoryStore.messageExists("non_existent"));
    }

    @Test
    @DisplayName("应该成功保存和获取对话摘要")
    void testSaveSummary() {
        // Arrange
        String sessionId = "session_123";
        String summary = "这是对话摘要";

        // Act
        memoryStore.saveSummary(sessionId, summary);

        // Assert
        var retrievedSummary = memoryStore.getSummary(sessionId);
        assertTrue(retrievedSummary.isPresent());
        assertEquals(summary, retrievedSummary.get());
    }

    @Test
    @DisplayName("应该成功保存和获取关键信息")
    void testSaveKeyInformation() {
        // Arrange
        String sessionId = "session_123";
        String key = "user_preference";
        String value = "偏好深度对话";

        // Act
        memoryStore.saveKeyInformation(sessionId, key, value);

        // Assert
        var retrievedValue = memoryStore.getKeyInformation(sessionId, key);
        assertTrue(retrievedValue.isPresent());
        assertEquals(value, retrievedValue.get());
    }

    @Test
    @DisplayName("应该成功获取所有关键信息")
    void testGetAllKeyInformation() {
        // Arrange
        String sessionId = "session_123";
        memoryStore.saveKeyInformation(sessionId, "key1", "value1");
        memoryStore.saveKeyInformation(sessionId, "key2", "value2");
        memoryStore.saveKeyInformation(sessionId, "key3", "value3");

        // Act
        Map<String, String> keyInfo = memoryStore.getAllKeyInformation(sessionId);

        // Assert
        assertEquals(3, keyInfo.size());
        assertEquals("value1", keyInfo.get("key1"));
        assertEquals("value2", keyInfo.get("key2"));
        assertEquals("value3", keyInfo.get("key3"));
    }

    @Test
    @DisplayName("应该成功清除会话所有数据")
    void testClearSessionData() {
        // Arrange
        String sessionId = "session_123";
        
        ChatMessage msg = ChatMessage.builder()
            .messageId("msg_001")
            .role(ChatBotConstants.MessageRole.USER)
            .content("测试消息")
            .timestamp(LocalDateTime.now())
            .build();

        memoryStore.saveMessage(sessionId, msg);
        memoryStore.saveSummary(sessionId, "摘要");
        memoryStore.saveKeyInformation(sessionId, "key", "value");

        // Act
        memoryStore.clearSessionData(sessionId);

        // Assert
        assertEquals(0, memoryStore.getMessageCount(sessionId));
        assertTrue(memoryStore.getSummary(sessionId).isEmpty());
        assertTrue(memoryStore.getKeyInformation(sessionId, "key").isEmpty());
    }
}
