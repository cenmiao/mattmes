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

1. 检查并停止现有服务（前端、后端、所有Java进程）
2. 清理编译缓存并重新打包后端（jar包模式）
3. 编译前端
4. 启动后端（使用jar包，后台运行）
5. 启动前端（前台运行）

**重要**: 为避免编译缓存问题，后端必须使用 `mvn package` 打包并直接运行jar文件，而不是使用 `mvn spring-boot:run`。

## Workflows

### 1. 检查并停止现有服务

在启动新服务前，必须彻底清理所有相关进程，包括端口占用和Java进程：

**Windows系统 (在bash环境中)**：

```bash
# 步骤1: 检查并停止后端端口 8080
netstat -ano | findstr :8080
# 如果有进程占用，使用 PID 终止进程
# 注意: 在Windows bash环境中必须使用双斜杠 //
taskkill //F //PID <PID>

# 步骤2: 检查并停止前端端口 3000
netstat -ano | findstr :3000
# 如果有进程占用，使用 PID 终止进程
taskkill //F //PID <PID>

# 步骤3: 查找并停止所有Java进程（避免IDE锁定编译文件）
tasklist | findstr java.exe
# 对每个Java进程执行终止（VSCode Java Language Server等）
taskkill //F //PID <PID>
```

**Linux/Mac系统**：

```bash
# 检查并停止后端 (占用 8080 端口的进程)
lsof -ti:8080 | xargs kill -9

# 检查并停止前端 (占用 3000 端口的进程)
lsof -ti:3000 | xargs kill -9

# 停止所有Java进程
pkill -9 java
```

**注意**:
- Windows bash环境中,taskkill命令必须使用 `//F` 和 `//PID` 格式
- 必须停止所有Java进程，包括IDE的Java Language Server，否则可能锁定编译文件
- 终止进程前确认是目标服务（Spring Boot 或 Vite dev server）

### 2. 清理编译缓存并打包后端

**重要**: 为避免编译缓存导致的 `NoSuchMethodError` 问题，必须使用 `mvn package` 打包并运行jar文件。

#### 2.1 尝试清理编译（推荐）

```bash
# 尝试清理并打包
mvn -f mattmes/pom.xml clean package -DskipTests
```

#### 2.2 如果 clean 失败（文件被锁定）

如果遇到 `Failed to delete ... target/classes` 错误，说明IDE进程锁定了文件：

```bash
# 停止所有Java进程（已在步骤1完成）
# 然后跳过clean直接打包
mvn -f mattmes/pom.xml package -DskipTests
```

**打包成功标志**:
```
[INFO] Building jar: D:\Projects\...\mattmes-web\target\mattmes-web-1.0.0-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

**为什么必须使用 package 而不是 compile?**
- `mvn compile` 只编译源代码到 `.class` 文件
- `mvn spring-boot:run` 可能在某些情况下使用缓存的class文件
- `mvn package` 会生成独立的jar包，确保使用最新编译结果
- jar包运行时完全独立，不受Maven插件缓存影响

#### 2.3 验证编译结果（可选）

如果需要确认方法是否正确编译：

```bash
# 查看编译后的接口方法
javap -p mattmes/mattmes-business/target/classes/com/matt/mes/business/service/ProcessService.class

