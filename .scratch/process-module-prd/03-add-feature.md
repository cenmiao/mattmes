---
name: process-add-feature
description: 实现工序新增功能
labels: [ready-for-agent]
---

## Parent

工序管理模块PRD - `.scratch/process-module-prd.md`

## What to build

实现工序新增的完整垂直切片，从后端校验到前端表单提交。

**后端**：
- DTO: `ProcessAddDTO` (新增请求参数)
- Service: `ProcessService.add()` 
  - 校验工序编码唯一性(重复抛出业务异常)
  - 校验必填项(编码、名称、类型)
  - 校验字段长度限制
- Controller: `ProcessController.add()` POST `/api/process/add`，权限`process:add`

**前端**：
- 页面: `ProcessList.vue`
  - 新增按钮(使用`v-permission`指令控制显示)
  - 新增弹窗：表单包含工序编码、工序名称、工序类型、启用状态、描述、备注
  - 表单验证：必填项提示、字段长度限制
  - 提交成功后刷新列表并关闭弹窗

**数据校验规则**：
- 工序编码：必填，最大50字符，仅允许字母、数字、下划线、中划线，全局唯一
- 工序名称：必填，最大100字符
- 工序类型：必填，枚举值校验
- 启用状态：必填，新增时默认1

## Acceptance criteria

- [ ] 后端新增接口编译通过
- [ ] 编码重复时返回明确的错误提示
- [ ] 必填项校验正常工作
- [ ] 前端新增按钮根据权限显示/隐藏
- [ ] 前端表单验证正常工作
- [ ] 新增成功后列表自动刷新

## Blocked by

- 02-query-feature (需要查询页面作为基础)
