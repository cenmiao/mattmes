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

**Windows系统 (在bash环境中)**：

```bash
# 检查后端端口 8080 是否被占用
netstat -ano | findstr :8080

# 如果有进程占用 8080，使用 PID 终止进程
# 注意: 在Windows bash环境中必须使用双斜杠 //
taskkill //F //PID <PID>

# 检查前端端口 3000 是否被占用
netstat -ano | findstr :3000

# 如果有进程占用 3000，使用 PID 终止进程
taskkill //F //PID <PID>
```

**Linux/Mac系统**：

```bash
# 检查并停止后端 (占用 8080 端口的进程)
lsof -ti:8080 | xargs kill -9

# 检查并停止前端 (占用 3000 端口的进程)
lsof -ti:3000 | xargs kill -9
```

**注意**:
- Windows bash环境中,taskkill命令必须使用 `//F` 和 `//PID` 格式
- 终止进程前确认是目标服务（Spring Boot 或 Vite dev server）

### 2. 编译后端

在项目根目录执行 Maven 编译：

```bash
# 使用相对路径从项目根目录编译
mvn -f mattmes/pom.xml clean compile -DskipTests
```

编译失败时，检查错误日志并提示用户修复。

### 3. 编译前端

前端编译需要确保在正确目录执行，使用以下方式之一：

**方式1: 使用子shell (推荐)**

```bash
# 子shell执行,不影响当前工作目录
(cd mattmes-ui && npm run build)
```

**方式2: 指定工作目录**

```bash
# 使用绝对路径确保目录正确
npm run build --prefix mattmes-ui
```

此命令会先执行 TypeScript 类型检查 (`vue-tsc -b`)，然后执行 Vite 构建。

### 4. 启动后端

后端使用 Spring Boot Maven 插件启动，在后台运行：

**重要**: 必须确保工作目录正确,推荐使用绝对路径:

```bash
# 使用绝对路径启动后端 (推荐)
mvn -f D:/Projects/MattSkillsDemoLocal/mattmes/mattmes-web/pom.xml spring-boot:run -Dspring-boot.run.fork=false
```

或者在项目根目录使用相对路径:

```bash
# 从项目根目录使用相对路径启动
mvn -f mattmes/mattmes-web/pom.xml spring-boot:run -Dspring-boot.run.fork=false
```

**配置说明**：
- 服务端口: `8080`
- 数据库: `localhost:3306/mattmes`
- 环境变量: `DB_USERNAME` 和 `DB_PASSWORD` (默认都是 `root`)

启动成功标志: 控制台输出 `Started MesApplication in X seconds`

### 5. 启动前端

前端使用 Vite 开发服务器，在前台运行：

**重要**: 确保在正确目录启动前端:

**方式1: 使用子shell (推荐)**

```bash
# 子shell执行,不影响后续命令的工作目录
(cd mattmes-ui && npm run dev)
```

**方式2: 在后台运行**

```bash
# 切换到前端目录启动
cd mattmes-ui && npm run dev
```

**方式3: 使用npm --prefix参数**

```bash
# 不切换目录,使用--prefix指定路径
npm run dev --prefix mattmes-ui
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

### Windows 系统 (在bash环境中)

**重要**: 在Windows bash环境中必须使用双斜杠 `//` 格式:

```bash
# 检查并停止后端 (占用 8080 端口的进程)
netstat -ano | findstr :8080
# 使用找到的 PID 终止进程
taskkill //F //PID <PID>

# 检查并停止前端 (占用 3000 端口的进程)
netstat -ano | findstr :3000
# 使用找到的 PID 终止进程
taskkill //F //PID <PID>
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