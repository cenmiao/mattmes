---
name: process-edit-feature
description: 实现工序编辑功能
labels: [ready-for-agent]
---

## Parent

工序管理模块PRD - `.scratch/process-module-prd.md`

## What to build

实现工序信息编辑的完整垂直切片，包含编辑限制和权限控制。

**后端**：
- DTO: `ProcessEditDTO` (编辑请求参数)
- Service: `ProcessService.edit()` 
  - 校验工序是否存在
  - 校验字段长度限制
  - 不允许修改工序编码
- Controller: `ProcessController.edit()` PUT `/api/process/edit`，权限`process:edit`

**前端**：
- 页面: `ProcessList.vue`
  - 数据表格操作列：编辑按钮(使用`v-permission`指令控制显示)
  - 编辑弹窗：表单包含工序名称、工序类型、启用状态、描述、备注
  - 工序编码字段显示但禁用(不可修改)
  - 工序类型默认显示当前值，可修改
  - 提交成功后刷新列表并关闭弹窗

**数据校验规则**：
- 工序编码：禁止修改
- 工序名称：必填，最大100字符
- 工序类型：必填，枚举值校验
- 其他字段：选填，长度限制

## Acceptance criteria

- [ ] 后端编辑接口编译通过
- [ ] 工序编码不可修改(后端拒绝修改请求)
- [ ] 前端编辑按钮根据权限显示/隐藏
- [ ] 前端编辑弹窗正确显示当前数据
- [ ] 工序编码字段禁用状态正确
- [ ] 编辑成功后列表自动刷新

## Blocked by

- 02-query-feature (需要查询页面作为基础)
