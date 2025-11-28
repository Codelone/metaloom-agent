# ChatBot 后端接口服务 API文档

## 概述

这是基于 **Spring Boot 3.4.3** 和 **Spring AI 1.1** 的通用聊天机器人后端服务。提供完整的多轮对话、会话管理、记忆存储等核心功能，为前端提供标准化的聊天机器人能力。

## 核心特性

✅ **多轮对话管理** - 支持上下文保持与自动管理
✅ **会话状态持久化** - 用户会话隔离与恢复
✅ **对话记忆机制** - 短期记忆与长期记忆存储
✅ **统一API入口** - 标准化的REST接口便于前端调用
✅ **灵活可扩展** - 模块解耦、易于集成智能体
✅ **Java 17特性** - 使用Record、Sealed Classes等现代特性
✅ **Spring生态最佳实践** - 完整的DDD设计、Advisor模式

---

## API接口文档

### 1. 发送聊天消息

**端点:** `POST /api/chatbot/chat`

**请求体:**
```json
{
  "sessionId": "session_xxx",  // 可选，为空则创建新会话
  "userId": "user_001",         // 必填，用户标识
  "message": "你好，请介绍一下你自己",  // 必填，用户消息
  "streaming": true,            // 可选，是否流式响应，默认true
  "sessionTitle": "新会话",     // 可选，新建会话时的标题
  "config": {
    "provider": "openai",       // 可选，LLM提供者
    "model": "deepseek-v3",     // 可选，模型名称
    "temperature": 0.7,         // 可选，温度参数
    "maxTokens": 2000,          // 可选，最大令牌数
    "systemPrompt": "你是...",   // 可选，系统提示词
    "memoryMode": "short_term",  // 可选，记忆模式
    "enableStreaming": true,    // 可选，启用流式
    "contextWindowSize": 10     // 可选，上下文窗口大小
  }
}
```

**响应体:**
```json
{
  "sessionId": "session_abc123",
  "messageId": "msg_def456",
  "content": "我是一个AI助手...",
  "status": "success",
  "tokenCount": 50,
  "messageCount": 2,
  "totalTokens": 120,
  "processingTime": 1250
}
```

**状态码:**
- `200` - 成功
- `400` - 请求参数错误
- `500` - 服务器错误

---

### 2. 创建新会话

**端点:** `POST /api/chatbot/sessions`

**请求体:**
```json
{
  "userId": "user_001",
  "title": "我的新会话"
}
```

**响应体:**
```json
{
  "sessionId": "session_abc123",
  "userId": "user_001",
  "title": "我的新会话",
  "status": "active",
  "createdAt": "2025-11-26T09:30:00",
  "lastAccessAt": "2025-11-26T09:30:00",
  "messageCount": 0,
  "totalTokens": 0
}
```

---

### 3. 获取用户会话列表

**端点:** `GET /api/chatbot/sessions/{userId}`

**路径参数:**
- `userId`: 用户ID

**响应体:**
```json
[
  {
    "sessionId": "session_abc123",
    "userId": "user_001",
    "title": "会话1",
    "status": "active",
    "createdAt": "2025-11-26T09:30:00",
    "lastAccessAt": "2025-11-26T10:00:00",
    "messageCount": 5,
    "totalTokens": 250
  },
  {
    "sessionId": "session_def456",
    "userId": "user_001",
    "title": "会话2",
    "status": "archived",
    "createdAt": "2025-11-26T08:30:00",
    "lastAccessAt": "2025-11-26T09:30:00",
    "messageCount": 10,
    "totalTokens": 450
  }
]
```

---

### 4. 获取会话详情

**端点:** `GET /api/chatbot/sessions/{sessionId}/detail`

**路径参数:**
- `sessionId`: 会话ID

**响应体:**
```json
{
  "sessionId": "session_abc123",
  "userId": "user_001",
  "title": "我的会话",
  "description": "关于天气的讨论",
  "status": "active",
  "createdAt": "2025-11-26T09:30:00",
  "lastAccessAt": "2025-11-26T10:00:00",
  "messageCount": 5,
  "totalTokens": 250
}
```

---

### 5. 获取会话的消息历史

**端点:** `GET /api/chatbot/sessions/{sessionId}/messages`

**路径参数:**
- `sessionId`: 会话ID

