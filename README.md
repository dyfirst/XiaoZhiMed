# xiaozhiMed

一个基于 Spring Boot 的练手项目，当前主要用于学习和验证以下内容：

- Spring Boot 基础项目搭建
- MyBatis 访问 MySQL
- RESTful 风格接口设计
- LangChain4j 接入大模型

## 技术栈

- Java 17
- Spring Boot 3.2.6
- MyBatis Spring Boot Starter 3.0.4
- MySQL
- Maven
- LangChain4j

## 当前功能

### 1. Employee RESTful 增删改查

基于 `sky_take_out.employee` 表实现了基础 CRUD 接口。

接口列表：

- `GET /employees` 查询全部员工
- `GET /employees/{id}` 按 id 查询员工
- `POST /employees` 新增员工
- `PUT /employees/{id}` 修改员工
- `DELETE /employees/{id}` 删除员工

### 2. 大模型接入测试

项目中已经接入了阿里百炼相关配置，当前用于测试：

- OpenAI 兼容接口调用
- DashScope Qwen 模型调用

测试类位置：

- `src/test/java/com/example/xiaozhimed/langchain4j/LLMTest.java`

## 运行环境

启动项目前请准备：

- JDK 17
- Maven 3.9+
- MySQL 8.x

## 数据库配置

当前默认连接的是本地 MySQL：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sky_take_out?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: 123456
```

请确保：

- 本地 MySQL 已启动
- 存在 `sky_take_out` 数据库
- 存在 `employee` 表
- 用户名和密码与本地环境一致

## 大模型配置

当前 `application.yaml` 中包含大模型配置。

建议不要将真实 API Key 明文提交到仓库，推荐改为环境变量形式，例如：

```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: ${DASHSCOPE_API_KEY}
        model-name: qwen-max
```

## 启动项目

在项目根目录执行：

```bash
mvnw.cmd spring-boot:run
```

启动成功后可访问接口，例如：

```text
http://localhost:8080/employees
```

## 运行测试

运行全部测试：

```bash
mvnw.cmd test
```

单独运行大模型测试：

```bash
mvnw.cmd -Dtest=LLMTest test
```

## 示例请求

新增员工：

```http
POST /employees
Content-Type: application/json

{
  "name": "测试员工",
  "username": "test01",
  "password": "123456",
  "phone": "13812345678",
  "sex": "1",
  "idNumber": "110101199001010011",
  "status": 1,
  "createUser": 1,
  "updateUser": 1
}
```

修改员工：

```http
PUT /employees/1
Content-Type: application/json

{
  "name": "管理员-修改",
  "username": "admin",
  "password": "123456",
  "phone": "13800000000",
  "sex": "1",
  "idNumber": "110101199001010047",
  "status": 1,
  "updateUser": 1
}
```

## 目录结构

```text
src
├─ main
│  ├─ java/com/example/xiaozhimed
│  │  ├─ controller
│  │  ├─ entity
│  │  ├─ mapper
│  │  └─ service
│  └─ resources
│     └─ application.yaml
└─ test
   └─ java/com/example/xiaozhimed
      ├─ controller
      └─ langchain4j
```

## 说明

这个项目目前处于学习和迭代阶段，后续可以继续补充：

- 统一返回结果封装
- DTO / VO 分层
- 分页查询
- 登录认证
- AI 聊天接口封装
