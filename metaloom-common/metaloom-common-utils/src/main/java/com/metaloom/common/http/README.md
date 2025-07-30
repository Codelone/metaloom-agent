# HTTP 请求工具类

本模块提供了简单易用的 HTTP 请求工具类，支持 GET、POST、PUT、DELETE 等常用 HTTP 方法。

## 主要类

### 1. HttpClientUtils
基础工具类，提供静态方法进行 HTTP 请求。

#### 使用方法：
```java
// GET 请求
String response = HttpClientUtils.get("https://api.example.com/data");

// POST 请求
Map<String, String> headers = new HashMap<>();
headers.put("Content-Type", "application/json");
String response = HttpClientUtils.post("https://api.example.com/data", jsonString, headers);
```

### 2. HttpClient
高级客户端类，提供链式调用的 Fluent API。

#### 创建客户端：
```java
// 默认配置
HttpClient client = HttpClient.getDefault();

// 自定义配置
HttpClient client = HttpClient.getDefault()
    .setConnectTimeout(5000)      // 连接超时时间（毫秒）
    .setReadTimeout(10000)        // 读取超时时间（毫秒）
    .setMaxRetries(3)             // 最大重试次数
    .addDefaultHeader("Authorization", "Bearer token123");
```

#### 发送请求：
```java
// GET 请求
HttpResponse<String> response = client.get("https://api.example.com/data");

// GET 带参数
Map<String, String> params = new HashMap<>();
params.put("page", "1");
params.put("size", "10");
HttpResponse<String> response = client.get("https://api.example.com/data", params);

// POST JSON 请求
Map<String, Object> data = new HashMap<>();
data.put("name", "张三");
data.put("age", 25);
HttpResponse<String> response = client.postJson("https://api.example.com/users", data);

// POST 原始字符串
HttpResponse<String> response = client.post("https://api.example.com/data", jsonString);

// 自定义请求头
Map<String, String> headers = new HashMap<>();
headers.put("X-Custom-Header", "value");
HttpResponse<String> response = client.get("https://api.example.com/data", null, headers);
```

### 3. HttpResponse
响应包装类，包含响应状态码、头部、消息体等信息。

#### 使用示例：
```java
HttpResponse<String> response = client.get("https://api.example.com/data");

if (response.isSuccess()) {
    String body = response.getBody();
    int statusCode = response.getStatusCode();
    Map<String, String> headers = response.getHeaders();
    String message = response.getMessage();
} else {
    System.err.println("请求失败: " + response.getStatusCode());
}
```

## 错误处理

所有方法都会抛出异常，建议使用 try-catch 块处理：

```java
try {
    HttpResponse<String> response = client.get("https://api.example.com/data");
    if (response.isSuccess()) {
        // 处理成功响应
    } else {
        // 处理错误响应
    }
} catch (Exception e) {
    // 处理网络异常
    System.err.println("请求失败: " + e.getMessage());
}
```

## 特性

- ✅ 支持 GET、POST、PUT、DELETE 方法
- ✅ 自动 JSON 序列化/反序列化
- ✅ 可配置超时时间
- ✅ 自动重试机制
- ✅ URL 参数编码
- ✅ 自定义请求头
- ✅ 响应状态码检查
- ✅ 详细的错误信息
- ✅ 线程安全

## 依赖

- **fastjson2**: JSON 序列化/反序列化
- **junit-jupiter**: 单元测试（测试范围）

## 运行示例

运行示例程序：
```bash
java com.metaloom.common.http.HttpClientExample
```

运行测试：
```bash
mvn test
```