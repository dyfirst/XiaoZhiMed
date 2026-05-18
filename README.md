# XiaoZhiMed - 智能医疗导诊与预约助手

一个基于 Spring Boot + Vue 3 + LangChain4j 的 AI 医疗导诊与预约系统。系统围绕“症状咨询 -> 科室推荐 -> 医生/排班查询 -> 预约落地”这条链路，提供医疗知识问答、预约业务工具调用和多轮会话记忆。

## 技术栈

**后端**
- Java 17 / Spring Boot 3.2.6
- LangChain4j 1.13.0-beta（LLM 集成、Tool Calling、RAG）
- DashScope 通义千问（`qwen-plus` / `qwen3.5-flash` / `text-embedding-v4`）
- Pinecone 向量数据库
- MyBatis-Plus + MySQL（预约数据）
- MongoDB（对话记忆持久化）
- Spring WebFlux（流式输出）

**前端**
- Vue 3 + TypeScript + Vite
- Pinia 状态管理
- Element Plus 组件库
- marked（Markdown 渲染）

## 当前架构

```text
用户 -> Vue 3 前端 -> Spring Boot 后端
                         |
                         v
                qwen3.5-flash 意图路由
                         |
        +----------------+----------------+
        |                |                |
        v                v                v
      TOOL             RAG              CHAT
        |                |                |
        v                v                v
   预约工具链      Pinecone + qwen-plus   普通对话
        |
        v
  MySQL 预约数据

MongoDB 用于持久化多轮会话记忆
```

## 核心能力

### 1. AI 智能导诊与流式对话
- 基于通义千问提供医疗咨询和科室推荐
- 流式输出回复
- 支持多会话记忆
- 同一 `memberId` 在上一轮流式响应结束前禁止重入，避免消息顺序错乱

### 2. 前置意图路由
- 使用 `qwen3.5-flash` 做前置三路由：`TOOL / RAG / CHAT`
- 规则优先，模型兜底
- `TOOL`：预约、取消、查个人预约、确认等业务操作
- `RAG`：医院、科室、医生、挂号规则、导诊知识查询
- `CHAT`：普通对话、闲聊、非检索非工具场景

### 3. RAG 知识增强
- 知识库位于 `src/main/resources/knowledge_base`
- 包含医院信息、科室资料、医生资料
- 使用 `text-embedding-v4` 生成向量并存入 Pinecone
- 当前检索参数：`maxResults=3`、`minScore=0.7`
- 是否进入 RAG 由前置意图路由决定，`contentRetriever` 只负责检索

### 4. Tool Calling 预约管理
- 支持预约挂号、取消预约、查询号源
- 支持查询“我的预约记录”
- 预约前会收集姓名、身份证号、科室、日期、时间等必要字段
- 预约数据持久化到 MySQL

### 5. 会话记忆与提示词
- MongoDB 存储每个用户的对话历史
- 当前滑动窗口保留最近 50 条消息
- 系统提示词按 UTF-8 显式读取，避免 Windows 默认编码导致乱码
- 当前日期在后端显式替换 `{{current_date}}` 后再传给模型

## 项目结构

```text
xiaozhiMed
├── src/main/java/com/example/xiaozhimed
│   ├── assistant/          # AI Agent 定义（主Agent、路由Agent、工具/普通对话Agent）
│   ├── bean/               # 请求/响应对象、路由结果对象
│   ├── config/             # 模型、RAG、记忆配置
│   ├── controller/         # REST 接口
│   ├── entity/             # 数据库实体
│   ├── mapper/             # MyBatis Mapper
│   ├── service/            # 业务服务、意图路由服务
│   ├── store/              # MongoDB 聊天存储
│   └── tools/              # 预约、排班、预约记录查询工具
├── src/main/resources
│   ├── application.yaml
│   ├── knowledge_base/     # 医院 / 科室 / 医生知识库
│   ├── mapper/
│   └── xiaozhi-prompt-template.txt
├── frontend/
└── pom.xml
```

## 运行环境

- JDK 17
- Maven 3.9+
- MySQL 8.x
- MongoDB 7.x
- Node.js 18+（前端）

## 环境变量

启动前需设置：

```bash
export DASHSCOPE_API_KEY=你的阿里百炼API Key
export PINECONE_API_KEY=你的Pinecone API Key
```

## 启动项目

**后端**

```bash
mvnw.cmd spring-boot:run
```

**前端**

```bash
cd frontend
npm install
npm run dev
```

## 访问地址

| 地址 | 说明 |
|------|------|
| `http://localhost:5173` | 前端页面 |
| `http://localhost:8080/doc.html` | Swagger API 文档 |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/xiaozhi/chat` | AI 对话（流式） |
| GET | `/appointments` | 查询预约列表 |
| GET | `/appointments/{id}` | 查询单个预约 |
| POST | `/appointments` | 创建预约 |
| DELETE | `/appointments/{id}` | 删除预约 |

## 当前对话链路

```text
用户消息
-> 后端读取并补全系统提示词
-> qwen3.5-flash 前置路由
   -> TOOL: 工具优先
   -> RAG: 知识检索 + 主模型
   -> CHAT: 普通回答
-> MongoDB 持久化会话记忆
```

## 调试与评估

- 控制器回归测试：

```bash
mvn -Dtest=XiaozhiControllerTest test
```

- RAG 阈值评估测试：

```bash
mvn -Dtest=RAGTest#testMinScoreWithLabeledCases test
```

- 当前基于样本评估推荐的 `minScore` 为 `0.7`
