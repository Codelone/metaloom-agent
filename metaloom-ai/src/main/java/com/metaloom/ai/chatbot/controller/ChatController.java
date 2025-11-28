package com.metaloom.ai.chatbot.controller;

import com.metaloom.ai.chatbot.constant.ChatBotConstants;
import com.metaloom.ai.chatbot.dto.ChatSessionDTO;
import com.metaloom.ai.chatbot.model.ChatMessage;
import com.metaloom.ai.chatbot.model.ChatSession;
import com.metaloom.ai.chatbot.service.ChatService;
import com.metaloom.ai.chatbot.service.ChatSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    private final ChatSessionService sessionService;
    private final ObjectMapper objectMapper;
    private static final String DEFAULT_USER_ID = "demo-user";

    @Autowired
    public ChatController(ChatService chatService, ChatSessionService sessionService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/chat/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> request) {
        String conversationId = (String) request.get("conversationId");
        String message = (String) request.get("message");

        try {
            ChatMessage responseMessage = chatService.sendMessage(
                    conversationId,
                    DEFAULT_USER_ID,
                    message,
                    null);

            return ResponseEntity.ok(formatResponse(responseMessage));

        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamMessage(@RequestBody Map<String, String> request) {
        String conversationId = request.get("conversationId");
        String message = request.get("message");

        log.info("=== Stream request START ===");
        log.info("conversationId: {}", conversationId);
        log.info("message: {}", message);
        log.info("userId: {}", DEFAULT_USER_ID);

        try {
            return chatService.streamMessage(conversationId, DEFAULT_USER_ID, message, null)
                    .doOnSubscribe(subscription -> log.info("Stream subscribed"))
                    .map(content -> {
                        try {
                            log.debug("Stream chunk: {}", content);
                            Map<String, Object> chunk = new HashMap<>();
                            chunk.put("type", "message");
                            chunk.put("content", content);
                            chunk.put("format", "markdown");
                            String jsonData = objectMapper.writeValueAsString(chunk);
                            // SSE格式需要 "data: " 前缀
                            return "data: " + jsonData + "\n\n";
                        } catch (Exception e) {
                            log.error("Error formatting stream chunk", e);
                            return "data: {\"error\": \"formatting error\"}\n\n";
                        }
                    })
                    .concatWith(Flux.just("data: [DONE]\n\n"))
                    .doOnComplete(() -> log.info("Stream completed successfully"))
                    .doOnError(error -> log.error("Stream error occurred", error))
                    .onErrorResume(error -> {
                        log.error("Resuming from error", error);
                        return Flux.just("data: {\"error\": \"" + error.getMessage() + "\"}\n\n", "data: [DONE]\n\n");
                    });
        } catch (Exception e) {
            log.error("Exception in streamMessage controller", e);
            return Flux.just("data: {\"error\": \"" + e.getMessage() + "\"}\n\n", "data: [DONE]\n\n");
        }
    }

    @GetMapping("/chat/test")
    public ResponseEntity<String> testEndpoint() {
        log.info("Test endpoint called");
        return ResponseEntity.ok("Backend is working!");
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ChatSessionDTO>> getConversations() {
        try {
            List<ChatSession> sessions = sessionService.getUserActiveSessions(DEFAULT_USER_ID);
            List<ChatSessionDTO> dtoList = sessions.stream()
                    .map(this::convertSessionToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            log.error("Error fetching conversations", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/conversations/{conversationId}/export")
    public ResponseEntity<String> exportConversation(@PathVariable String conversationId) {
        try {
            List<ChatMessage> messages = chatService.getConversationHistory(conversationId);
            // Simple JSON export
            return ResponseEntity.ok(objectMapper.writeValueAsString(messages));
        } catch (Exception e) {
            log.error("Error exporting conversation", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private Map<String, Object> formatResponse(ChatMessage message) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "message");
        response.put("content", message.getContent());
        response.put("format", "markdown");

        Map<String, Object> meta = new HashMap<>();
        meta.put("messageId", message.getMessageId());
        meta.put("timestamp", message.getTimestamp().toString());
        response.put("meta", meta);

        response.put("actions", Collections.emptyList());
        return response;
    }

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