**响应体:**
```json
[
  {
    "messageId": "msg_001",
    "role": "user",
    "content": "你好",
    "timestamp": "2025-11-26T09:30:00",
    "tokenCount": 10
  },
  {
    "messageId": "msg_002",
    "role": "assistant",
    "content": "你好！我是AI助手...",
    "timestamp": "2025-11-26T09:30:05",
    "tokenCount": 40
  }
]
```

---

### 6. 更新会话配置

**端点:** `PUT /api/chatbot/sessions/{sessionId}/config`

**路径参数:**
- `sessionId`: 会话ID

**请求体:**
```json
{
  "provider": "ollama",
  "model": "local_model",
  "temperature": 0.5,
  "maxTokens": 1500,
  "memoryMode": "long_term"
}
```

**响应:**
- `200` - 成功
- `404` - 会话不存在

---

### 7. 清除会话对话历史

**端点:** `DELETE /api/chatbot/sessions/{sessionId}/messages`

**路径参数:**
- `sessionId`: 会话ID

**响应:**
- `200` - 成功
- `404` - 会话不存在

---

### 8. 归档会话

**端点:** `PUT /api/chatbot/sessions/{sessionId}/archive`

**路径参数:**
- `sessionId`: 会话ID

**响应:**
- `200` - 成功

---

### 9. 删除会话

**端点:** `DELETE /api/chatbot/sessions/{sessionId}`

**路径参数:**
- `sessionId`: 会话ID

**响应:**
- `200` - 成功

---

### 10. 获取系统统计信息

**端点:** `GET /api/chatbot/stats`

**响应体:**
```json
{
  "activeSessionCount": 15,
  "totalSessionCount": 42,
  "timestamp": "2025-11-26T10:00:00"
}
```

---

### 11. 健康检查

**端点:** `GET /api/chatbot/health`

**响应体:**
```json
{
  "status": "UP",
  "timestamp": "2025-11-26T10:00:00"
}
```

---

## 数据模型

### ChatMessage（聊天消息）
```java
{
  messageId: String,        // 消息ID
  role: String,             // 角色: user, assistant, system
  content: String,          // 消息内容
  timestamp: LocalDateTime, // 时间戳
  metadata: String,         // 元数据
  tokenCount: Integer       // 令牌数
}
```

### ChatSession（聊天会话）
```java
{
  sessionId: String,              // 会话ID
  userId: String,                 // 用户ID
  title: String,                  // 标题
  description: String,            // 描述
  status: String,                 // 状态: active, archived, deleted
  createdAt: LocalDateTime,       // 创建时间
  updatedAt: LocalDateTime,       // 更新时间
  lastAccessAt: LocalDateTime,    // 最后访问时间
  messages: List<ChatMessage>,    // 消息列表
  config: SessionConfig,          // 会话配置
  messageCount: Integer,          // 消息数
  totalTokens: Long               // 总令牌数
}
```

### SessionConfig（会话配置）
```java
{
  provider: String,           // LLM提供者: openai, ollama
  model: String,              // 模型名称
  temperature: Double,        // 温度 (0-1)
  maxTokens: Integer,         // 最大令牌数
  systemPrompt: String,       // 系统提示词
  memoryMode: String,         // none, short_term, long_term
  enableStreaming: Boolean,   // 是否启用流式响应
  contextWindowSize: Integer  // 上下文窗口大小
}
```

---

## 配置说明

在 `application.yml` 中配置：

```yaml
metaloom:
  chatbot:
    enabled: true                              # 是否启用
    session-timeout: 3600                      # 会话超时（秒）
    max-sessions-per-user: 50                  # 单用户最大会话数
    default-provider: "openai"                 # 默认提供者
    default-model: "deepseek-v3"               # 默认模型
    default-temperature: 0.7                   # 默认温度
    default-max-tokens: 2000                   # 默认最大令牌
    default-context-window-size: 10            # 默认上下文窗口
    default-memory-mode: "short_term"          # 默认记忆模式
    enable-streaming: true                     # 启用流式
    enable-memory: true                        # 启用记忆
    enable-persistence: true                   # 启用持久化
    persistence-type: "memory"                 # memory 或 database
    system-prompt: "你是一个有帮助的AI助手..." # 系统提示词
```

---

## 使用场景

