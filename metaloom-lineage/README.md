# 数据血缘智能体

基于Spring AI实现的数据血缘智能查询服务，支持深度血缘关系分析和查询。

## 功能特性

- 基于ReAct模式的智能血缘查询
- 支持上游和下游血缘关系查询
- 支持深度血缘查询，可递归分析数据流向
- 规整化的查询结果格式

## 实现方式

本模块通过以下方式与现有系统集成：

1. 使用 `ToolCallbackProviderService` 获取血缘查询工具，无需重新实现工具逻辑
2. 所有工具已经在 `metaloom-mcp-server` 中实现，并通过SSE方式在 `metaloom-tools` 中注入
3. 应用的启动类在 `metaloom-ai` 模块中，已通过POM引用本血缘模块

## 使用方法

### API接口

#### 血缘查询接口

**POST** `/api/lineage/query`

请求体示例：
```json
{
  "query": "查询表customer_order的上游血缘关系，最多查询3层",
  "depth": 3
}
```

响应示例：
```json
{
  "success": true,
  "message": "血缘查询成功",
  "result": "表customer_order的上游血缘关系如下：\n\n第1层：\n- customer_info（客户信息表）\n- product_info（产品信息表）\n\n第2层：\n- user_account（用户账户表）← customer_info\n- inventory（库存表）← product_info\n\n第3层：\n- authentication（认证表）← user_account\n- supplier（供应商表）← inventory",
  "instId": "customer_order",
  "depth": 3
}
```

## 架构设计

血缘智能体采用ReAct（Reasoning + Acting）模式实现，工作流程如下：

1. **思考**：分析用户查询，提取实例ID、查询深度和查询方向（上游/下游）
2. **行动**：调用相应的血缘查询工具获取数据
3. **观察**：分析返回结果，进行深度查询或结果整合

本模块不包含工具的具体实现，只负责调用已有的工具并进行智能化处理。
