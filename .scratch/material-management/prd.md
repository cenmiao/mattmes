# PRD: 料号管理功能

## Problem Statement

MES系统需要实现料号管理功能，用于管理具体产品变体（如 iPhone 17 金色 5G版）。料号是MES系统的核心主数据，向上归属于项目，向下关联路由和工单。目前系统已完成项目管理模块，缺少料号管理模块，导致无法定义具体产品变体，后续的路由管理、工单管理等业务无法正常开展。

## Solution

实现一个完整的料号管理模块，包括料号的增删改查、状态管理、批量删除和导出功能。料号作为主数据，具有以下特性：

- **归属项目**：每个料号必须归属于一个启用的项目
- **规格属性**：支持颜色、尺码及三个通用规格字段
- **路由绑定**：料号可绑定路由（在路由模块中实现），有工单后禁止解绑
- **删除保护**：有路由绑定或工单的料号禁止删除
- **编码唯一**：料号编码全局唯一，创建后不可修改

料号数据支持分页查询、按项目筛选、模糊搜索、状态筛选和CSV导出，为路由管理、工单管理等功能提供基础数据支撑。

## User Stories

### 数据管理

1. 作为MES管理员，我想要创建新料号（如 iPhone 17 金色 5G版），以便对具体产品变体进行管理
2. 作为MES管理员，我想要为料号设置唯一编码，以便在系统中快速识别和引用料号
3. 作为MES管理员，我想要为料号指定所属项目，以便建立料号与项目的归属关系
4. 作为MES管理员，我想要为料号填写颜色规格，以便记录产品的颜色属性
5. 作为MES管理员，我想要为料号填写尺码规格，以便记录产品的尺码属性
6. 作为MES管理员，我想要为料号填写通用规格字段，以便灵活记录其他产品属性
7. 作为MES管理员，我想要编辑料号名称和规格，以便完善料号信息
8. 作为MES管理员，我想要删除不再使用的料号，以便清理历史数据
9. 作为MES管理员，我想要批量删除多个料号，以便快速清理一批废弃料号
10. 作为MES管理员，我想要在删除料号时得到保护（如果料号已绑定路由或有工单则禁止删除），以便防止误删有业务数据的料号
11. 作为MES管理员，我想要启用或禁用料号，以便控制料号是否可用于新业务
12. 作为MES管理员，我想要在创建料号时必须选择一个启用的项目，以便保证数据完整性
13. 作为MES管理员，我想要在创建料号后无法修改所属项目，以便保证归属关系稳定

### 数据查询

14. 作为MES管理员，我想要查看料号列表，以便了解系统中有哪些产品变体
15. 作为MES管理员，我想要在料号列表中看到料号编码、名称、所属项目、绑定路由、颜色、尺码、状态、创建人和创建时间，以便全面了解料号信息
16. 作为MES管理员，我想要在料号列表中对料号进行分页展示，以便应对大量料号数据
17. 作为MES管理员，我想要通过料号编码模糊搜索料号，以便快速找到特定料号
18. 作为MES管理员，我想要通过料号名称模糊搜索料号，以便快速找到特定料号
19. 作为MES管理员，我想要按项目筛选料号，以便查看特定项目下的所有料号
20. 作为MES管理员，我想要按启用状态筛选料号（启用/禁用），以便查看特定状态的料号
21. 作为MES管理员，我想要在搜索后重置查询条件，以便恢复查看全部料号
22. 作为MES管理员，我想要在项目下拉框中只看到启用的项目，以便选择有效的项目进行筛选

### 数据导出

23. 作为MES管理员，我想要导出料号列表到CSV文件，以便在系统外部使用料号数据
24. 作为MES管理员，我想要在导出时包含完整的料号信息，以便导出数据完整可用
25. 作为MES管理员，我想要导出文件自动命名包含时间戳，以便区分不同时间导出的文件

### 权限控制

