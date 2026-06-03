---
status: needs-triage
---

# 系统初始化脚本

## Parent

参考 `.scratch/login-module/PRD.md`

## What to build

创建系统初始化SQL脚本，插入超级管理员账号、默认角色、默认权限集，确保系统首次启动即可登录使用。

核心功能：
- 创建超级管理员账号（admin / Admin@123）
- 创建SUPER_ADMIN角色
- 创建默认权限集（用户、角色、权限、登录日志模块及按钮级权限）
- 关联角色权限
- 验证脚本可执行无误

## Acceptance criteria

- [ ] 创建SQL初始化脚本文件（可放置在 `mattmes-web/src/main/resources/init-data.sql`）
- [ ] 脚本包含以下初始化数据：
- [ ] **超级管理员账号**：
  - INSERT INTO sys_user: user_no='admin', password=BCrypt('Admin@123'), name='超级管理员', status=1
  - password_update_time设为NULL（首次登录强制修改）
  - 角色关联：INSERT INTO sys_user_role关联SUPER_ADMIN角色
- [ ] **SUPER_ADMIN角色**：
  - INSERT INTO sys_role: role_name='超级管理员', role_code='SUPER_ADMIN', status=1
- [ ] **默认权限集**：
  - 用户管理模块：permission_code='user'
    - 按钮级：user:add, user:edit, user:delete, user:disable, user:reset-password, user:view
  - 角色管理模块：permission_code='role'
    - 按钮级：role:add, role:edit, role:delete, role:assign-permission, role:view
  - 权限管理模块：permission_code='permission'
    - 按钮级：permission:add, permission:edit, permission:delete, permission:view
  - 登录日志模块：permission_code='login-log'
    - 按钮级：login-log:export, login-log:view
- [ ] **角色权限关联**：
  - SUPER_ADMIN角色关联所有权限
  - 或标记SUPER_ADMIN拥有全部权限（后端跳过验证）
- [ ] 脚本执行顺序：先表结构(#1)，后初始化数据
- [ ] 脚本可多次执行不报错（使用INSERT IGNORE或检查存在）
- [ ] 编写验证脚本：
  - 查询admin账号存在
  - 查询SUPER_ADMIN角色存在
  - 查询所有默认权限存在
  - 查询角色权限关联正确
- [ ] 在application.yml配置自动执行初始化脚本（可选）
- [ ] 或提供手动执行说明文档

## Blocked by

- #01-database-schema（需要表结构）
- #04-login-auth-core（需要BCrypt密码加密）
- #06-user-management（需要用户角色关联）
- #07-role-management（需要角色权限关联）

## Comments

_在此追加对话历史和讨论记录_