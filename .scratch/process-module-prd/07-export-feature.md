---
name: process-export-feature
description: 实现工序数据导出功能
labels: [done]
completed: 2026-06-06
---

## Parent

工序管理模块PRD - `.scratch/process-module-prd.md`

## What to build

实现工序数据导出CSV的完整垂直切片，支持按当前查询条件导出。

**后端**：
- Service: `ProcessService.export()` 
  - 根据当前查询条件查询数据
  - 生成CSV格式数据
  - 文件名格式：`工序数据_YYYYMMDD_HHMMSS.csv`
- Controller: `ProcessController.export()` GET `/api/process/export`，权限`process:export`

**前端**：
- 页面: `ProcessList.vue`
  - 操作区域：导出按钮，使用`v-permission`指令控制显示
  - 点击导出按钮调用导出接口
  - 浏览器自动下载CSV文件

**导出特性**：
- 导出当前查询条件筛选后的数据
- CSV文件包含表头
- 文件名带时间戳区分不同导出

## Acceptance criteria

- [x] 后端导出接口编译通过
- [x] 导出数据符合当前查询条件
- [x] CSV格式正确，包含表头
- [x] 文件名格式为`工序数据_YYYYMMDD_HHMMSS.csv`
- [x] 前端导出按钮根据权限显示/隐藏
- [x] 点击导出按钮成功下载文件

## Blocked by

- 02-query-feature (需要查询页面作为基础)

---

## Completion Notes

**2026-06-06 完成记录:**

- 状态: `ready-for-agent` → `done`
- 实现方式: 实现了工序数据导出CSV的完整垂直切片，支持按当前查询条件导出

**创建的文件:**
1. `.mattmes/src/main/java/com/mattmes/controller/ProcessController.java` - 导出接口
2. `.mattmes/src/main/java/com/mattmes/service/ProcessService.java` - 导出服务方法
3. `.mattmes-ui/src/views/process/ProcessList.vue` - 前端导出按钮

**Git 提交记录:**
- `6eae392 feat: 实现工序数据导出功能`

---
