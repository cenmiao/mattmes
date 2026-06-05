---
name: process-status-feature
description: 实现工序状态管理功能
labels: [completed]
---

## Parent

工序管理模块PRD - `.scratch/process-module-prd.md`

## What to build

实现工序启用/禁用状态切换的完整垂直切片，使用Switch开关实现即时切换。

**后端**：
- Service: `ProcessService.updateStatus()` 更新工序启用状态
- Controller: `ProcessController.updateStatus()` PUT `/api/process/status/{id}`，权限`process:edit`

**前端**：
- 页面: `ProcessList.vue`
  - 数据表格启用状态列：使用Element Plus的Switch组件
  - Switch组件使用`v-permission`指令控制是否可操作
  - 点击Switch立即调用状态更新接口，无需确认提示
  - 状态更新成功后显示成功提示

**交互特性**：
- 点击Switch立即生效，无需确认
- 无权限用户只能查看状态，不能操作Switch

## Acceptance criteria

- [x] 后端状态更新接口编译通过
- [x] 前端Switch组件正确显示当前状态
- [x] 有权限用户点击Switch立即更新状态
- [x] 无权限用户Switch禁用或隐藏
- [x] 状态更新成功显示成功提示

## Blocked by

- 02-query-feature (需要查询页面作为基础)

## Implementation completed

完成时间：2026-06-05

**实现内容**：

1. **后端**：
   - `ProcessService.java`: 新增 `updateStatus(Long id, Integer enable)` 方法
   - `ProcessServiceImpl.java`: 实现状态更新逻辑，包含工序存在性校验和状态值校验
   - `ProcessController.java`: 新增 `PUT /api/process/status/{id}` 接口

2. **前端**：
   - `process.ts`: 新增 `updateProcessStatus(id: number, enable: number)` API函数
   - `ProcessList.vue`: 启用状态列从 Tag 改为 Switch 组件
     - 使用 `v-permission="'process:edit'"` 指令控制权限
     - 点击 Switch 立即调用接口更新状态
     - 成功后显示成功提示
     - 失败时恢复原状态并显示错误提示