package com.metaloom.ai.chatbot.service;

import com.metaloom.ai.chatbot.constant.ChatBotConstants;
import com.metaloom.ai.chatbot.model.ChatMessage;
import com.metaloom.ai.chatbot.model.ChatSession;
import com.metaloom.ai.chatbot.model.SessionConfig;
import com.metaloom.ai.chatbot.memory.ChatMemoryStore;
import com.metaloom.ai.chatbot.config.ChatBotConfig;
import com.metaloom.model.llm.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import reactor.core.publisher.Flux;

/**
 * 聊天业务服务层
 * 封装多轮对话逻辑，整合会话管理和LLM调用
 */
@Slf4j
@Service
public class ChatService {

    private final ChatSessionService sessionService;
    private final ChatMemoryStore memoryStore;
    private final ChatClientFactory chatClientFactory;
    private final ChatBotConfig chatBotConfig;

    @Autowired
    public ChatService(ChatSessionService sessionService,
            ChatMemoryStore memoryStore,
            ChatClientFactory chatClientFactory,
            ChatBotConfig chatBotConfig) {
        this.sessionService = sessionService;
        this.memoryStore = memoryStore;
        this.chatClientFactory = chatClientFactory;
        this.chatBotConfig = chatBotConfig;
    }

    /**
     * 发送消息（多轮对话）
     * 如果sessionId为空，则创建新会话
     */
    public ChatMessage sendMessage(String sessionId, String userId, String message, SessionConfig customConfig) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.INVALID_USER_ID);
        }

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.INVALID_MESSAGE);
        }

        // 获取或创建会话
        ChatSession session;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            Optional<ChatSession> optSession = sessionService.getSession(sessionId);
            if (optSession.isEmpty()) {
                throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.SESSION_NOT_FOUND);
            }
            session = optSession.get();
        } else {
            // 创建新会话
            session = sessionService.createSession(userId, null, customConfig);
        }

        // 更新会话访问时间
        sessionService.updateSessionAccessTime(session.getSessionId());

        // 保存用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .messageId(generateMessageId())
                .role(ChatBotConstants.MessageRole.USER)
                .content(message)
                .timestamp(LocalDateTime.now())
                .build();

        memoryStore.saveMessage(session.getSessionId(), userMessage);

        // 构建提示词上下文（历史消息）
        String prompt = buildPrompt(session);

        // 调用LLM获取响应
        String assistantResponse;
        try {
            SessionConfig config = customConfig != null ? customConfig : session.getConfig();
            ChatClient chatClient = getChatClient(config);

            assistantResponse = chatClient.prompt()
                    .system(config.getSystemPrompt() != null ? config.getSystemPrompt() : buildSystemPrompt())
                    .user(prompt)
                    .call()
                    .content();

            log.info("LLM响应成功: sessionId={}, userId={}", session.getSessionId(), userId);
        } catch (Exception e) {
            log.error("LLM调用失败: sessionId={}, userId={}, error={}", session.getSessionId(), userId, e.getMessage());
            throw new RuntimeException(ChatBotConstants.ErrorMessages.LLM_ERROR, e);
        }

        // 保存助手响应
        ChatMessage assistantMessage = ChatMessage.builder()
                .messageId(generateMessageId())
                .role(ChatBotConstants.MessageRole.ASSISTANT)
                .content(assistantResponse)
                .timestamp(LocalDateTime.now())
                .build();

        memoryStore.saveMessage(session.getSessionId(), assistantMessage);

        // 更新会话统计信息
        int messageCount = memoryStore.getMessageCount(session.getSessionId());
        long totalTokens = estimateTokenCount(session.getSessionId());
        sessionService.updateSessionStats(session.getSessionId(), messageCount, totalTokens);

        log.debug("消息已处理并响应: sessionId={}, messageId={}", session.getSessionId(), assistantMessage.getMessageId());

        return assistantMessage;
    }

    /**
     * 获取会话的消息历史
     */
    public List<ChatMessage> getConversationHistory(String sessionId) {
        if (!sessionService.sessionExists(sessionId)) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.SESSION_NOT_FOUND);
        }

        return memoryStore.getMessages(sessionId);
    }

    /**
     * 获取会话的最近N条消息
     */
    public List<ChatMessage> getRecentMessages(String sessionId, int count) {
        if (!sessionService.sessionExists(sessionId)) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.SESSION_NOT_FOUND);
        }

        return memoryStore.getRecentMessages(sessionId, count);
    }

    /**
     * 获取会话详情
     */
    public ChatSession getSessionDetail(String sessionId) {
        Optional<ChatSession> session = sessionService.getSession(sessionId);
        if (session.isEmpty()) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.SESSION_NOT_FOUND);
        }

        ChatSession detail = session.get();
        detail.setMessages(memoryStore.getMessages(sessionId));
        return detail;
    }

    /**
     * 清除会话对话历史
     */
    public void clearConversationHistory(String sessionId) {
        if (!sessionService.sessionExists(sessionId)) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.SESSION_NOT_FOUND);
        }

        memoryStore.clearMessages(sessionId);
        sessionService.updateSessionStats(sessionId, 0, 0);
        log.info("会话历史已清除: sessionId={}", sessionId);
    }

    /**
     * 保存关键信息到会话记忆
     */
    public void saveKeyInformation(String sessionId, String key, String value) {
        if (!sessionService.sessionExists(sessionId)) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.SESSION_NOT_FOUND);
        }

        memoryStore.saveKeyInformation(sessionId, key, value);
    }

    /**
     * 获取会话的关键信息
     */
    public Map<String, String> getKeyInformation(String sessionId) {
        if (!sessionService.sessionExists(sessionId)) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.SESSION_NOT_FOUND);
        }

        return memoryStore.getAllKeyInformation(sessionId);
    }

    /**
     * 构建提示词（包含历史消息）
     */
    private String buildPrompt(ChatSession session) {
        List<ChatMessage> recentMessages = memoryStore.getRecentMessages(
                session.getSessionId(),
                session.getConfig().getContextWindowSize());

        StringBuilder promptBuilder = new StringBuilder();

        // 添加最近的历史消息作为上下文
        for (ChatMessage msg : recentMessages) {
            if (!ChatBotConstants.MessageRole.ASSISTANT.equals(msg.getRole())) {
                continue; // 跳过系统消息，只保留用户和助手消息
            }
            promptBuilder.append("Assistant: ").append(msg.getContent()).append("\n");
        }

        // 添加最后的用户消息（会话中最后一条用户消息）
        Optional<ChatMessage> lastUserMessage = recentMessages.stream()
                .filter(msg -> ChatBotConstants.MessageRole.USER.equals(msg.getRole()))
                .reduce((first, second) -> second);

        if (lastUserMessage.isPresent()) {
            promptBuilder.append("User: ").append(lastUserMessage.get().getContent()).append("\n");
        }

        return promptBuilder.toString();
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return chatBotConfig.getSystemPrompt() != null
                ? chatBotConfig.getSystemPrompt()
                : "你是一个有帮助的AI助手。请根据用户的问题提供准确、清晰的回答。";
    }

    /**
     * 获取ChatClient
     */
    private ChatClient getChatClient(SessionConfig config) {
        try {
            return chatClientFactory.getClient(config.getProvider(), config.getModel());
        } catch (Exception e) {
            log.warn("无法获取指定的ChatClient: provider={}, model={}, 使用默认客户端",
                    config.getProvider(), config.getModel());
            return chatClientFactory.getClient("openai", "deepseek-v3");
        }
    }

    /**
     * 生成消息ID
     */
    private String generateMessageId() {
        return "msg_" + UUID.randomUUID().toString().substring(0, 12);
    }

    /**
     * 流式发送消息
     */
    public Flux<String> streamMessage(String sessionId, String userId, String message, SessionConfig customConfig) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.INVALID_USER_ID);
        }

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.INVALID_MESSAGE);
        }

        // 获取或创建会话
        ChatSession session;
        if (sessionId != null && !sessionId.trim().isEmpty()) {

            Optional<ChatSession> optSession = sessionService.getSession(sessionId);
            if (optSession.isEmpty()) {
                throw new IllegalArgumentException(ChatBotConstants.ErrorMessages.SESSION_NOT_FOUND);
            }
            session = optSession.get();
        } else {
            // 创建新会话
            session = sessionService.createSession(userId, null, customConfig);
        }

        // 更新会话访问时间
        sessionService.updateSessionAccessTime(session.getSessionId());

        // 保存用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .messageId(generateMessageId())
                .role(ChatBotConstants.MessageRole.USER)
                .content(message)
                .timestamp(LocalDateTime.now())
                .build();

        memoryStore.saveMessage(session.getSessionId(), userMessage);

        // 构建提示词上下文
        String prompt = buildPrompt(session);
        SessionConfig config = customConfig != null ? customConfig : session.getConfig();
        ChatClient chatClient = getChatClient(config);

        StringBuilder contentBuilder = new StringBuilder();

        return chatClient.prompt()
                .system(config.getSystemPrompt() != null ? config.getSystemPrompt() : buildSystemPrompt())
                .user(prompt)
                .stream()
                .content()
                .doOnNext(contentBuilder::append)
                .doOnComplete(() -> {
                    String fullContent = contentBuilder.toString();
                    // 保存助手响应
                    ChatMessage assistantMessage = ChatMessage.builder()
                            .messageId(generateMessageId())
                            .role(ChatBotConstants.MessageRole.ASSISTANT)
                            .content(fullContent)
                            .timestamp(LocalDateTime.now())
                            .build();

                    memoryStore.saveMessage(session.getSessionId(), assistantMessage);

                    // 更新会话统计信息
                    int messageCount = memoryStore.getMessageCount(session.getSessionId());
                    long totalTokens = estimateTokenCount(session.getSessionId());
                    sessionService.updateSessionStats(session.getSessionId(), messageCount, totalTokens);

                    log.info("流式响应完成并保存: sessionId={}", session.getSessionId());
                })
                .doOnError(e -> {
                    log.error("流式响应出错: sessionId={}", session.getSessionId(), e);
                });
    }

    /**
     * 估算令牌数（简化实现）
     */
    private long estimateTokenCount(String sessionId) {
        List<ChatMessage> messages = memoryStore.getMessages(sessionId);
        return messages.stream()
                .mapToLong(msg -> (long) Math.ceil(msg.getContent().length() / 4.0))
                .sum();
    }
}
