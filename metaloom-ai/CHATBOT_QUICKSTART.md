# ChatBot 快速开始指南

## 项目结构

```
metaloom-ai/
├── src/main/java/com/metaloom/ai/
│   └── chatbot/
│       ├── config/              # 配置类
│       │   └── ChatBotConfig.java
│       ├── constant/            # 常量定义
│       │   └── ChatBotConstants.java
│       ├── controller/          # REST控制器
│       │   └── ChatBotController.java
│       ├── dto/                 # 数据传输对象
│       │   ├── ChatRequestDTO.java
│       │   ├── ChatResponseDTO.java
│       │   ├── ChatSessionDTO.java
│       │   └── SessionConfigDTO.java
│       ├── memory/              # 记忆存储
│       │   ├── ChatMemoryStore.java (接口)
│       │   └── InMemoryChatMemoryStore.java (实现)
│       ├── model/               # 核心模型
│       │   ├── ChatMessage.java
│       │   ├── ChatSession.java
│       │   ├── ConversationContext.java
│       │   └── SessionConfig.java
│       └── service/             # 业务服务层
│           ├── ChatService.java
│           └── ChatSessionService.java
├── src/test/java/com/metaloom/ai/
│   └── chatbot/
│       ├── integration/         # 集成测试
│       │   └── ChatBotIntegrationTest.java
│       ├── memory/              # 内存测试
│       │   └── InMemoryChatMemoryStoreTest.java
│       └── service/             # 服务测试
│           └── ChatSessionServiceTest.java
├── src/main/resources/
│   ├── application.yml          # 配置文件
│   └── application-prod.yml     # 生产配置
├── CHATBOT_API.md               # API文档
└── CHATBOT_QUICKSTART.md        # 本文件
```

## 安装与依赖

### 前置要求
- Java 17+
- Maven 3.8+
- Spring Boot 3.4.3
- Spring AI 1.1

### Maven依赖

核心依赖已在 `metaloom-ai/pom.xml` 中配置：

```xml
<!-- Spring Boot Web Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring AI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-core</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## 快速启动

### 1. 编译项目

```bash
cd metaloom-agent
mvn clean compile -pl metaloom-ai
```

### 2. 运行测试

```bash
# 运行所有chatbot测试
mvn test -pl metaloom-ai -Dtest=*ChatBot*

# 运行特定测试
mvn test -pl metaloom-ai -Dtest=ChatSessionServiceTest
```

### 3. 启动应用

```bash
# 直接运行（需要配置application.yml）
mvn spring-boot:run -pl metaloom-ai

# 或构建后运行
mvn clean package -pl metaloom-ai -DskipTests
java -jar metaloom-ai/target/metaloom-ai-1.0-SNAPSHOT.jar
```

## 配置指南

### application.yml 配置

```yaml
server:
  port: 8080

metaloom:
  chatbot:
    # 基本设置
    enabled: true
    session-timeout: 3600        # 单位：秒
    max-sessions-per-user: 50
    
    # LLM默认配置
    default-provider: "openai"
    default-model: "deepseek-v3"
    default-temperature: 0.7
    default-max-tokens: 2000
    
    # 对话管理
    default-context-window-size: 10  # 保留最近10条消息
    default-memory-mode: "short_term"  # none|short_term|long_term
    
    # 功能开关
    enable-streaming: true        # 流式响应
    enable-memory: true           # 记忆功能
    enable-persistence: true      # 对话历史持久化
    persistence-type: "memory"    # memory|database
    
    # 系统提示词
    system-prompt: "你是一个有帮助的AI助手。请根据用户的问题提供准确、清晰和有用的回答。"

# LLM配置（来自metaloom-model）
spring:
  ai:
    openai:
      api-key: ${AI_API_KEY:your_api_key}
      base-url: https://chatapi.littlewheat.com
```

### 环境变量

```bash
# 设置LLM API密钥
export AI_API_KEY=sk-xxxxx

