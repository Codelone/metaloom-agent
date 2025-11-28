# ChatBot 架构设计文档

## 1. 系统架构概览

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     前端应用层                               │
│                  (Vue 3 + Vite)                              │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/WebSocket
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              API 网关层 / REST 控制器                         │
│                ChatBotController                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ • 请求验证与路由                                     │    │
│  │ • 响应序列化与格式转换                               │    │
│  │ • 错误处理与日志记录                                 │    │
│  └─────────────────────────────────────────────────────┘    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              业务服务层                                      │
├─────────────────────────────────────────────────────────────┤
│  ChatService              ChatSessionService                 │
│  ┌──────────────────┐    ┌──────────────────┐               │
│  │ • 多轮对话管理   │    │ • 会话生命周期   │               │
│  │ • 消息处理       │    │ • 状态转换管理   │               │
│  │ • 上下文构建     │    │ • 配置管理       │               │
│  │ • LLM调用       │    │ • 隔离与恢复     │               │
│  │ • 记忆操作      │    │ • 统计信息       │               │
│  └──────────────────┘    └──────────────────┘               │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│ 记忆存储层       │ │ LLM集成层        │ │ 数据模型层       │
│                  │ │                  │ │                  │
│ ChatMemoryStore  │ │ ChatClientFactory│ │ ChatSession      │
│ (接口)           │ │                  │ │ ChatMessage      │
│                  │ │ • OpenAI Client │ │ SessionConfig    │
│InMemoryChatMem   │ │ • Ollama Client │ │ Conversation     │
│oryStore          │ │ • 自定义Client  │ │ Context          │
│ (内存实现)       │ │ • Advisor链      │ │                  │
│                  │ │                  │ │ DTO:             │
│• 消息存储        │ │ • 日志记录       │ │ ChatRequestDTO   │
│• 摘要存储        │ │ • 内存管理       │ │ ChatResponseDTO  │
│• 关键信息存储    │ │ • 流式处理       │ │ ChatSessionDTO   │
│                  │ │ • 缓存管理       │ │ SessionConfigDTO │
└──────────────────┘ └──────────────────┘ └──────────────────┘
        ▲                ▲
        │                │
        └────────────────┴──────────┬──────────────────┐
                                   ▼
                      ┌──────────────────────────┐
                      │   配置层                  │
                      │                          │
                      │ ChatBotConfig            │
                      │ ChatBotConstants         │
                      │ application.yml          │
                      └──────────────────────────┘
```

---

## 2. 核心模块设计

### 2.1 REST 控制器层 (ChatBotController)

**职责:**
- HTTP请求接收和验证
- 请求/响应DTO转换
- 错误处理和异常返回
- 操作日志记录

**关键方法:**
```java
@PostMapping("/chat")
ChatResponseDTO chat(ChatRequestDTO request)

@PostMapping("/sessions")
ChatSessionDTO createSession(Map<String, String> request)

@GetMapping("/sessions/{userId}")
List<ChatSessionDTO> getUserSessions(String userId)

@GetMapping("/sessions/{sessionId}/messages")
List<ChatMessage> getConversationHistory(String sessionId)

@PutMapping("/sessions/{sessionId}/config")
void updateSessionConfig(String sessionId, SessionConfigDTO configDTO)
```

**设计特点:**
- ✅ 统一的错误响应格式
- ✅ 参数验证与反序列化
- ✅ 响应包装与序列化
- ✅ 完整的HTTP状态码处理

---

### 2.2 业务服务层 (ChatService)

**职责:**
- 多轮对话的核心逻辑
- 消息处理和上下文管理
- LLM客户端调用
- 记忆存储操作

**核心流程:**

```
用户消息
   │
   ▼
[验证参数]
   │
   ▼
[获取或创建会话]
   │
   ▼
[保存用户消息到记忆]
   │
   ▼
[构建提示词上下文]
   │
   ▼
[调用LLM]
   │
   ▼
[保存助手响应]
   │
   ▼
[更新会话统计]
   │
   ▼
