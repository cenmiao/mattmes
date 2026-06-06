---
name: process-edit-feature
description: 实现工序编辑功能
labels: [done]
completed: 2026-06-05
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

- [x] 后端编辑接口编译通过
- [x] 工序编码不可修改(后端拒绝修改请求)
- [x] 前端编辑按钮根据权限显示/隐藏
- [x] 前端编辑弹窗正确显示当前数据
- [x] 工序编码字段禁用状态正确
- [x] 编辑成功后列表自动刷新

## Blocked by

- 02-query-feature (需要查询页面作为基础)

---

## Completion Notes

**2026-06-05 完成记录:**

- 状态: `needs-triage` → `done`
- 实现方式: 严格按照 TDD 的 RED-GREEN-REFACTOR 流程实施，完成工序编辑功能的完整垂直切片

**创建的文件:**

**后端:**
1. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/dto/ProcessEditRequest.java` - 编辑请求DTO
2. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/service/ProcessService.java` - 添加 edit 方法签名
3. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/service/impl/ProcessServiceImpl.java` - 实现 edit 方法
4. `mattmes/mattmes-web/src/main/java/com/matt/mes/controller/ProcessController.java` - 添加 edit 接口

**后端测试:**
5. `mattmes/mattmes-business/src/test/java/com/matt/mes/business/service/ProcessServiceTest.java` - 添加编辑测试
6. `mattmes/mattmes-web/src/test/java/com/matt/mes/controller/ProcessControllerTest.java` - 添加编辑接口测试

**前端:**
7. `mattmes-ui/src/api/process.ts` - 添加 ProcessEditRequest 类型和 editProcess 函数
8. `mattmes-ui/src/views/process/ProcessList.vue` - 添加编辑按钮和编辑弹窗

**前端测试:**
9. `mattmes-ui/src/api/__tests__/process.spec.ts` - 添加 editProcess 测试

**测试统计:**
- 后端 Service 测试: 21 个测试全部通过
- 后端 Controller 测试: 8 个测试全部通过
- 前端 API 测试: 5 个测试全部通过

**关键实现细节:**
- 后端 edit 方法实现了完整的校验逻辑：工序存在性、名称必填/长度、类型必填/枚举有效性
- 工序编码不在编辑 DTO 中，确保编码不可修改
- 前端使用 `v-permission="'process:edit'"` 控制编辑按钮显示
- 编辑弹窗中工序编码字段使用 `disabled` 属性禁用
