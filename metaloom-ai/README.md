# Metaloom AI - A2A智能体系统

## 概述

Metaloom AI是一个基于A2A（Agent-to-Agent）架构的数据分析智能体系统，采用ReAct模式让LLM自行决定调用哪个专家智能体来获取信息。

## 架构设计

### A2A架构
- **协调器（Orchestrator）**：负责协调多个专家智能体
- **专家智能体**：
  - 血缘分析智能体（Lineage Agent）：查询数据血缘关系、数据流向、依赖关系
  - 元数据查询智能体（Metadata Agent）：查询表结构、字段信息、数据字典

### ReAct模式
- **Reasoning**：LLM分析用户查询，决定下一步行动
- **Acting**：执行选定的智能体调用
- **循环**：基于结果继续推理，直到解决问题

## 功能特性

### 1. ReAct模式分析
- 让LLM自行决定调用哪个智能体
- 支持多轮迭代，逐步解决问题
- 自动构造智能体调用请求

### 2. 异步模式分析
- 并行调用多个智能体
- 整合多个智能体的结果
- 提高处理效率

### 3. 智能体管理
- 动态配置智能体
- 支持智能体的启用/禁用
- 超时控制和错误处理

## API接口

### 1. ReAct模式分析
```http
POST /api/analysis/react
Content-Type: application/json

{
    "query": "查询用户表的数据血缘关系"
}
```

### 2. 异步模式分析
```http
POST /api/analysis/async
Content-Type: application/json

{
    "query": "查询用户表的结构和血缘关系"
}
```

### 3. 状态查询
```http
GET /api/analysis/status
```

### 4. 健康检查
```http
GET /api/analysis/health
```

## 响应格式

### 分析结果
```json
{
    "originalQuery": "查询用户表的数据血缘关系",
    "steps": [
        {
            "iteration": 1,
            "llmResponse": "LLM的决策响应",
            "action": {
                "type": "CALL_AGENT",
                "agentName": "lineage_agent",
                "requestBody": "查询内容",
                "reasoning": "推理过程"
            },
            "actionResult": "智能体执行结果",
            "executionTime": 1500
        }
    ],
    "finalAnswer": "最终的分析结果",
    "processingTime": 5000,
    "success": true,
    "errorMessage": null
}
```

## 配置说明

### application.yml配置
```yaml
metaloom:
  a2a:
    enabled: true
    max-iterations: 5
    timeout: 30000
    available-agents:
      - lineage_agent
      - metadata_agent
    agents:
      lineage_agent:
        name: "血缘分析智能体"
        description: "用于查询数据血缘关系、数据流向、依赖关系等"
        enabled: true
        timeout: 10000
      metadata_agent:
        name: "元数据查询智能体"
        description: "用于查询表结构、字段信息、数据字典等元数据信息"
        enabled: true
        timeout: 10000
    llm:
      provider: "openai"
      model: "deepseek-v3"
      temperature: 0.7
      max-tokens: 2000
```

## 使用示例

### 1. 血缘关系查询
```bash
curl -X POST http://localhost:8080/api/analysis/react \
  -H "Content-Type: application/json" \
  -d '{
    "query": "查询用户表的数据血缘关系，包括上游和下游依赖"
  }'
```

### 2. 元数据查询
```bash
curl -X POST http://localhost:8080/api/analysis/react \
  -H "Content-Type: application/json" \
  -d '{
    "query": "查询用户表的字段结构，包括字段类型和注释"
  }'
```

### 3. 综合分析
```bash
curl -X POST http://localhost:8080/api/analysis/async \
  -H "Content-Type: application/json" \
  -d '{
    "query": "分析用户表的完整信息，包括结构、血缘关系和数据质量"
  }'
```

## 技术栈

- **Spring Boot 3.4.3**：应用框架
- **Spring AI 1.0**：AI集成框架
- **Java 17**：编程语言
- **Lombok**：代码简化
- **Jackson**：JSON处理
- **CompletableFuture**：异步处理

## 部署说明

### 1. 环境要求
- Java 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 启动应用
```bash
mvn spring-boot:run
```

### 3. 访问地址
- 应用地址：http://localhost:8080
- API文档：http://localhost:8080/api/analysis/status

## 扩展指南

### 添加新的专家智能体

1. 创建智能体类
```java
@Component
public class NewAgent {
    public NewResponse processQuery(NewRequest request) {
        // 实现智能体逻辑
    }
}
```

2. 在协调器中添加调用
```java
@Autowired
private NewAgent newAgent;

private String executeNewAgent(String query) {
    NewRequest request = NewRequest.builder()
        .query(query)
        .build();
    
    NewResponse response = newAgent.processQuery(request);
    return response.toString();
}
```

3. 更新配置
```yaml
metaloom:
  a2a:
    available-agents:
      - new_agent
    agents:
      new_agent:
        name: "新智能体"
        description: "新智能体的功能描述"
        enabled: true
```

## 监控和日志

### 日志级别
- INFO：正常操作日志
- WARN：警告信息
- ERROR：错误信息

### 关键指标
- 处理时间
- 迭代次数
- 智能体调用次数
- 成功率

## 故障排除

### 常见问题

1. **LLM响应解析失败**
   - 检查LLM配置
   - 验证响应格式
   - 查看日志详情

2. **智能体调用超时**
   - 调整超时配置
   - 检查网络连接
   - 验证智能体状态

3. **内存使用过高**
   - 调整线程池大小
   - 优化查询逻辑
   - 增加内存配置

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request

## 许可证

MIT License 