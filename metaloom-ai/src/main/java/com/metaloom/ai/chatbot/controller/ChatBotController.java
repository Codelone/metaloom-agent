package com.metaloom.ai.chatbot.controller;

import com.metaloom.ai.chatbot.constant.ChatBotConstants;
import com.metaloom.ai.chatbot.dto.ChatRequestDTO;
import com.metaloom.ai.chatbot.dto.ChatResponseDTO;
import com.metaloom.ai.chatbot.dto.ChatSessionDTO;
import com.metaloom.ai.chatbot.dto.SessionConfigDTO;
import com.metaloom.ai.chatbot.model.ChatMessage;
import com.metaloom.ai.chatbot.model.ChatSession;
import com.metaloom.ai.chatbot.model.SessionConfig;
import com.metaloom.ai.chatbot.service.ChatService;
import com.metaloom.ai.chatbot.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ChatBot REST控制器
 * 提供统一的聊天接口API
 */
@Slf4j
@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {

    private final ChatService chatService;
    private final ChatSessionService sessionService;

    @Autowired
    public ChatBotController(ChatService chatService, ChatSessionService sessionService) {
        this.chatService = chatService;
        this.sessionService = sessionService;
    }

    /**
     * 发送聊天消息（创建新会话或继续现有会话）
     * POST /api/chatbot/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDTO> chat(@RequestBody ChatRequestDTO request) {
        long startTime = System.currentTimeMillis();

        try {
            // 验证请求
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ChatResponseDTO.builder()
                        .status(ChatBotConstants.ResponseStatus.ERROR)
                        .errorMessage(ChatBotConstants.ErrorMessages.INVALID_MESSAGE)
                        .build());
            }

            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ChatResponseDTO.builder()
                        .status(ChatBotConstants.ResponseStatus.ERROR)
                        .errorMessage(ChatBotConstants.ErrorMessages.INVALID_USER_ID)
                        .build());
            }

            // 转换DTO到模型
            SessionConfig config = convertDTOToConfig(request.getConfig());

            // 发送消息
            ChatMessage responseMessage = chatService.sendMessage(
                request.getSessionId(),
                request.getUserId(),
                request.getMessage(),
                config
            );

            // 获取更新后的会话信息
            ChatSession session = chatService.getSessionDetail(responseMessage.getMessageId());
            // 由于responseMessage是新添加的消息，我们需要从会话获取sessionId
            Optional<ChatSession> sessionOptional = sessionService.getSession(request.getSessionId());
            if (sessionOptional.isEmpty() && request.getSessionId() == null) {
                // 获取最后一条消息对应的会话（新创建的会话）
                List<ChatSession> activeSessions = sessionService.getUserActiveSessions(request.getUserId());
                if (!activeSessions.isEmpty()) {
                    session = activeSessions.get(0);
                }
            } else if (sessionOptional.isPresent()) {
                session = sessionOptional.get();
            }

            long processingTime = System.currentTimeMillis() - startTime;

            ChatResponseDTO response = ChatResponseDTO.builder()
                .sessionId(session.getSessionId())
                .messageId(responseMessage.getMessageId())
                .content(responseMessage.getContent())
                .status(ChatBotConstants.ResponseStatus.SUCCESS)
                .tokenCount(responseMessage.getTokenCount())
                .messageCount(session.getMessageCount())
                .totalTokens(session.getTotalTokens())
                .processingTime(processingTime)
                .build();

            log.info("聊天请求处理成功: sessionId={}, userId={}, processingTime={}ms",
                session.getSessionId(), request.getUserId(), processingTime);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("聊天请求验证失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ChatResponseDTO.builder()
                    .status(ChatBotConstants.ResponseStatus.ERROR)
                    .errorMessage(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("聊天请求处理失败", e);
            return ResponseEntity.internalServerError()
                .body(ChatResponseDTO.builder()
                    .status(ChatBotConstants.ResponseStatus.ERROR)
                    .errorMessage(ChatBotConstants.ErrorMessages.LLM_ERROR + ": " + e.getMessage())
                    .build());
        }
    }

    /**
     * 创建新会话
     * POST /api/chatbot/sessions
     */
    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionDTO> createSession(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String title = request.get("title");

            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ChatSessionDTO.builder()
                        .status(ChatBotConstants.ResponseStatus.ERROR)
                        .build());
            }

            ChatSession session = sessionService.createSession(userId, title, null);

            ChatSessionDTO response = convertSessionToDTO(session);
            log.info("会话已创建: sessionId={}, userId={}", session.getSessionId(), userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("创建会话失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取会话列表
     * GET /api/chatbot/sessions/{userId}
     */
    @GetMapping("/sessions/{userId}")
    public ResponseEntity<List<ChatSessionDTO>> getUserSessions(@PathVariable String userId) {
        try {
            List<ChatSession> sessions = sessionService.getUserActiveSessions(userId);
            List<ChatSessionDTO> dtoList = sessions.stream()
                .map(this::convertSessionToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {
            log.error("获取会话列表失败: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取会话详情
     * GET /api/chatbot/sessions/{sessionId}/detail
     */
    @GetMapping("/sessions/{sessionId}/detail")
    public ResponseEntity<ChatSessionDTO> getSessionDetail(@PathVariable String sessionId) {
        try {
            ChatSession session = chatService.getSessionDetail(sessionId);
            ChatSessionDTO dto = convertSessionToDTO(session);

            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException e) {
            log.warn("会话不存在: sessionId={}", sessionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取会话详情失败: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取会话的消息历史
     * GET /api/chatbot/sessions/{sessionId}/messages
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessage>> getConversationHistory(@PathVariable String sessionId) {
        try {
            List<ChatMessage> messages = chatService.getConversationHistory(sessionId);
            return ResponseEntity.ok(messages);

        } catch (IllegalArgumentException e) {
            log.warn("会话不存在: sessionId={}", sessionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取消息历史失败: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新会话配置
     * PUT /api/chatbot/sessions/{sessionId}/config
     */
    @PutMapping("/sessions/{sessionId}/config")
    public ResponseEntity<Void> updateSessionConfig(@PathVariable String sessionId,
                                                      @RequestBody SessionConfigDTO configDTO) {
        try {
            SessionConfig config = convertDTOToConfig(configDTO);
            sessionService.updateSessionConfig(sessionId, config);

            log.info("会话配置已更新: sessionId={}", sessionId);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.warn("会话不存在: sessionId={}", sessionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("更新会话配置失败: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 清除会话对话历史
     * DELETE /api/chatbot/sessions/{sessionId}/messages
     */
    @DeleteMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Void> clearConversationHistory(@PathVariable String sessionId) {
        try {
            chatService.clearConversationHistory(sessionId);
            log.info("会话历史已清除: sessionId={}", sessionId);

            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.warn("会话不存在: sessionId={}", sessionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("清除会话历史失败: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 归档会话
     * PUT /api/chatbot/sessions/{sessionId}/archive
     */
    @PutMapping("/sessions/{sessionId}/archive")
    public ResponseEntity<Void> archiveSession(@PathVariable String sessionId) {
        try {
            sessionService.archiveSession(sessionId);
            log.info("会话已归档: sessionId={}", sessionId);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("归档会话失败: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除会话
     * DELETE /api/chatbot/sessions/{sessionId}
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        try {
            sessionService.deleteSession(sessionId);
            log.info("会话已删除: sessionId={}", sessionId);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("删除会话失败: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取系统统计信息
     * GET /api/chatbot/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("activeSessionCount", sessionService.getActiveSessionCount());
            stats.put("totalSessionCount", sessionService.getTotalSessionCount());
            stats.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 健康检查
     * GET /api/chatbot/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // ========================= 辅助方法 =========================

    /**
     * 将SessionConfigDTO转换为SessionConfig
     */
    private SessionConfig convertDTOToConfig(SessionConfigDTO dto) {
        if (dto == null) {
            return null;
        }

        return SessionConfig.builder()
            .provider(dto.getProvider())
            .model(dto.getModel())
            .temperature(dto.getTemperature())
            .maxTokens(dto.getMaxTokens())
            .systemPrompt(dto.getSystemPrompt())
            .memoryMode(dto.getMemoryMode())
            .enableStreaming(dto.getEnableStreaming())
            .contextWindowSize(dto.getContextWindowSize())
            .build();
    }

    /**
     * 将ChatSession转换为ChatSessionDTO
     */
    private ChatSessionDTO convertSessionToDTO(ChatSession session) {
        return ChatSessionDTO.builder()
            .sessionId(session.getSessionId())
            .userId(session.getUserId())
            .title(session.getTitle())
            .description(session.getDescription())
            .status(session.getStatus())
            .createdAt(session.getCreatedAt())
            .lastAccessAt(session.getLastAccessAt())
            .messageCount(session.getMessageCount())
            .totalTokens(session.getTotalTokens())
            .build();
    }
}

