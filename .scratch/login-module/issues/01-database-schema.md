---
status: completed
---

# 数据库Schema初始化

## Parent

参考 `.scratch/login-module/PRD.md`

## What to build

创建MES登录模块所需的7张核心数据库表，包含所有审计字段、索引和外键约束。使用MySQL 8.x语法。

涉及的表：
- `sys_user` - 用户表（工号、密码、姓名、状态、锁定信息、Token等）
- `sys_role` - 角色表（角色名、编码、状态）
- `sys_permission` - 权限表（权限名、编码）
- `sys_user_role` - 用户角色关联表
- `sys_role_permission` - 角色权限关联表
- `sys_permanent_token` - 永久Token表
- `sys_login_log` - 登录日志表

所有表必须包含审计字段：`created_by`, `create_time`, `updated_by`, `update_time`。

## Acceptance criteria

- [x] 创建完整的SQL脚本文件（可放置在 `mattmes-web/src/main/resources/schema.sql` 或独立文档目录）
- [x] `sys_user` 表包含所有字段：id, user_no(唯一索引), password, name, phone, email, status, disable_reason, password_update_time, login_error_count, lock_time, last_login_time, current_token, token_expire_time, 审计字段
- [x] `sys_user.user_no` 设置唯一索引
- [x] `sys_role.role_code` 设置唯一索引（如SUPER_ADMIN）
- [x] `sys_permission.permission_code` 设置唯一索引（如user:add）
- [x] 关联表正确设置外键约束指向主表
- [x] 所有表设置主键自增
- [x] 所有审计字段设置默认值或自动填充机制说明
- [x] SQL脚本可直接在MySQL 8.x执行无误
- [x] 编写简单的验证脚本（插入测试数据验证表结构）

## Blocked by

None - 可立即开始

## Comments

**2026-05-31 执行记录:**

- 创建了 `mattmes` 数据库 (utf8mb4字符集)
- 成功创建7张表: sys_user, sys_role, sys_permission, sys_user_role, sys_role_permission, sys_permanent_token, sys_login_log
- 插入测试数据验证通过:
  - 用户: admin(超级管理员), testuser(测试用户)
  - 角色: SUPER_ADMIN, TEST_ROLE
  - 权限: 4个模块权限 + 9个按钮权限
  - 关联关系: 用户角色关联2条, 角色权限关联4条
  - 永久Token: 1条
  - 登录日志: 2条(1成功1失败)
- 唯一索引验证: 重复工号插入被正确拒绝
- 外键约束验证: CASCADE删除已配置
- 审计字段自动填充: create_time/update_time 使用数据库默认值