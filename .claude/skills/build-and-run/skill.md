---
name: build-and-run
description: 编译并启动前后端项目。前端是 Vue+Vite 项目，后端是 Spring Boot Maven 项目。Use when 用户要求编译、构建、启动、运行项目，或提到 "build"、"启动前后端"、"运行项目" 等关键词。
---

# 编译并启动前后端

## 项目结构

- **前端**: `mattmes-ui` - Vue 3 + Vite + TypeScript
- **后端**: `mattmes` - Spring Boot 3.3 + Maven 多模块项目

## Quick start

用户要求启动项目时，按以下顺序执行：

1. 检查并停止现有服务（前端、后端）
2. 编译后端
3. 编译前端
4. 启动后端（后台运行）
5. 启动前端（前台运行）

## Workflows

### 1. 检查并停止现有服务

在启动新服务前，检查前端和后端是否已在运行，如果运行中则先停止：

```bash
# 检查后端端口 8080 是否被占用 (Windows)
netstat -ano | findstr :8080

# 如果有进程占用 8080，获取 PID 并终止 (Windows)
for /f "tokens=5" %a in ('netstat -ano ^| findstr :8080') do taskkill /F /PID %a

# 检查前端端口 3000 是否被占用 (Windows)
netstat -ano | findstr :3000

# 如果有进程占用 3000，获取 PID 并终止 (Windows)
for /f "tokens=5" %a in ('netstat -ano ^| findstr :3000') do taskkill /F /PID %a
```

**注意**: 终止进程前确认是目标服务（Spring Boot 或 Vite dev server）。

### 2. 编译后端

在 `mattmes` 目录执行 Maven 编译：

```bash
cd mattmes
mvn clean compile -DskipTests
```

编译失败时，检查错误日志并提示用户修复。

### 3. 编译前端

在 `mattmes-ui` 目录执行 npm 构建：

```bash
cd mattmes-ui
npm run build
```

此命令会先执行 TypeScript 类型检查 (`vue-tsc -b`)，然后执行 Vite 构建。

### 4. 启动后端

后端使用 Spring Boot Maven 插件启动，在后台运行：

```bash
cd mattmes/mattmes-web
mvn spring-boot:run -Dspring-boot.run.fork=false
```

**配置说明**：
- 服务端口: `8080`
- 数据库: `localhost:3306/mattmes`
- 环境变量: `DB_USERNAME` 和 `DB_PASSWORD` (默认都是 `root`)

启动成功标志: 控制台输出 `Started MesApplication in X seconds`

### 5. 启动前端

前端使用 Vite 开发服务器，在前台运行：

```bash
cd mattmes-ui
npm run dev
```

**默认配置**：
- 开发服务器端口: `3000`
- 自动打开浏览器: 根据配置

启动成功标志: 控制台输出 `VITE vX.X.X  ready in X ms`

## Checklist

执行启动流程时，按此清单验证：

- [ ] 已检查并停止现有服务（前端、后端）
- [ ] 后端编译成功 (无 Maven 错误)
- [ ] 前端编译成功 (TypeScript + Vite)
- [ ] 后端启动成功 (8080 端口响应)
- [ ] 前端启动成功 (可访问 http://localhost:3000)

## 常见问题

### 后端启动失败

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| `Communications link failure` | MySQL 未启动或连接失败 | 启动 MySQL 服务，检查端口 |
| `Unknown database 'mattmes'` | 数据库不存在 | 执行初始化脚本创建数据库 |
| `Access denied for user` | 用户名密码错误 | 设置环境变量 `DB_USERNAME`/`DB_PASSWORD` |

### 前端编译失败

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| TypeScript 类型错误 | 类型定义不匹配 | 运行 `vue-tsc --noEmit` 检查具体错误 |
| `node_modules` 缺失 | 依赖未安装 | 执行 `npm install` |

## 停止服务

### Windows 系统

```bash
# 停止后端 (占用 8080 端口的进程)
for /f "tokens=5" %a in ('netstat -ano ^| findstr :8080') do taskkill /F /PID %a

# 停止前端 (占用 3000 端口的进程)
for /f "tokens=5" %a in ('netstat -ano ^| findstr :3000') do taskkill /F /PID %a
```

### Linux/Mac 系统

```bash
# 停止后端
lsof -ti:8080 | xargs kill -9

# 停止前端
lsof -ti:3000 | xargs kill -9
```

**注意**: 如果服务在前台运行，可以直接使用 `Ctrl+C` 停止。

## 端口冲突处理

工作流第一步会自动检测并停止占用端口的进程。如需手动检查：

```bash
# 检查后端端口占用
netstat -ano | findstr :8080  # Windows
netstat -tlnp | grep :8080    # Linux/Mac

# 检查前端端口占用
netstat -ano | findstr :3000  # Windows
netstat -tlnp | grep :3000    # Linux/Mac
```