# 运行应用
mvn spring-boot:run -pl metaloom-ai
```

## 使用示例

### 示例1：创建会话并发送消息

```bash
# 创建新会话
curl -X POST http://localhost:8080/api/chatbot/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_001",
    "title": "我的第一个会话"
  }'

# 响应获得 sessionId: "session_abc123"

# 发送消息
curl -X POST http://localhost:8080/api/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "session_abc123",
    "userId": "user_001",
    "message": "你好，请介绍一下你自己"
  }'
```

### 示例2：获取对话历史

```bash
curl -X GET http://localhost:8080/api/chatbot/sessions/session_abc123/messages \
  -H "Content-Type: application/json"
```

### 示例3：使用自定义配置

```bash
curl -X POST http://localhost:8080/api/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_001",
    "message": "用Ollama本地模型回答我",
    "config": {
      "provider": "ollama",
      "model": "qwen:7b",
      "temperature": 0.5,
      "enableStreaming": false
    }
  }'
```

### 示例4：更新会话配置

```bash
curl -X PUT http://localhost:8080/api/chatbot/sessions/session_abc123/config \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "openai",
    "model": "gpt-4",
    "temperature": 0.8,
    "memoryMode": "long_term"
  }'
```

## 核心类说明

### ChatBotController
REST接口入口，处理HTTP请求并路由到业务服务。

**主要方法:**
- `POST /chat` - 发送聊天消息
- `POST /sessions` - 创建新会话
- `GET /sessions/{userId}` - 获取用户会话列表
- `GET /sessions/{sessionId}/detail` - 获取会话详情
- `GET /sessions/{sessionId}/messages` - 获取消息历史
- `PUT /sessions/{sessionId}/config` - 更新会话配置
- `DELETE /sessions/{sessionId}/messages` - 清除对话历史
- `PUT /sessions/{sessionId}/archive` - 归档会话
- `DELETE /sessions/{sessionId}` - 删除会话
- `GET /stats` - 获取统计信息
- `GET /health` - 健康检查

### ChatService
核心业务逻辑层，处理多轮对话、LLM调用、上下文管理。

**主要方法:**
```java
// 发送消息（支持创建新会话或继续现有会话）
ChatMessage sendMessage(String sessionId, String userId, String message, SessionConfig customConfig)

// 获取对话历史
List<ChatMessage> getConversationHistory(String sessionId)

// 获取最近N条消息
List<ChatMessage> getRecentMessages(String sessionId, int count)

// 获取会话详情
ChatSession getSessionDetail(String sessionId)

// 清除对话历史
void clearConversationHistory(String sessionId)

// 记忆相关
void saveKeyInformation(String sessionId, String key, String value)
Map<String, String> getKeyInformation(String sessionId)
```

### ChatSessionService
会话生命周期管理服务。

**主要方法:**
```java
// 创建会话
ChatSession createSession(String userId, String title, SessionConfig config)

// 获取会话
Optional<ChatSession> getSession(String sessionId)

// 获取用户会话
List<ChatSession> getUserActiveSessions(String userId)
List<ChatSession> getUserAllSessions(String userId)

// 更新会话
void updateSessionConfig(String sessionId, SessionConfig config)
void updateSessionStats(String sessionId, int messageCount, long tokenCount)

// 会话状态管理
void archiveSession(String sessionId)
void restoreSession(String sessionId)
void deleteSession(String sessionId)

// 会话清理
void clearUserSessions(String userId)
```

### ChatMemoryStore / InMemoryChatMemoryStore
消息存储和记忆管理。

**主要方法:**
```java
// 消息管理
void saveMessage(String sessionId, ChatMessage message)
void saveMessages(String sessionId, List<ChatMessage> messages)
List<ChatMessage> getMessages(String sessionId)
List<ChatMessage> getRecentMessages(String sessionId, int count)

// 摘要和关键信息存储
void saveSummary(String sessionId, String summary)
Optional<String> getSummary(String sessionId)
void saveKeyInformation(String sessionId, String key, String value)
Map<String, String> getAllKeyInformation(String sessionId)

