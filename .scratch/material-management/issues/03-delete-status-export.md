# Issue: 删除、状态切换与导出

Status: needs-triage

## Parent

[02-material-crud-core](issues/02-material-crud-core.md)

## What to build

实现料号的删除功能（单个删除和批量删除），包含删除保护逻辑：已绑定路由或有工单的料号禁止删除。实现状态切换功能，通过表格中的 Switch 开关快速切换启用/禁用状态。实现导出功能，将料号列表导出为 CSV 文件，文件名包含时间戳。

## Acceptance criteria

- [ ] 后端单个删除接口 `DELETE /api/material/{id}` 可正常调用
- [ ] 后端批量删除接口 `DELETE /api/material/batch` 可正常调用
- [ ] 后端状态切换接口 `PUT /api/material/{id}/status` 可正常调用
- [ ] 后端导出接口 `GET /api/material/export` 可正常调用并返回 CSV 文件
- [ ] 后端删除保护逻辑正常：已绑定路由的料号无法删除，返回错误提示
- [ ] 后端删除保护逻辑正常：有工单的料号无法删除，返回错误提示
- [ ] 后端批量删除结果汇总正常：返回成功删除数量和跳过数量
- [ ] 前端删除按钮可触发删除确认对话框
- [ ] 前端删除确认对话框显示料号名称
- [ ] 前端删除成功后列表自动刷新并显示成功提示
- [ ] 前端删除失败时显示错误提示（如已绑定路由）
- [ ] 前端批量删除按钮在无选中时禁用
- [ ] 前端批量删除确认对话框显示删除数量
- [ ] 前端批量删除成功后显示成功和跳过数量汇总
- [ ] 前端表格状态列使用 Switch 开关显示
- [ ] 前端 Switch 开关可正常切换状态
- [ ] 前端状态切换成功后显示成功提示
- [ ] 前端状态切换失败后恢复原状态并显示错误
- [ ] 前端导出按钮可触发导出
- [ ] 前端导出成功后自动下载 CSV 文件
- [ ] 前端导出文件名格式为 `料号列表_YYYYMMDD_HHmmss.csv`
- [ ] 前端导出数据与当前查询条件匹配

## Blocked by

- [02-material-crud-core](issues/02-material-crud-core.md) - 需要核心 CRUD 功能完成

## Technical Details

### 后端接口设计

**单个删除**：`DELETE /api/material/{id}`
- 删除前检查料号是否已绑定路由（route_id != null）
- 删除前检查料号是否有工单关联（需查询工单表，当前工单模块未实现，可预留检查点）
- 若有绑定则抛出 `BusinessException("该料号已绑定路由或已有工单，无法删除")`

**批量删除**：`DELETE /api/material/batch`
- 请求体：`List<Long> ids`
- 逐个检查关联，跳过有绑定的料号
- 返回结果包含成功数量和跳过数量
- 若有跳过项，返回 `BusinessException("成功删除X条，跳过Y条(已绑定路由或有工单)")`

**状态切换**：`PUT /api/material/{id}/status`
- 请求体：`{ enable: 1|0 }`
- 无关联检查，可随时切换

**导出**：`GET /api/material/export`
- 使用 CSV 格式导出
- 文件名格式：`料号列表_YYYYMMDD_HHmmss.csv`
- 导出字段：ID、编码、名称、所属项目、绑定路由、颜色、尺码、spec1-3、描述、备注、状态、创建人、创建时间

### 删除保护逻辑说明

当前工单模块尚未实现，删除保护逻辑可预留检查点：
- 路由绑定检查：立即实现（检查 route_id）
- 工单关联检查：预留接口，当前返回 false（无工单），待工单模块实现后补充

### 前端交互设计

**批量删除结果提示**：
- 使用 `ElMessage` 显示结果
- 格式：`成功删除 X 条，跳过 Y 条（已绑定路由或有工单）`

**导出实现**：
- 调用 API 获取 Blob 数据
- 创建临时下载链接
- 自动触发下载并清理链接

### 参考先例

- 后端删除逻辑：`ProjectServiceImpl.delete()` / `batchDelete()`
- 后端导出逻辑：`ProjectServiceImpl.export()`
- 前端删除交互：`ProjectList.vue` 的 `handleDelete()` / `handleBatchDelete()`
- 前端导出交互：`ProjectList.vue` 的 `handleExport()`

## Comments

(None yet)