26. 作为系统管理员，我想要为料号管理设置独立的权限，以便精细化控制用户对料号功能的访问
27. 作为系统管理员，我想要控制哪些用户可以查看料号列表（material:read权限），以便保护料号数据隐私
28. 作为系统管理员，我想要控制哪些用户可以新增料号（material:add权限），以便限制料号创建权限
29. 作为系统管理员，我想要控制哪些用户可以编辑料号（material:update权限），以便限制料号修改权限
30. 作为系统管理员，我想要控制哪些用户可以删除料号（material:delete权限），以便限制料号删除权限
31. 作为系统管理员，我想要控制哪些用户可以导出料号（material:export权限），以便限制料号数据导出权限
32. 作为普通用户，我想要在没有material:add权限时看不到新增按钮，以便界面简洁且符合权限要求
33. 作为普通用户，我想要在没有material:delete权限时看不到删除和批量删除按钮，以便界面简洁且符合权限要求
34. 作为普通用户，我想要在没有material:export权限时看不到导出按钮，以便界面简洁且符合权限要求

### 数据约束

35. 作为MES管理员，我想要在创建料号时必须填写料号编码，以便每个料号都有唯一标识
36. 作为MES管理员，我想要料号编码最多50个字符，以便编码简洁易用
37. 作为MES管理员，我想要料号编码只能包含字母、数字、下划线、中划线，以便编码符合系统规范
38. 作为MES管理员，我想要料号编码全局唯一，以便避免料号混淆
39. 作为MES管理员，我想要料号编码创建后不可修改，以便保证业务标识稳定
40. 作为MES管理员，我想要在创建料号时必须填写料号名称，以便料号易于识别
41. 作为MES管理员，我想要料号名称最多100个字符，以便名称简洁易用
42. 作为MES管理员，我想要料号名称可随时修改，以便完善料号命名
43. 作为MES管理员，我想要在创建料号时必须选择所属项目，以便建立归属关系
44. 作为MES管理员，我想要所属项目创建后不可修改，以便保证归属关系稳定
45. 作为MES管理员，我想要颜色、尺码、通用规格字段可选填写，以便灵活记录产品属性
46. 作为MES管理员，我想要颜色、尺码最多50个字符，以便属性值简洁
47. 作为MES管理员，我想要通用规格字段最多100个字符，以便属性值详尽
48. 作为MES管理员，我想要描述和备注可选填写，最多500个字符

### 界面交互

49. 作为MES管理员，我想要在新增料号时通过弹窗表单输入信息，以便操作便捷不离开列表页
50. 作为MES管理员，我想要在编辑料号时通过弹窗表单修改信息，以便操作便捷不离开列表页
51. 作为MES管理员，我想要在弹窗表单中看到编码字段在编辑时禁用，以便明确编码不可修改
52. 作为MES管理员，我想要在弹窗表单中看到所属项目字段在编辑时禁用，以便明确项目不可修改
53. 作为MES管理员，我想要在项目下拉框中按项目编码排序，以便快速找到目标项目
54. 作为MES管理员，我想要在表单提交时自动验证字段格式和长度，以便提前发现错误
55. 作为MES管理员，我想要在表单验证失败时看到明确的错误提示，以便快速纠正错误
56. 作为MES管理员，我想要在删除料号前看到确认对话框，以便防止误删
57. 作为MES管理员，我想要在批量删除前看到确认对话框显示删除数量，以便确认操作
58. 作为MES管理员，我想要在批量删除后看到成功和跳过的数量汇总，以便了解删除结果
59. 作为MES管理员，我想要在料号列表中通过Switch开关快速切换料号状态，以便操作便捷
60. 作为MES管理员，我想要在料号列表中通过复选框选择多个料号，以便批量操作
61. 作为MES管理员，我想要在料号列表中通过操作按钮编辑或删除单个料号，以便灵活操作
62. 作为MES管理员，我想要在料号列表中看到绑定路由信息，以便了解料号的路由绑定状态

### 菜单导航

63. 作为MES用户，我想要在侧边栏"基础数据"分组下看到"料号管理"菜单项，以便快速访问料号管理功能
64. 作为MES用户，我想要在没有material:read权限时不看到"料号管理"菜单项，以便菜单符合权限要求

## Implementation Decisions

### 数据库设计

