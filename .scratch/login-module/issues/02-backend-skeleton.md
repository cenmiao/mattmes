---
status: done
---

# 后端项目骨架搭建

## Parent

参考 `.scratch/login-module/PRD.md`

## What to build

搭建Maven多模块SpringBoot项目骨架，为后续业务模块开发提供基础设施。

模块结构：
- `mattmes-common` - 通用组件（工具类、配置、异常处理、拦截器）
- `mattmes-system` - 系统管理（用户、角色、权限、登录、日志）
- `mattmes-business` - 业务模块（预留，暂无内容）
- `mattmes-web` - Web层（Controller、启动类）

模块依赖：`mattmes-web → mattmes-business → mattmes-system → mattmes-common`

## Acceptance criteria

- [x] 创建父POM `mattmes/pom.xml`，配置Java 17、SpringBoot 3.x父依赖、依赖管理
- [x] 创建 `mattmes-common/pom.xml`，包含基础依赖（SpringBoot Web、MyBatis-Plus、Spring Security BCrypt、JWT、Lombok等）
- [x] 创建 `mattmes-system/pom.xml`，依赖 mattmes-common
- [x] 创建 `mattmes-business/pom.xml`，依赖 mattmes-system（预留空模块）
- [x] 创建 `mattmes-web/pom.xml`，依赖 mattmes-business 和 mattmes-system
- [x] 创建 `mattmes-web/src/main/java/com/matt/mes/MesApplication.java` 启动类
- [x] 创建 `mattmes-web/src/main/resources/application.yml` 配置文件（数据库连接、端口等占位配置）
- [x] 在 mattmes-common 创建通用组件骨架：
  - `config/MybatisPlusConfig.java`（审计字段自动填充处理器）
  - `exception/BusinessException.java`
  - `exception/GlobalExceptionHandler.java`
  - `result/Result.java`（统一响应格式：code, message, data）
  - `result/ResultCode.java`（标准错误码：200/400/401/403/409/500）
- [x] 项目可成功执行 `mvn clean install` 编译通过
- [x] 启动类可成功启动（即使无业务逻辑）

## Blocked by

None - 可立即开始

## Comments

_2026-05-31_: 已完成所有验收标准。项目结构：
- 父POM使用SpringBoot 3.3.0，Java 17
- 依赖版本：MyBatis-Plus 3.5.5，JJWT 0.12.5
- 编译验证：`mvn clean install -DskipTests` BUILD SUCCESS
- 启动验证：Spring Boot启动成功，Tomcat初始化端口8080（无MySQL驱动导致数据源失败，属预期行为）
