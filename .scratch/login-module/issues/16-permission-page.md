---
status: needs-triage
---

# 权限管理页面

## Parent

参考 `.scratch/login-module/PRD.md`

## What to build

实现权限管理页面UI组件，包含树形展开表格、权限创建编辑删除、模块/按钮类型区分。

核心功能：
- 树形展开表格（模块级权限展开显示按钮级）
- 新增/编辑权限弹窗
- 权限类型样式区分（模块徽章/按钮徽章）
- 删除权限（同步清理关联）

## Acceptance criteria

- [ ] 创建 `src/views/permission/PermissionList.vue` 权限管理页面
- [ ] 创建 `src/api/permission.ts` 权限API封装：
  - `getPermissions()` - 查询列表
  - `getPermissionTree()` - 查询树形结构
  - `createPermission(data)` - 创建权限
  - `updatePermission(id, data)` - 编辑权限
  - `deletePermission(id)` - 删除权限
- [ ] 树形展开表格（Element Plus Table带expand功能）：
  - 权限名称列：模块级显示模块图标，按钮级显示按钮图标
  - 权限编码列：等宽字体(IBMMono)，Cyan色(#22D3EE)
  - 类型列：Badge徽章 - 模块(蓝色)、按钮(灰色)
  - 描述列：普通字体
  - 创建时间列：等宽字体
  - 操作列：图标按钮组（编辑、删除、新增子权限）
- [ ] 模块级权限行可展开，显示其下所有按钮级权限
- [ ] 新增权限弹窗：
  - 权限名称输入（必填）
  - 权限编码输入（必填，提示格式如user或user:add）
  - 父权限选择（可选，选择后为按钮级权限）
  - 描述输入（可选）
  - 提交按钮
- [ ] 编辑权限弹窗：
  - 权限名称、描述可编辑
  - 权限编码显示不可编辑
  - 提交按钮
- [ ] 新增子权限按钮（模块行操作列）：
  - 点击打开新增权限弹窗，自动填充父权限
- [ ] 删除按钮：
  - 点击显示确认弹窗
  - 删除模块级时提示"将同时删除其下所有按钮级权限"
  - 删除成功刷新列表
- [ ] 使用v-permission指令控制按钮显示：
  - 新增按钮：v-permission="permission:add"
  - 编辑按钮：v-permission="permission:edit"
  - 删除按钮：v-permission="permission:delete"
- [ ] 编写组件测试覆盖关键交互

## Blocked by

- #03-frontend-skeleton（需要前端骨架）
- #08-permission-management（需要权限管理接口）
- #11-main-layout（需要路由和权限指令）

## Comments

_在此追加对话历史和讨论记录_