创建 `mes_material` 表，字段包括：

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | BIGINT | PRIMARY KEY AUTO_INCREMENT | 主键 |
| `material_code` | VARCHAR(50) | UNIQUE, NOT NULL | 料号编码（全局唯一） |
| `material_name` | VARCHAR(100) | NOT NULL | 料号名称 |
| `project_id` | BIGINT | NOT NULL | 所属项目ID |
| `route_id` | BIGINT | NULL | 绑定的路由ID（创建路由时更新） |
| `color` | VARCHAR(50) | NULL | 颜色 |
| `size` | VARCHAR(50) | NULL | 尺码 |
| `spec1` | VARCHAR(100) | NULL | 通用规格1 |
| `spec2` | VARCHAR(100) | NULL | 通用规格2 |
| `spec3` | VARCHAR(100) | NULL | 通用规格3 |
| `description` | VARCHAR(500) | NULL | 描述 |
| `remark` | VARCHAR(500) | NULL | 备注 |
| `enable` | TINYINT | DEFAULT 1 | 启用状态(1=启用,0=禁用) |
| `deleted` | TINYINT | DEFAULT 0 | 逻辑删除(0=未删除,1=已删除) |
| `created_by` | VARCHAR(50) | NULL | 创建人 |
| `create_time` | DATETIME | NULL | 创建时间 |
| `updated_by` | VARCHAR(50) | NULL | 更新人 |
| `update_time` | DATETIME | NULL | 更新时间 |

索引设计：
- 主键索引：`id`
- 唯一索引：`uk_material_code`（material_code）
- 普通索引：`idx_project_id`（project_id）、`idx_route_id`（route_id）、`idx_enable`（enable）、`idx_deleted`（deleted）

### 后端模块

创建以下模块（遵循ADR-0008基础数据模块开发规范）：

**实体层**：`MesMaterial` 实体类
- 使用MyBatis-Plus注解映射数据库表
- 审计字段使用 `@TableField(fill = FieldFill.INSERT/UPDATE)` 自动填充
- 逻辑删除使用 `@TableLogic` 注解

**数据访问层**：`MaterialMapper` 接口
- 继承 `BaseMapper<MesMaterial>`
- 自定义方法：`countByCodeIncludeDeleted(String code)` 用于唯一性校验

**业务逻辑层**：`MaterialService` 接口及实现 `MaterialServiceImpl`
- 方法设计：
  - `page(MaterialQueryRequest)` - 分页查询
  - `getById(Long id)` - 获取详情
  - `add(MaterialAddRequest)` - 新增料号（包含编码唯一性校验、项目存在性校验）
  - `edit(MaterialEditRequest)` - 编辑料号
  - `delete(Long id)` - 删除料号（包含路由和工单关联检查）
  - `batchDelete(List<Long> ids)` - 批量删除（包含关联检查和结果汇总）
  - `updateStatus(Long id, Integer enable)` - 更新启用状态
  - `listByProjectId(Long projectId)` - 按项目查询料号列表
  - `export(MaterialQueryRequest, HttpServletResponse)` - 导出CSV

**Controller层**：`MaterialController`
- REST API路径：`/api/material`
- 接口设计：
  - `GET /api/material/page` - 分页查询
  - `GET /api/material/{id}` - 获取详情
  - `POST /api/material` - 新增料号
  - `PUT /api/material/{id}` - 编辑料号
  - `DELETE /api/material/{id}` - 删除料号
  - `DELETE /api/material/batch` - 批量删除
  - `PUT /api/material/{id}/status` - 修改状态
  - `GET /api/material/export` - 导出数据
  - `GET /api/material/list-by-project/{projectId}` - 按项目查询料号列表

**DTO对象**：
- `MaterialQueryRequest` - 查询请求（code, name, projectId, enable, pageNum, pageSize）
- `MaterialAddRequest` - 新增请求（code, name, projectId, color, size, spec1, spec2, spec3, description, remark）
- `MaterialEditRequest` - 编辑请求（id, name, color, size, spec1, spec2, spec3, description, remark）
- `MaterialResponse` - 响应对象（完整字段，包含项目名称、路由名称）
- `MaterialPageResult` - 分页结果（列表字段，包含项目名称、路由名称）
- `MaterialSimpleResponse` - 简单响应对象（id, code, name，用于下拉列表）

### 项目模块更新

**新增接口**：`ProjectService` 添加方法
- `listEnabled()` - 获取启用项目列表（用于下拉选择）

**新增接口**：`ProjectController`
- `GET /api/project/list` - 获取启用项目下拉列表

**新增DTO**：
- `ProjectSimpleResponse` - 简单响应对象（id, code, name）

### 前端模块

