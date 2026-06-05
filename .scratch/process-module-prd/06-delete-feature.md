---
name: process-delete-feature
description: 实现工序删除功能
labels: [ready-for-agent]
---

## Parent

工序管理模块PRD - `.scratch/process-module-prd.md`

## What to build

实现工序删除和批量删除的完整垂直切片，包含确认提示和权限控制。

**后端**：
- Service: `ProcessService.delete()` 单个删除, `ProcessService.batchDelete()` 批量删除
- Controller: 
  - `ProcessController.delete()` DELETE `/api/process/delete/{id}`，权限`process:delete`
  - `ProcessController.batchDelete()` DELETE `/api/process/batchDelete`，权限`process:delete`

**前端**：
- 页面: `ProcessList.vue`
  - 数据表格：复选框列，支持多选
  - 操作区域：删除按钮(批量删除)，使用`v-permission`指令控制显示
  - 数据表格操作列：删除按钮(单个删除)，使用`v-permission`指令控制显示
  - 删除前显示确认提示框
  - 删除成功后刷新列表

**交互特性**：
- 单个删除和批量删除都需要确认提示
- 批量删除可删除任何状态的工序
- 删除成功后自动刷新列表

## Acceptance criteria

- [ ] 后端删除接口编译通过
- [ ] 后端批量删除接口编译通过
- [ ] 前端删除按钮根据权限显示/隐藏
- [ ] 删除前显示确认提示框
- [ ] 单个删除成功后列表自动刷新
- [ ] 批量删除成功后列表自动刷新
- [ ] 复选框多选功能正常工作

## Blocked by

- 02-query-feature (需要查询页面作为基础)
