# MetaloomAI Chatbot Frontend

这是一个基于 Vue 3 + TypeScript + Element Plus 的聊天机器人前端项目，专为数据分析人员设计。

## 功能特性

- **简约 UI**: 专注于内容和交互，无干扰设计。
- **Markdown 支持**: 完整的 Markdown 渲染和代码高亮。
- **流式响应**: 支持 SSE (Server-Sent Events) 实时显示 AI 回复。
- **会话管理**: 本地持久化存储会话历史，支持创建、重命名、删除会话。
- **导出功能**: 支持导出会话为 JSON。

## 技术栈

- Vue 3 (Composition API)
- TypeScript
- Vite
- Pinia (状态管理 + 持久化)
- Element Plus (UI 组件库)
- Axios (HTTP 请求)
- Marked + Highlight.js (Markdown 渲染)

## 快速开始

### 前置要求

- Node.js >= 16
- 后端服务已启动 (默认端口 8080)

### 安装依赖

\`\`\`bash
npm install
\`\`\`

### 启动开发服务器

\`\`\`bash
npm run dev
\`\`\`

访问 `http://localhost:5173` 开始使用。

### 构建生产版本

\`\`\`bash
npm run build
\`\`\`

## 接入后端

前端通过 Vite 代理配置了 `/api` 转发到 `http://localhost:8080`。如果后端端口不同，请修改 `vite.config.ts` 中的 `proxy` 配置。

### API 契约

- `POST /api/chat/send`: 发送消息 (非流式)
- `GET /api/chat/stream`: 发送消息 (流式 SSE)
- `GET /api/conversations`: 获取会话列表 (可选)

## 目录结构

\`\`\`
src/
├── components/     # Vue 组件
├── services/       # API 服务
├── stores/         # Pinia 状态管理
├── styles/         # 全局样式和变量
├── utils/          # 工具函数 (Markdown 等)
├── App.vue         # 根组件
└── main.ts         # 入口文件
\`\`\`