// 数据清理
void clearMessages(String sessionId)
void clearSessionData(String sessionId)
```

## 开发指南

### 添加新的API端点

在 `ChatBotController` 中添加方法：

```java
@PostMapping("/your-endpoint")
public ResponseEntity<YourResponse> yourMethod(@RequestBody YourRequest request) {
    try {
        // 业务逻辑
        YourResponse response = chatService.yourMethod(request);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        log.error("Error", e);
        return ResponseEntity.internalServerError().build();
    }
}
```

### 自定义记忆存储

继承 `ChatMemoryStore` 接口实现数据库存储：

```java
@Component
public class DatabaseChatMemoryStore implements ChatMemoryStore {
    @Autowired
    private ChatMessageRepository messageRepo;
    
    @Override
    public void saveMessage(String sessionId, ChatMessage message) {
        // 持久化到数据库
        ChatMessageEntity entity = mapper.toEntity(message);
        messageRepo.save(entity);
    }
    
    // 实现其他方法...
}
```

### 集成自定义业务逻辑

通过扩展 `ChatService` 或在 Advisor 中添加：

```java
@Component
public class CustomBusinessAdvisor implements Advisor {
    @Override
    public AiResponse before(AiRequest request) {
        // 请求前处理（如敏感词过滤）
        return request.getAiResponse();
    }
    
    @Override
    public AiResponse after(AiResponse response) {
        // 响应后处理（如数据统计）
        return response;
    }
}
```

## 测试指南

### 运行单元测试

```bash
# ChatSessionService 测试 - 15个测试用例
mvn test -pl metaloom-ai -Dtest=ChatSessionServiceTest

# InMemoryChatMemoryStore 测试 - 17个测试用例
mvn test -pl metaloom-ai -Dtest=InMemoryChatMemoryStoreTest

# 集成测试 - 7个测试用例
mvn test -pl metaloom-ai -Dtest=ChatBotIntegrationTest
```

### 测试覆盖

- ✅ 会话创建与获取
- ✅ 多轮对话流程
- ✅ 消息存储与检索
- ✅ 会话状态转换
- ✅ 记忆功能
- ✅ 配置管理
- ✅ 错误处理

## 性能优化建议

1. **使用Redis缓存** - 替代内存存储提升并发
2. **异步消息处理** - 使用 `@Async` 处理长耗时操作
3. **批量操作** - 合并多个消息保存操作
4. **连接池** - 优化数据库连接池配置
5. **监控指标** - 集成Prometheus监控性能

## 常见问题

### Q1: 如何支持流式响应？
A: 在请求中设置 `streaming: true`，在 `SessionConfig` 中启用 `enableStreaming`。

### Q2: 如何切换LLM模型？
A: 通过 `config` 字段指定不同的 `provider` 和 `model`，或在 `application.yml` 中配置默认值。

### Q3: 如何实现长期记忆？
A: 设置 `memoryMode: "long_term"`，实现数据库版本的 `ChatMemoryStore`。

### Q4: 会话超时如何处理？
A: 通过定时任务清理超时会话，可扩展 `ChatSessionService`。

### Q5: 如何集成智能体系统？
A: 在 `ChatService.sendMessage()` 中判断 `memoryMode`，调用Agent服务进行对话分析或摘要。

## 后续扩展方向

1. **数据库持久化** - 从内存存储迁移到MySQL/PostgreSQL
2. **Redis缓存** - 提升高并发场景下的性能
3. **向量数据库** - 集成Milvus/Pinecone实现语义搜索
4. **智能体集成** - 对接元数据Agent和血缘Agent
5. **WebSocket支持** - 实现实时流式消息推送
6. **用户认证** - 集成Spring Security进行权限管理
7. **审计日志** - 记录所有对话用于合规性审计
8. **多言语支持** - 支持多语言自动翻译

## 相关文档

- [API文档](./CHATBOT_API.md)
- [项目架构](../core-architecture/)
- [Spring AI官方文档](https://spring.io/projects/spring-ai)
- [Spring Boot 3.4.3文档](https://docs.spring.io/spring-boot/docs/3.4.3/reference/html/)

## 许可证

MIT License