# 查看实现类方法
javap -p mattmes/mattmes-business/target/classes/com/matt/mes/business/service/impl/ProcessServiceImpl.class
```

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

**注意**: 前端编译不会有缓存问题，因为Vite每次都会重新构建。

### 4. 启动后端（使用jar包）

**重要**: 必须使用 `java -jar` 运行打包好的jar文件，而不是使用 `mvn spring-boot:run`。

#### 4.1 使用jar包启动（推荐方式）

```bash
# 在后台运行jar包
java -jar D:/Projects/MattSkillsDemoLocal/mattmes/mattmes-web/target/mattmes-web-1.0.0-SNAPSHOT.jar
```

或者在项目根目录：

```bash
# 使用相对路径
java -jar mattmes/mattmes-web/target/mattmes-web-1.0.0-SNAPSHOT.jar
```

#### 4.2 为什么禁止使用 mvn spring-boot:run？

❌ **不推荐**: `mvn spring-boot:run`
- Maven插件可能缓存编译结果
- 当源代码修改后，可能仍使用旧的class文件
- 导致 `NoSuchMethodError` 等运行时错误

✅ **推荐**: `java -jar xxx.jar`
- jar包包含所有依赖和编译结果
- 每次运行都使用最新的打包结果
- 完全独立，不受Maven插件缓存影响

**配置说明**：
- 服务端口: `8080`
- 数据库: `localhost:3306/mattmes`
- 环境变量: `DB_USERNAME` 和 `DB_PASSWORD` (默认都是 `root`)

**启动成功标志**: 控制台输出 `Started MesApplication in X seconds`

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

- [ ] 已检查并停止所有Java进程（包括IDE进程）
- [ ] 已检查并停止端口占用（8080、3000）
- [ ] 后端打包成功，生成jar文件（BUILD SUCCESS）
- [ ] 前端编译成功 (TypeScript + Vite)
- [ ] 后端使用 `java -jar` 启动成功 (8080 端口响应)
- [ ] 前端启动成功 (可访问 http://localhost:3000)
- [ ] 验证核心接口功能正常（如批量删除等）

## 常见问题

### 后端启动失败

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| `Communications link failure` | MySQL 未启动或连接失败 | 启动 MySQL 服务，检查端口 |
| `Unknown database 'mattmes'` | 数据库不存在 | 执行初始化脚本创建数据库 |
| `Access denied for user` | 用户名密码错误 | 设置环境变量 `DB_USERNAME`/`DB_PASSWORD` |
| `NoSuchMethodError` | 使用了缓存的旧class文件 | 使用 `mvn package` + `java -jar`，停止所有Java进程后重新打包 |

### 编译缓存问题

#### 问题现象
```
java.lang.NoSuchMethodError: 'java.util.List ProcessService.batchDelete(java.util.List)'
```

#### 根本原因
- VSCode Java Language Server锁定了 `target/classes` 目录
- Maven插件缓存了旧的编译结果
- `mvn spring-boot:run` 使用缓存而不是重新编译

#### 解决方案
```bash
# 1. 停止所有Java进程
tasklist | findstr java.exe
taskkill //F //PID <所有Java进程PID>

# 2. 跳过clean直接打包（避免文件锁定）
mvn -f mattmes/pom.xml package -DskipTests

# 3. 使用jar包运行
java -jar mattmes/mattmes-web/target/mattmes-web-1.0.0-SNAPSHOT.jar
```

#### 验证方法
```bash
# 检查编译后的class文件是否包含新方法
javap -p mattmes/mattmes-business/target/classes/com/matt/mes/business/service/ProcessService.class | grep batchDelete
```

### Maven clean 失败

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| `Failed to delete target/classes` | IDE进程锁定文件 | 停止所有Java进程，或跳过clean直接package |
| `Device or resource busy` | 文件正在被使用 | 等待5秒后重试，或关闭IDE后重新编译 |

### 前端编译失败

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| TypeScript 类型错误 | 类型定义不匹配 | 运行 `vue-tsc --noEmit` 检查具体错误 |
| `node_modules` 缺失 | 依赖未安装 | 执行 `npm install` |

## 最佳实践

### 开发阶段快速迭代

如果只是简单修改，不需要完全重启：

```bash
# 前端: Vite支持热更新，修改后自动刷新
# 无需重启前端服务

# 后端: 如果使用IDE的调试模式，可以热部署
# 但验证重要功能时，建议完全重启
```

### 重要功能验证流程

在验证重要功能（如新接口、批量操作等）时：

1. ✅ **停止所有服务** - 包括IDE的Java进程
2. ✅ **清理并打包** - `mvn package -DskipTests`
3. ✅ **使用jar运行** - `java -jar xxx.jar`
4. ✅ **接口测试** - 使用curl或Postman验证
5. ✅ **前端验证** - 在浏览器中测试完整流程

### 避免的常见错误

❌ **错误做法**:
```bash
# 错误1: 使用spring-boot:run
mvn spring-boot:run  # 可能使用缓存

# 错误2: 只编译不打包
mvn compile  # 不够彻底

# 错误3: 不停止旧进程
# 直接启动新进程导致端口冲突
```

✅ **正确做法**:
```bash
# 正确流程
1. 停止所有Java进程
2. mvn package -DskipTests
3. java -jar xxx.jar
4. 验证接口功能
```

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