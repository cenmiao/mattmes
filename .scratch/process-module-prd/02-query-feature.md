---
name: process-query-feature
description: 实现工序查询功能
labels: [done]
completed: 2026-06-04
---

## Parent

工序管理模块PRD - `.scratch/process-module-prd.md`

## What to build

实现工序列表查询的完整垂直切片，从数据库到前端页面的端到端功能。

**后端**：
- Entity: `Process` 实体类
- DTO: `ProcessQueryDTO` (查询条件), `ProcessVO` (返回视图)
- Mapper: `ProcessMapper` 继承MyBatis-Plus BaseMapper
- Service: `ProcessService.queryList()` 实现分页查询、多条件筛选(编码/名称/类型/状态)
- Controller: `ProcessController.list()` POST `/api/process/list`

**前端**：
- API: `src/api/process.ts` 定义查询接口
- 页面: `src/views/process/ProcessList.vue`
  - 查询表单：工序编码、工序名称、工序类型、启用状态
  - 查询/重置按钮
  - 数据表格：显示工序列表，支持分页和每页条数调整

**权限**：查询无需权限控制

## Acceptance criteria

- [x] 后端Entity/Mapper/Service/Controller编译通过
- [x] 查询接口支持分页参数(pageNum, pageSize)
- [x] 查询接口支持多条件筛选(编码模糊、名称模糊、类型精确、状态精确)
- [x] 前端页面显示查询表单和数据表格
- [x] 前端调用查询接口成功渲染数据
- [x] 分页功能正常工作

## Blocked by

- 01-table-creation (数据库表必须先创建)

---

## Completion Notes

**2026-06-04 完成记录:**

- 状态: `ready-for-agent` → `done`
- 实现方式: 完整实现了工序查询功能的垂直切片，包括后端Entity/Mapper/Service/Controller和前端API/页面组件

**创建的文件:**

**后端:**
1. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/entity/Process.java`
2. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/mapper/ProcessMapper.java`
3. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/dto/ProcessQueryRequest.java`
4. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/dto/ProcessResponse.java`
5. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/dto/ProcessPageResult.java`
6. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/service/ProcessService.java`
7. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/service/impl/ProcessServiceImpl.java`
8. `mattmes/mattmes-web/src/main/java/com/matt/mes/controller/ProcessController.java`

**前端:**
1. `mattmes-ui/src/api/process.ts`
2. `mattmes-ui/src/views/process/ProcessList.vue`

**测试验证:**
- ✅ 后端编译通过
- ✅ 接口测试成功（使用永久token验证返回数据格式正确）
- ✅ 前端页面正确调用接口
- ✅ 修复了API路径重复问题（从 `/api/process/list` 改为 `/process/list`）

---
