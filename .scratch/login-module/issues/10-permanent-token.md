---
status: needs-triage
---

# 永久Token管理

## Parent

参考 `.scratch/login-module/PRD.md`

## What to build

实现永久Token管理功能，允许外部系统或脚本调用API。

核心功能：
- 为用户生成永久Token（UUID格式）
- 记录用途说明
- 查询永久Token列表
- 删除永久Token（取消API访问权限）

## Acceptance criteria

- [ ] 创建 `SysPermanentToken.java` 实体类和 Mapper
  - 字段：id, user_id, token(UUID), description(用途说明), 审计字段
- [ ] 创建 `PermanentTokenService.java` 实现Token管理逻辑
- [ ] 创建 `PermanentTokenController.java` 提供REST接口：
  - POST /api/permanent-tokens - 生成永久Token
  - GET /api/permanent-tokens - 查询Token列表
  - DELETE /api/permanent-tokens/{id} - 删除Token
- [ ] 生成Token时参数：{userId, description}
- [ ] Token格式：UUID.randomUUID().toString()
- [ ] 查询列表返回：Token值、所属用户、用途说明、创建时间
- [ ] 删除Token时同步清理（取消API访问权限）
- [ ] 永久Token验证逻辑（TokenInterceptor需识别永久Token）：
  - 检查sys_permanent_token表
  - 永久Token无过期时间限制
- [ ] 编写 `PermanentTokenServiceTest.java` 单元测试覆盖：
  - 生成Token成功
  - 查询列表
  - 删除Token成功
  - Token验证逻辑

## Blocked by

- #01-database-schema（需要表结构）
- #02-backend-skeleton（需要项目骨架）
- #06-user-management（Token关联用户）

## Comments

_在此追加对话历史和讨论记录_