AI响应
```

**关键设计:**

1. **上下文构建** - 动态获取最近N条消息作为上下文
2. **错误处理** - 异常时返回友好提示
3. **令牌计数** - 简化实现估算消息长度
4. **配置灵活性** - 支持每次请求自定义LLM配置

---

### 2.3 会话管理层 (ChatSessionService)

**职责:**
- 会话的创建、获取、更新、删除
- 会话状态管理(ACTIVE/ARCHIVED/DELETED)
- 用户会话隔离
- 会话统计信息维护

**会话生命周期:**

```
┌─────────────────────────────────────────┐
│           会话创建                       │
│     (ACTIVE状态)                         │
└────────────────┬────────────────────────┘
                 │
        ┌────────┴────────┐
        ▼                 ▼
   [活跃使用]          [长期不用]
        │                │
        ▼                ▼
    [ACTIVE]        [归档ARCHIVED]
        │                │
        └────────┬────────┘
                 ▼
            [删除]
          (DELETED)
```

**关键特性:**

1. **用户隔离** - 每个用户有独立的会话空间
2. **配置继承** - 新会话继承默认配置
3. **访问跟踪** - 记录最后访问时间
4. **超时管理** - 支持会话过期清理

---

### 2.4 记忆存储层 (ChatMemoryStore)

**设计模式:** Strategy 模式

```
ChatMemoryStore (接口)
    ▲
    │ implements
    ├─── InMemoryChatMemoryStore (内存实现)
    ├─── DatabaseChatMemoryStore (数据库实现)
    ├─── RedisChatMemoryStore (Redis实现)
    └─── VectorStoreChatMemoryStore (向量数据库实现)
```

**核心功能:**

1. **消息存储** - 保存和检索对话消息
   - `saveMessage()` - 单条保存
   - `saveMessages()` - 批量保存
   - `getMessages()` - 获取全部
   - `getRecentMessages()` - 获取最近N条

2. **摘要存储** - 保存对话摘要（长期记忆）
   - `saveSummary()` - 保存摘要
   - `getSummary()` - 获取摘要

3. **关键信息存储** - 保存用户偏好、意图等
   - `saveKeyInformation()` - 保存键值对
   - `getKeyInformation()` - 获取值
   - `getAllKeyInformation()` - 获取全部

4. **数据清理**
   - `clearMessages()` - 清除会话消息
   - `clearSessionData()` - 清除全部会话数据

---

### 2.5 数据模型层

#### ChatMessage (聊天消息)
```java
record ChatMessage {
    String messageId;           // 唯一标识
    String role;                // user/assistant/system
    String content;             // 消息内容
    LocalDateTime timestamp;    // 时间戳
    String metadata;            // 扩展信息
    Integer tokenCount;         // 令牌数
}
```

#### ChatSession (聊天会话)
```java
record ChatSession {
    String sessionId;           // 会话ID
    String userId;              // 用户ID
    String title;               // 标题
    String status;              // ACTIVE/ARCHIVED/DELETED
    LocalDateTime createdAt;    // 创建时间
    LocalDateTime lastAccessAt; // 最后访问
    List<ChatMessage> messages; // 消息列表
    SessionConfig config;       // 会话配置
    Integer messageCount;       // 消息数统计
    Long totalTokens;           // 令牌总数
}
```

#### SessionConfig (会话配置)
```java
record SessionConfig {
    String provider;            // openai/ollama
    String model;               // 模型名称
    Double temperature;         // 温度参数
    Integer maxTokens;          // 最大令牌数
    String systemPrompt;        // 系统提示词
    String memoryMode;          // none/short_term/long_term
    Boolean enableStreaming;    // 流式响应
    Integer contextWindowSize;  // 上下文窗口
}
```

---

## 3. 关键设计模式

### 3.1 Builder 模式
用于构建复杂的消息和会话对象：

```java
ChatMessage message = ChatMessage.builder()
    .messageId("msg_001")
    .role(ChatBotConstants.MessageRole.USER)
    .content("你好")
    .timestamp(LocalDateTime.now())
    .build();
```

### 3.2 Strategy 模式
用于记忆存储的多种实现：

```java
interface ChatMemoryStore {
    void saveMessage(String sessionId, ChatMessage message);
    // ...
}

