# XiaoZhiMed - 智能医疗问诊助手

一个基于 Spring Boot + Vue 3 + LangChain4j 的 AI 医疗问诊和预约挂号系统。通过大模型实现智能分诊、医疗咨询和预约管理。

## 技术栈

**后端**
- Java 17 / Spring Boot 3.2.6
- LangChain4j 1.13.0-beta（LLM 集成、Tool Calling、RAG）
- DashScope 通义千问（qwen-plus / text-embedding-v4）
- Pinecone 向量数据库
- MyBatis-Plus + MySQL（预约数据）
- MongoDB（对话记忆持久化）
- Spring WebFlux（流式输出）

**前端**
- Vue 3 + TypeScript + Vite
- Pinia 状态管理
- Element Plus 组件库
- marked（Markdown 渲染）

## 系统架构

```
用户 → Vue 3 前端 → Spring Boot 后端 → 通义千问 LLM
                         ↓
         ┌───────────────┼───────────────┐
         ↓               ↓               ↓
    Pinecone RAG    MongoDB 记忆     MySQL 预约
```

## 核心功能

### 1. AI 智能问诊（流式对话）
- 基于通义千问大模型的医疗咨询
- 流式输出，逐字显示回复
- 支持多会话管理，历史记录持久化
- Markdown 格式渲染

### 2. RAG 知识增强
- 医院信息、科室列表、医生资料等知识文档存入 Pinecone 向量库
- 文本分块（300 字/块，50 字重叠）后向量化
- 用户提问时自动召回相关知识，注入 LLM 上下文
- 召回日志记录，便于调试和优化

### 3. Tool Calling 预约管理
- LLM 自动判断何时调用预约工具
- 支持预约挂号、取消预约、查询号源
- 预约前自动收集完整信息（姓名、身份证、科室、日期、时间）
- 数据持久化到 MySQL

### 4. 对话记忆
- MongoDB 存储每个用户的对话历史
- 支持多用户独立记忆空间
- 滑动窗口机制，保留最近 20 条消息

### 5. 系统提示词工程
- 角色定义：医疗顾问 + 伴诊助手
- 流程约束：信息收集 → 号源查询 → 用户确认 → 执行预约
- 安全兜底：严重症状建议就医、禁止编造信息、免责声明
- 动态日期注入

## 项目结构

```
xiaozhiMed
├── src/main/java/com/example/xiaozhimed
│   ├── assistant/          # AI Agent 接口定义
│   ├── config/             # Bean 配置（LLM、RAG、记忆）
│   ├── controller/         # REST 接口
│   ├── entity/             # 数据库实体
│   ├── mapper/             # MyBatis Mapper
│   ├── service/            # 业务逻辑
│   ├── store/              # MongoDB 聊天存储
│   ├── tools/              # LLM 工具（预约、计算器）
│   └── bean/               # 请求/响应对象
├── src/main/resources
│   ├── application.yaml    # 配置文件
│   ├── knowledge/          # RAG 知识文档
│   ├── xiaozhi-prompt-template.txt  # 系统提示词
│   └── mapper/             # MyBatis XML
├── frontend/               # Vue 3 前端
│   ├── src/views/          # 页面组件
│   ├── src/components/     # 通用组件
│   ├── src/stores/         # Pinia 状态管理
│   ├── src/api/            # HTTP 请求封装
│   └── src/composables/    # 组合式函数
└── pom.xml
```

## 运行环境

- JDK 17
- Maven 3.9+
- MySQL 8.x
- MongoDB 7.x
- Node.js 18+（前端）

## 环境变量

启动前需设置以下环境变量：

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