**API封装**：`src/api/material.ts`
- TypeScript类型定义：
  - `MaterialQueryRequest`, `MaterialAddRequest`, `MaterialEditRequest`
  - `MaterialResponse`, `MaterialPageResult`, `MaterialSimpleResponse`
- API函数：
  - `queryMaterialList` - 查询列表
  - `getMaterialDetail` - 获取详情
  - `addMaterial` - 新增料号
  - `editMaterial` - 编辑料号
  - `deleteMaterial` - 删除料号
  - `batchDeleteMaterials` - 批量删除
  - `updateMaterialStatus` - 修改状态
  - `exportMaterial` - 导出数据
  - `listMaterialsByProject` - 按项目查询料号列表

**API更新**：`src/api/project.ts`
- 新增 `listEnabledProjects` - 获取启用项目列表

**页面组件**：`src/views/material/MaterialList.vue`
- 页面结构参考 `ProjectList.vue`：
  - 查询表单区域：编码、名称、项目（下拉选择）、状态输入框 + 查询/重置/新增/批量删除/导出按钮
  - 数据表格：复选框、ID、编码、名称、所属项目、绑定路由、颜色、尺码、状态、创建人、创建时间、操作列
  - 新增/编辑弹窗：表单字段 + 验证规则
- 表单验证规则（遵循ADR-0008）：
  - 编码：必填，最多50字符，正则 `^[a-zA-Z0-9_-]+$`
  - 名称：必填，最多100字符
  - 所属项目：必填
  - 颜色/尺码：非必填，最多50字符
  - 通用规格：非必填，最多100字符
  - 描述/备注：非必填，最多500字符
- 权限指令：`v-permission="'material:add'"` 等应用于按钮

**路由配置**：`src/router/index.ts`
- 路径：`/materials`
- 组件：`() => import('@/views/material/MaterialList.vue')`
- 元信息：`{ title: '料号管理', permission: 'material:read' }`

**菜单配置**：`src/router/menus.ts`
- 添加菜单项：
  - path: `/materials`
  - title: `料号管理`
  - icon: `Box`（Element Plus图标）
  - permission: `material:read`

### 权限配置

预置以下权限到 `sys_permission` 表：
- `material:read` - 查看料号
- `material:add` - 新增料号
- `material:update` - 编辑料号
- `material:delete` - 删除料号
- `material:export` - 导出料号

权限采用扁平化命名 `{模块}:{操作}`，符合CONTEXT.md定义的权限体系。

### 业务逻辑约束

**编码唯一性校验**：
新增料号时，检查数据库是否存在相同编码的料号（包括已删除记录），若存在则抛出 `BusinessException("料号编码已存在")`。

**项目存在性校验**：
新增料号时，检查所选项目是否存在且启用，若不存在或已禁用则抛出 `BusinessException("所选项目不存在或已禁用")`。

**删除保护逻辑**：
- 单个删除：检查料号是否已绑定路由（route_id不为空）或是否有工单关联，若有则抛出 `BusinessException("该料号已绑定路由或已有工单，无法删除")`
- 批量删除：逐个检查关联，跳过有绑定的料号，最后汇总结果。若有跳过项，抛出 `BusinessException("成功删除X条，跳过Y条(已绑定路由或有工单)")`

**状态切换**：
可随时切换启用/禁用状态，无需检查关联数据。

**导出实现**：
使用CSV格式导出（参考项目模块实现），文件名格式：`料号列表_YYYYMMDD_HHmmss.csv`

### 前端交互细节

**项目下拉框**：
- 新增弹窗中，项目下拉框只显示启用状态的项目
- 项目下拉框按项目编码排序
- 查询表单中的项目下拉框包含"全部"选项

**路由绑定显示**：
表格中显示路由名称，未绑定时显示"未绑定"文本。

**批量删除结果**：
批量删除后，通过 `ElMessage` 提示成功和跳过数量，若有跳过项则显示详细原因。

## Testing Decisions

### 测试原则

- 仅测试外部行为，不测试实现细节
- 使用集成测试验证业务逻辑正确性
- 使用端到端测试验证用户交互流程

### 后端测试

**测试模块**：`MaterialService` 业务逻辑层

**测试场景**：
1. 编码唯一性校验测试
   - 新增料号时编码重复应抛出异常
   - 新增料号时编码唯一应成功创建