// 可切换不同实现
ChatMemoryStore memoryStore = new InMemoryChatMemoryStore();
// 或
ChatMemoryStore memoryStore = new DatabaseChatMemoryStore();
```

### 3.3 Template Method 模式
用于ChatService的对话流程：

```java
// 模板：
1. 验证参数
2. 获取或创建会话
3. 保存用户消息
4. 构建上下文
5. 调用LLM
6. 保存响应
7. 更新统计
```

### 3.4 Dependency Injection 模式
Spring完整的依赖注入：

```java
@Service
public class ChatService {
    @Autowired
    private ChatSessionService sessionService;
    
    @Autowired
    private ChatMemoryStore memoryStore;
    
    @Autowired
    private ChatClientFactory chatClientFactory;
}
```

---

## 4. 多轮对话流程详解

### 完整的多轮对话时序图

```
用户                     ChatBot服务             LLM
 │                          │                      │
 ├─── 发送消息1 ────────────>│                      │
 │                          │                      │
 │                          ├── 保存消息1          │
 │                          │                      │
 │                          ├── 构建上下文1 ───────>│
 │                          │                  (消息1)
 │                          │<────── 响应1 ────────┤
 │<────── 响应1 ────────────┤                      │
 │                          ├── 保存响应1          │
 │                          │                      │
 │                          │                      │
 ├─── 发送消息2 ────────────>│                      │
 │                          │                      │
 │                          ├── 保存消息2          │
 │                          │                      │
 │                          ├── 构建上下文2 ───────>│
 │                          │ (消息1+响应1+消息2)  │
 │                          │<────── 响应2 ────────┤
 │<────── 响应2 ────────────┤                      │
 │                          ├── 保存响应2          │
 │                          │                      │
```

### 代码实现流程

```java
// 1. 发送消息（可能是新会话或现有会话）
public ChatMessage sendMessage(
    String sessionId,              // 可选
    String userId,                 // 必填
    String message,                // 必填
    SessionConfig customConfig     // 可选
) {
    // 2. 获取或创建会话
    ChatSession session = getOrCreateSession(sessionId, userId, customConfig);
    
    // 3. 保存用户消息
    ChatMessage userMessage = createUserMessage(message);
    memoryStore.saveMessage(session.getSessionId(), userMessage);
    
    // 4. 构建提示词（包含历史消息）
    String prompt = buildPrompt(session);
    
    // 5. 调用LLM
    ChatClient client = getChatClient(session.getConfig());
    String response = client.prompt()
        .system(buildSystemPrompt())
        .user(prompt)
        .call()
        .content();
    
    // 6. 保存助手响应
    ChatMessage assistantMessage = createAssistantMessage(response);
    memoryStore.saveMessage(session.getSessionId(), assistantMessage);
    
    // 7. 更新会话统计
    updateSessionStats(session);
    
    return assistantMessage;
}
```

---

## 5. 会话隔离与恢复

### 用户隔离机制

```
User1
├── Session A (active)
│   ├── Message 1
│   ├── Message 2
│   └── Message 3
├── Session B (archived)
│   ├── Message 1
│   └── Message 2
└── Session C (deleted)

User2
├── Session D (active)
│   ├── Message 1
│   └── Message 2
└── Session E (active)

User3
└── Session F (active)
```

### 恢复机制

```java
// 步骤1: 获取用户会话
List<ChatSession> sessions = sessionService.getUserAllSessions(userId);

// 步骤2: 选择要恢复的会话
ChatSession archivedSession = sessions.stream()
    .filter(s -> s.getSessionId().equals(sessionId))
    .findFirst();

// 步骤3: 恢复会话
sessionService.restoreSession(sessionId);

// 步骤4: 继续对话
ChatMessage response = chatService.sendMessage(
    sessionId,
    userId,
    "继续之前的话题..."
);
```

---

## 6. 记忆管理策略

### 短期记忆 (Short-term Memory)
- **存储范围:** 当前会话的最近N条消息
- **实现:** 使用 `getRecentMessages(sessionId, contextWindowSize)`
- **清理时机:** 会话结束或超时

### 长期记忆 (Long-term Memory)
- **存储内容:** 
  - 对话摘要
  - 关键信息（用户偏好、意图等）
  - 提取的知识点
- **实现:** `saveSummary()` 和 `saveKeyInformation()`
- **应用:** 新会话可引用历史记忆

### 记忆存储流程

```
对话过程
   │
   ├── 短期记忆
   │   └── 逐条保存消息
   │       (使用ContextWindowSize)
   │
   └── 长期记忆
       └── 定期操作:
           ├── 调用Agent提取摘要
           ├── 识别关键信息
           └── 存储到记忆库
