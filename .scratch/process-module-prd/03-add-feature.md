---
name: process-add-feature
description: 实现工序新增功能
labels: [done]
completed: 2026-06-05
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

- [x] 后端新增接口编译通过
- [x] 编码重复时返回明确的错误提示
- [x] 必填项校验正常工作
- [x] 前端新增按钮根据权限显示/隐藏
- [x] 前端表单验证正常工作
- [x] 新增成功后列表自动刷新

## Blocked by

- 02-query-feature (需要查询页面作为基础)

---

## Completion Notes

**2026-06-05 完成记录:**

- 状态: `ready-for-agent` → `done`
- 实现方式: 严格遵循TDD RED-GREEN-REFACTOR流程完成工序新增功能

**创建/修改的文件:**

**后端:**
1. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/dto/ProcessAddRequest.java` - 新增请求DTO
2. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/service/ProcessService.java` - 添加add方法接口
3. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/service/impl/ProcessServiceImpl.java` - 实现新增逻辑（编码唯一性校验、必填项校验、字段长度校验、编码格式校验）
4. `mattmes/mattmes-web/src/main/java/com/matt/mes/controller/ProcessController.java` - 添加add接口

**前端:**
1. `mattmes-ui/src/api/process.ts` - 添加ProcessAddRequest类型和addProcess函数
2. `mattmes-ui/src/views/process/ProcessList.vue` - 实现新增按钮、弹窗、表单验证和提交

**测试:**
1. `mattmes/mattmes-business/src/test/java/com/matt/mes/business/service/ProcessServiceTest.java` - 添加编码唯一性、必填项、字段长度校验测试
2. `mattmes/mattmes-web/src/test/java/com/matt/mes/controller/ProcessControllerTest.java` - 添加Controller新增接口测试
3. `mattmes-ui/src/api/__tests__/process.spec.ts` - 添加前端API测试

**TDD开发流程:**
- RED阶段: 编写失败测试，验证测试确实失败
- GREEN阶段: 实现最小代码使测试通过
- 遵循垂直切片原则，一次只实现一个行为

**验证结果:**
- ✅ 后端编译通过
- ✅ 前端编译通过
- ✅ 所有单元测试通过
- ✅ 所有验收标准达成