### 场景1：创建新会话并发送消息
```bash
# 1. 创建会话
POST /api/chatbot/sessions
{
  "userId": "user_001",
  "title": "我的会话"
}

# 响应: sessionId = "session_abc123"

# 2. 发送消息（此时sessionId可以为空，会自动创建）
POST /api/chatbot/chat
{
  "userId": "user_001",
  "message": "你好"
  // 第一条消息时可以不指定sessionId
}
```

### 场景2：继续现有会话的对话
```bash
POST /api/chatbot/chat
{
  "sessionId": "session_abc123",
  "userId": "user_001",
  "message": "继续上面的话题..."
}
```

### 场景3：自定义会话配置
```bash
POST /api/chatbot/chat
{
  "userId": "user_001",
  "message": "用Ollama回答我",
  "config": {
    "provider": "ollama",
    "model": "local_model",
    "temperature": 0.5
  }
}
```

### 场景4：获取对话历史
```bash
GET /api/chatbot/sessions/session_abc123/messages
```

---

## 错误处理

所有错误响应使用标准格式：

```json
{
  "status": "error",
  "errorMessage": "错误描述信息"
}
```

### 常见错误码

| 错误 | 说明 |
|------|------|
| `INVALID_USER_ID` | 无效的用户ID |
| `INVALID_MESSAGE` | 消息内容不能为空 |
| `SESSION_NOT_FOUND` | 会话不存在 |
| `SESSION_EXPIRED` | 会话已过期 |
| `LLM_ERROR` | LLM调用失败 |

---

## 设计架构

```
┌─────────────────────────────────────────────┐
│         前端应用 (Vue 3 + Vite)              │
└───────────────┬─────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────┐
│     ChatBotController (REST接口层)           │
└───────────────┬─────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────┐
│  ChatService (业务服务层)                    │
├─────────────────────────────────────────────┤
│ • 多轮对话管理                               │
│ • 消息处理与提示词构建                       │
│ • 上下文管理                                 │
└───────────────┬─────────────────────────────┘
                │
        ┌───────┴──────────┬────────────────┐
        ▼                  ▼                ▼
  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
  │ChatSession   │  │ChatMemory    │  │ChatClientFactory
  │Service       │  │Store         │  │(LLM客户端)
  │(会话管理)     │  │(记忆存储)    │  │
  └──────────────┘  └──────────────┘  └──────────────┘
```

---

## 可扩展性指南

### 添加数据库持久化

实现 `ChatMemoryStore` 接口，创建 `DatabaseChatMemoryStore`：

```java
@Component
public class DatabaseChatMemoryStore implements ChatMemoryStore {
    @Autowired
    private ChatMessageRepository messageRepo;
    // 实现接口方法...
}
```

### 集成智能体系统

通过 `ChatService` 的 `customConfig` 参数：

```java
// 在ChatService中调用Agent
if (config.getMemoryMode().equals(MemoryMode.LONG_TERM)) {
    // 触发Agent进行对话摘要或关键信息提取
    String summary = agentService.summarizeConversation(sessionId);
    memoryStore.saveSummary(sessionId, summary);
}
```

### 添加自定义Advisor

在 `AiChatClientAutoConfiguration` 中注册新的Advisor：

```java
.defaultAdvisors(
    new SimpleLoggerAdvisor(),
    new MessageChatMemoryAdvisor(chatMemory),
    new CustomBusinessAdvisor()  // 自定义业务逻辑
)
```

---

## 测试

运行所有测试：
```bash
mvn test -pl metaloom-ai
```

运行特定测试：
```bash
mvn test -pl metaloom-ai -Dtest=ChatSessionServiceTest
mvn test -pl metaloom-ai -Dtest=InMemoryChatMemoryStoreTest
mvn test -pl metaloom-ai -Dtest=ChatBotIntegrationTest
```

---

## 最佳实践

1. **会话管理** - 总是提供正确的 `userId` 和 `sessionId`
2. **消息长度** - 控制单条消息不超过模型的token限制
3. **温度参数** - 回答性问题用 0.3-0.5，创意问题用 0.7-0.9
4. **上下文窗口** - 根据模型能力调整 `contextWindowSize`
5. **错误处理** - 使用统一的错误响应格式处理错误

---

## 许可证

MIT License
