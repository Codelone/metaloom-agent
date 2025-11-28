---
trigger: always_on
---

请依据项目已有依赖（项目 package.json 位于：/mnt/data/package.json）来生成代码并输出为项目文件结构。实现要点、UI 风格与交付物、API 契约、校验标准、以及代码要求都在下面，严格按要求交付代码文件（含 README、运行指令、关键实现说明）。

## 一、总体目标
实现一个**简约、技术感**的聊天前端，目标用户为数据分析人员。UI 简洁、信息密度高、交互高效。前端需支持本地持久化历史、会话管理、Markdown 渲染与代码高亮、流式响应（SSE 或 WebSocket）以及基本的导出功能。不要加入与后端无关的复杂样式或动画。

## 二、技术栈与约束
- 框架：Vue 3 + TypeScript（Composition API）
- 打包：Vite
- UI：Element Plus（已在 /mnt/data/package.json 中声明）
- 状态管理：Pinia（持久化）
- HTTP：axios；支持 SSE / WebSocket 流式接口（按后端可用性）
- Markdown：marked + highlight.js（或等价）
- 代码风格：严格的 TypeScript 类型注解、清晰注释、模块化目录
- 适配依赖：**必须**依据 `/mnt/data/package.json` 中现有依赖生成代码（不要额外引入大型无必要依赖）

## 三、UI 风格（Design tokens）
面向数据分析人员，视觉要求：简约、技术感、低噪音、信息优先。
- 主色 Primary: `#3366FF`
- 辅助色 Accent: `#00BFA6`
- 危险色 Danger: `#FF5C6A`
- 页面背景: `#F7F8FA`
- 卡片/气泡: `#FFFFFF`
- 文字：主文字 `#111827`，次级 `#6B7280`
- 圆角：8px；基线间距 8 / 12 / 16
- 字体优先：`Inter, "Noto Sans SC", "Microsoft YaHei", sans-serif`

UI 组件遵循最小化设计：干净卡片、可折叠摘要、代码块清晰展示、消息状态（发送中/失败/已发送）可见、每条消息展示时间戳。不要添加表情或装饰性图形。

## 四、核心功能（必须实现）
1. 聊天界面（消息流）  
   - 支持 Markdown 渲染与代码高亮；支持图片和链接渲染。
   - 用户消息与机器人消息样式区分（用户右侧、机器人左侧）。
   - 支持消息状态标识（sending/sent/failed）与重试按钮。

2. 历史会话侧边栏  
   - 列表、创建、重命名、删除、置顶、导出会话 JSON/Markdown。  
   - 搜索会话（按标题/内容）。

3. 会话存储与持久化  
   - 使用 Pinia + 持久化到 localStorage（或 IndexedDB），并提供“是否保存会话”开关。

4. 流式输出支持  
   - 支持后端 SSE（/api/chat/stream）优先；如果不可用，实现 WebSocket 回退或 chunk 模拟。
   - 在流式模式下前端应实现增量显示（typing / partial chunks）。

5. 后端 API 契约（前端按此与后端交互）
   - POST `/api/chat/send`  
     请求 body:
     ```json
     {
       "conversationId":"string",
       "message":"string",
       "options":{"stream":false}
     }
     ```
     响应（成功）:
     ```json
     {
       "type":"message",
       "content":"assistant reply markdown",
       "format":"markdown",
       "meta":{"messageId":"uuid","timestamp":"ISO8601"},
       "actions":[{"label":"...","payload":{}}]
     }
     ```
   - SSE `/api/chat/stream`：接收同样 body，通过多次 JSON chunk 发送增量 `{type, content, format, meta}`，以 `[DONE]` 收尾。
   - GET `/api/conversations`：返回会话列表（分页可选）。
   - POST `/api/conversations/export`：导出会话（可选，前端也应支持本地导出）。

6. 前端和模型之间的轻量消息约定（前端期望模型返回的 JSON 结构，以便渲染）：
   ```json
   {
     "type":"message",
     "content":"markdown string",
     "format":"markdown",
     "meta":{"messageId":"uuid","timestamp":"ISO8601","summary":"..." },
     "actions":[{"type":"suggested","label":"继续","payload":{"op":"continue"}}]
   }

7. 交互与可用性细节

   * 输入框支持 Enter 发送，Shift+Enter 换行；支持快捷操作（插入代码块、上传文件）。
   * 建议回复（最多 3 个）以按钮形式展现。
   * 键盘快捷键：Ctrl/Cmd+K 打开会话搜索、↑ 编辑上一条消息。

## 五、交付物（必须在项目中生成以下文件/说明）

* `src/main.ts`、`src/App.vue`（布局：左侧历史、右侧聊天、顶部工具栏）
* `src/components/ChatPanel.vue`（消息流、composer）
* `src/components/SidebarHistory.vue`（历史会话）
* `src/stores/chatStore.ts`（Pinia store，含持久化）
* `src/services/api.ts`（axios 封装 + SSE/WS helper）
* `src/styles/variables.css`（设计 tokens）与全局样式 `src/styles/global.css`
* `src/utils/markdown.ts`（marked + highlight.js 集成）
* 一个 README.md：包含运行说明（基于 `/mnt/data/package.json`）、说明如何接入后端 API、接受标准与校验点
* 组件/文件需含 TypeScript 类型声明（例如 `types/chat.ts`）

## 六、验收标准（必须满足）

* 能在本地 `npm install`（依据 /mnt/data/package.json）并 `npm run dev` 启动（提供 README 中的命令）。
* 聊天能发送消息并显示后端返回（支持普通与流式两种模式，至少实现前端的流式增量显示逻辑）。
* 会话历史可创建/切换/删除，且重启后依旧存在（说明持久化工作正常）。
* Markdown 与代码高亮渲染正常（示例：三段代码演示）。
* 提供一套简单的 mock 后端（或 mock 模式说明）供本地验证流式与非流式行为。
* 代码结构清晰、模块化、注释充分。

## 七、额外要求（增强，但非必须）

* 提供 `export as Markdown` 与 `export as JSON` 的前端实现。
* 一个基本的单元测试（例如对 store 的单测）会更好，但不是必须。

## 八、输出要求（交付给调用者的内容格式）

qoder 输出为项目文件（文件树），并在根目录生成 README.md 指导如何运行和如何接入真实后端。
在生成代码之后，给出一段「验收脚本/步骤」供人工验证（例如：启动、发送一条测试消息、模拟 SSE）。

## 九、实现说明与优先级（给 qoder 的开发顺序建议）

1. 先搭建项目基本骨架（main, App, styles, store）
2. 实现 ChatPanel 与 Markdown 渲染（包括代码高亮）
3. 实现 SidebarHistory 与持久化
4. 实现 api.ts（包括 SSE mock），并支持流式展示逻辑
5. 最后做可导出与 UX 打磨（suggested actions、重试等）

## 十、关于 package.json

请读取并依据 `package.json` 中已声明的库来生成代码（例如若 Element Plus、pinia、marked 已存在则直接使用；不要引入与之冲突的版本）。