```

---

## 7. 配置管理

### 配置优先级

```
请求级配置 (highest)
    ▲
    │ override
    │
会话配置
    ▲
    │ or
    │
应用默认配置 (application.yml)
    │
    ▼
ChatBotConfig (lowest)
```

### 配置继承示例

```java
// 应用默认配置
metaloom.chatbot.default-model: "deepseek-v3"
metaloom.chatbot.default-temperature: 0.7

// 会话创建时继承
SessionConfig config = buildDefaultConfig(); // 继承应用配置

// 请求时可覆盖
POST /api/chatbot/chat {
    "config": {
        "model": "gpt-4"  // 覆盖默认模型
    }
}
```

---

## 8. 错误处理策略

### 错误分类

```
错误类型
├── 参数验证错误
│   ├── INVALID_USER_ID
│   ├── INVALID_SESSION_ID
│   └── INVALID_MESSAGE
│
├── 业务逻辑错误
│   ├── SESSION_NOT_FOUND
│   ├── SESSION_EXPIRED
│   └── MAX_SESSIONS_EXCEEDED
│
└── 系统错误
    ├── LLM_ERROR
    ├── STORAGE_ERROR
    └── INTERNAL_ERROR
```

### 错误恢复

```java
try {
    ChatMessage response = chatService.sendMessage(...);
} catch (IllegalArgumentException e) {
    // 参数验证错误 -> 400
    return ResponseEntity.badRequest()
        .body(errorResponse(e.getMessage()));
} catch (NotFoundException e) {
    // 资源不存在 -> 404
    return ResponseEntity.notFound().build();
} catch (Exception e) {
    // 系统错误 -> 500
    log.error("Internal error", e);
    return ResponseEntity.internalServerError()
        .body(errorResponse("服务暂时不可用"));
}
```

---

## 9. 扩展接入点

### 9.1 接入智能体系统

```java
// 在ChatService中判断记忆模式
if (config.getMemoryMode().equals("long_term")) {
    // 调用Agent进行对话分析
    String summary = metadataAgent.analyzeConversation(sessionId);
    memoryStore.saveSummary(sessionId, summary);
}
```

### 9.2 接入数据库

```java
// 实现DatabaseChatMemoryStore
@Component
public class DatabaseChatMemoryStore implements ChatMemoryStore {
    @Autowired
    private ChatMessageRepository messageRepo;
    // ...
}

// 在ChatService中注入
@Qualifier("databaseChatMemoryStore")
private ChatMemoryStore memoryStore;
```

### 9.3 集成WebSocket

```java
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    @Override
    protected void handleTextMessage(WebSocketSession session, 
                                     TextMessage message) {
        // 接收消息
        ChatRequestDTO request = parse(message);
        
        // 调用ChatService
        ChatMessage response = chatService.sendMessage(...);
        
        // 推送响应
        session.sendMessage(new TextMessage(response));
    }
}
```

---

## 10. 性能考虑

### 并发处理
- 使用 `ConcurrentHashMap` 实现线程安全的会话存储
- 支持多线程并发会话管理

### 内存管理
- 定期清理过期会话
- 限制单用户会话数量
- 实现消息老化策略

### 缓存策略
- Redis缓存热点会话
- 缓存LLM响应以避免重复调用
- 预热常用配置

---

## 11. 监控与日志

### 监控指标
- 会话创建速率
- 平均对话轮数
- 消息处理延迟
- LLM调用成功率

### 日志级别
```yaml
logging:
  level:
    com.metaloom.ai.chatbot: DEBUG
    org.springframework.ai: DEBUG
```

---

## 总结

该ChatBot架构设计遵循以下原则：

1. ✅ **模块化** - 清晰的分层和职责划分
2. ✅ **可扩展** - 易于添加新功能和替换实现
3. ✅ **可维护** - 使用设计模式和最佳实践
4. ✅ **可靠** - 完善的错误处理和日志
5. ✅ **灵活** - 配置和定制能力强
6. ✅ **高性能** - 考虑并发和缓存
7. ✅ **可测试** - 充分的单元测试和集成测试

该架构为未来与A2A智能体系统的集成预留了充分的接入点。
