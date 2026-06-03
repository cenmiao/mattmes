# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Matt MES管理系统后端 - 离散制造执行系统，采用 Spring Boot 3.3 多模块架构。

## 常用命令

```bash
# 编译项目（根目录下）
mvn clean compile

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=UserServiceTest

# 启动后端服务（端口 8080）
mvn spring-boot:run -pl mattmes-web

# 打包（跳过测试）
mvn clean package -DskipTests
```

## 模块架构

多模块依赖关系（从下到上）：

```
mattmes-web          # Controller、启动类
    ↓
mattmes-business     # 业务模块（SN、过站、检测等）
    ↓
mattmes-system       # 系统模块（用户、角色、权限）
    ↓
mattmes-common       # 通用组件、异常处理、工具类
```

## 包结构规范

```
com.matt.mes.{module}
├── config/          # 配置类
├── controller/      # 控制器（仅 web 模块）
├── service/         # 服务接口
│   └── impl/        # 服务实现
├── mapper/          # MyBatis Mapper
├── entity/          # 实体类（对应数据库表）
└── dto/             # 数据传输对象（请求/响应）
```

## 关键组件

- 统一响应：`mattmes-common/.../result/Result.java`
- 异常处理：`mattmes-common/.../exception/GlobalExceptionHandler.java`
- JWT工具：`mattmes-common/.../utils/JwtUtils.java`
- Token拦截器：`mattmes-system/.../interceptor/TokenInterceptor.java`
- MyBatis-Plus配置：`mattmes-common/.../config/MybatisPlusConfig.java`（含审计字段自动填充）

## 数据库配置

- 连接配置：`mattmes-web/src/main/resources/application.yml`
- 表前缀：`sys_`（系统表）、`biz_`（业务表）
- 审计字段：所有表必须包含 `created_by`, `create_time`, `updated_by`, `update_time`
- 逻辑删除：字段 `deleted`，值为 0（未删除）/ 1（已删除）

## API规范

- 所有API路径以 `/api/` 开头
- 登录接口 `/api/login` 无需认证
- 其他接口需在 Header 携带 `Authorization: Bearer {token}`
- 统一返回格式：`{ code, message, data }`

## 认证机制

- JWT Token 有效期 6 小时
- Token 存储在 `sys_user.current_token` 字段，用于单点登录控制
- 用户被禁用时清空 Token 强制登出