2. 项目校验测试
   - 新增料号时项目不存在应抛出异常
   - 新增料号时项目已禁用应抛出异常
   - 新增料号时项目存在且启用应成功创建

3. 删除保护测试
   - 删除无绑定的料号应成功
   - 删除已绑定路由的料号应抛出异常
   - 删除有工单的料号应抛出异常
   - 批量删除混合场景应返回正确的成功和跳过数量

4. 状态切换测试
   - 启用料号应成功
   - 禁用料号应成功

5. 分页查询测试
   - 查询应返回正确的分页数据
   - 模糊搜索应正确过滤结果
   - 按项目筛选应正确过滤结果

**参考先例**：`ProjectServiceTest`（项目服务测试）

### 前端测试

**测试模块**：`MaterialList.vue` 页面组件

**测试场景**：
1. 表单验证测试
   - 编码格式错误应显示错误提示
   - 名称超长应显示错误提示
   - 未选择项目应显示错误提示

2. 权限控制测试
   - 无material:add权限时新增按钮应隐藏
   - 无material:delete权限时删除按钮应隐藏

**参考先例**：`ProjectList.vue`

### API测试

**测试方式**：使用永久Token直接调用后端API进行测试

**测试场景**：
1. 新增料号API
2. 编辑料号API
3. 删除料号API
4. 批量删除API
5. 状态切换API
6. 导出API
7. 按项目查询料号列表API

## Out of Scope

以下内容不在本次PRD范围内：

1. **路由管理**：本次只实现料号管理，路由管理后续单独开发
2. **路由绑定/解绑操作**：料号绑定路由的功能在路由模块中实现
3. **料号与工单的联动展示**：料号列表不显示工单数量或工单列表
4. **料号统计分析**：不实现料号维度的统计分析功能
5. **料号类型字段**：料号不设置类型字段
6. **料号版本管理**：料号不支持版本控制
7. **料号替代关系**：不支持料号之间的替代关系
8. **料号BOM结构**：料号不支持BOM（物料清单）结构
9. **料号导入功能**：首期不支持批量导入，后续迭代
10. **富文本编辑**：描述和备注字段使用普通文本，不支持富文本格式
11. **料号图片/附件**：料号不支持上传图片或附件
12. **料号审批流程**：料号创建、修改、删除不需要审批流程

## Further Notes

### 开发顺序建议

按照ADR-0008基础数据模块开发规范的建议顺序：
1. 创建数据库表 `mes_material`
2. 后端开发：Entity → Mapper → Service → Controller
3. 更新项目模块：添加项目下拉列表接口
4. 后端测试：编写Service单元测试
5. 前端开发：API封装 → 页面组件 → 路由配置
6. 权限配置：初始化权限数据
7. 集成测试：API测试、前端功能测试

### 后续依赖模块

料号完成后，后续需要依赖料号数据的模块：
- **路由管理**：路由创建时可绑定多个料号
- **工单管理**：工单需要指定料号
- **SN管理**：SN通过工单关联料号
- **报表统计**：可能需要按料号维度统计生产数据

### 数据迁移考虑

如果系统已有历史数据需要迁移：
- 需要准备料号数据迁移脚本
- 料号编码需要符合新系统规范（字母、数字、下划线、中划线）
- 迁移后需要验证编码唯一性
- 需要确保料号所属的项目已迁移

### 性能考虑

料号数据量预估：
- 典型制造企业料号数量：1000-10000个
- 数据量中等，分页查询默认pageSize=10足够
- 导出功能支持全量导出（不分页）
- 项目下拉框数据量较小，无需分页

### 与项目管理的对比

料号管理与项目管理的主要差异：

| 特性 | 项目管理 | 料号管理 |
|------|---------|---------|
| 归属关系 | 独立存在 | 必须归属于项目 |
| 删除保护 | 检查料号关联 | 检查路由和工单关联 |
| 规格字段 | 无 | 有（颜色、尺码、spec1/2/3） |
| 外键关联 | 无 | project_id, route_id |
| 下拉列表接口 | 需新增 | 需新增（按项目查询） |

### API路径统一

本次遵循项目管理模块的API设计模式：
- 分页查询使用 `GET /page`
- 新增使用 `POST /`
- 编辑使用 `PUT /{id}`
- 删除使用 `DELETE /{id}`
