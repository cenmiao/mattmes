---
status: needs-triage
---

# 权限管理CRUD

## Parent

参考 `.scratch/login-module/PRD.md`

## What to build

实现权限管理完整CRUD功能，支持创建权限、模块分组、按钮级子权限、树形结构查询。

核心功能：
- 创建权限（权限名称、权限编码如user:add）
- 按模块组织权限（用户管理、角色管理等）
- 定义按钮级子权限（模块下的具体操作）
- 编辑权限（名称、描述）
- 删除权限
- 查询权限树形结构

## Acceptance criteria

- [ ] 创建 `SysPermission.java` 实体类和 Mapper
  - 字段：id, permission_name, permission_code, parent_id(父权限), permission_type(模块/按钮), description
- [ ] 创建 `PermissionService.java` 实现权限管理逻辑
- [ ] 创建 `PermissionController.java` 提供REST接口：
  - POST /api/permissions - 创建权限
  - PUT /api/permissions/{id} - 编辑权限
  - DELETE /api/permissions/{id} - 删除权限
  - GET /api/permissions - 查询权限列表
  - GET /api/permissions/tree - 查询权限树形结构
- [ ] 创建权限时验证permission_code唯一性，已存在返回400
- [ ] permission_code格式规范：模块级如`user`，按钮级如`user:add`
- [ ] parent_id=null表示模块级权限，有值表示按钮级子权限
- [ ] 查询树形结构返回嵌套JSON：模块包含其下所有按钮权限
- [ ] 删除权限时同步清理sys_role_permission关联数据
- [ ] 删除模块级权限时同时删除其下所有按钮级权限
- [ ] 编写 `PermissionServiceTest.java` 单元测试覆盖：
  - 创建模块级权限成功
  - 创建按钮级权限成功
  - 编码重复
  - 编辑权限成功
  - 删除权限成功（含关联清理）
  - 删除模块级权限同时删除子权限
  - 查询列表
  - 查询树形结构

## Blocked by

- #01-database-schema（需要表结构）
- #02-backend-skeleton（需要项目骨架）
- #07-role-management（权限需关联角色）

## Comments

_在此追加对话历史和讨论记录_