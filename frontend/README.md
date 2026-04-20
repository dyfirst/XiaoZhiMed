# 小智医疗前端

该目录是独立前端工程，不参与 Maven 构建，也不会修改后端代码。

## 技术栈

- Vue 3
- Vite
- TypeScript
- Vue Router
- Pinia
- Element Plus
- Vitest
- Playwright

## 页面说明

- `/chat`：流式聊天主界面，支持会话切换、手动打断、重试上次回答、memberId 绑定。
- `/appointments`：预约列表、新增预约、删除预约。

## API 约定

- 开发环境默认代理 `/api/*` 到 `http://127.0.0.1:8080/*`
- 聊天接口：`POST /xiaozhi/chat`
- 预约接口：`GET/POST/DELETE /appointments`

## 启动方式

在 `frontend` 目录执行：

```powershell
& 'E:\develope\nodejs\npm.cmd' install
& 'E:\develope\nodejs\npm.cmd' run dev
```

默认打开地址：

```text
http://127.0.0.1:5173
```

## 测试命令

```powershell
& 'E:\develope\nodejs\npm.cmd' run test:unit
& 'E:\develope\nodejs\npm.cmd' run test:e2e
```

## 鉴权说明

页面右上角提供 Bearer Token 输入框。当前后端没有登录接口时，可以留空；后续如果接入认证，`request` 会自动把 token 注入 `Authorization` 请求头。
