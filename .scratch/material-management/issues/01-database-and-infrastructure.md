# Issue: 数据库表与基础架构

Status: completed

## What to build

建立料号管理模块的基础架构，为后续功能开发提供数据层和代码骨架支撑。

创建 `mes_material` 数据库表，包含所有业务字段和审计字段。创建后端实体类、Mapper接口、DTO类、Service接口骨架和Controller骨架。创建前端API文件骨架、空页面组件并配置路由。

## Acceptance criteria

- [x] 数据库表 `mes_material` 创建成功，包含所有必要字段和索引
- [x] 后端 `MesMaterial` 实体类创建完成，使用 MyBatis-Plus 注解
- [x] 后端 `MaterialMapper` 接口创建完成
- [x] 后端所有 DTO 类创建完成（MaterialQueryRequest、MaterialAddRequest、MaterialEditRequest、MaterialResponse、MaterialPageResult、MaterialSimpleResponse）
- [x] 后端 `MaterialService` 接口及空实现类创建完成
- [x] 后端 `MaterialController` 骨架创建完成
- [x] 前端 `src/api/material.ts` 文件创建完成，包含类型定义和 API 函数骨架
- [x] 前端 `src/views/material/MaterialList.vue` 空页面创建完成
- [x] 前端菜单配置完成，显示"料号管理"菜单项
- [x] 后端项目可成功编译
- [x] 前端项目可成功编译
- [ ] 访问 `/materials` 路由可显示空白页面（无报错）

## Blocked by

None - 可立即开始

## Technical Details

### 数据库表设计

创建 `mes_material` 表：

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | BIGINT | PRIMARY KEY AUTO_INCREMENT | 主键 |
| `material_code` | VARCHAR(50) | UNIQUE, NOT NULL | 料号编码（全局唯一） |
| `material_name` | VARCHAR(100) | NOT NULL | 料号名称 |
| `project_id` | BIGINT | NOT NULL | 所属项目ID |
| `route_id` | BIGINT | NULL | 绑定的路由ID |
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

索引：
- 主键索引：`id`
- 唯一索引：`uk_material_code`（material_code）
- 普通索引：`idx_project_id`、`idx_route_id`、`idx_enable`、`idx_deleted`

### 后端模块位置

- 实体类：`mattmes-business/src/main/java/com/matt/mes/business/entity/MesMaterial.java`
- Mapper：`mattmes-business/src/main/java/com/matt/mes/business/mapper/MaterialMapper.java`
- DTO：`mattmes-business/src/main/java/com/matt/mes/business/dto/*.java`
- Service接口：`mattmes-business/src/main/java/com/matt/mes/business/service/MaterialService.java`
- Service实现：`mattmes-business/src/main/java/com/matt/mes/business/service/impl/MaterialServiceImpl.java`
- Controller：`mattmes-web/src/main/java/com/matt/mes/controller/MaterialController.java`

### 前端模块位置

- API文件：`mattmes-ui/src/api/material.ts`
- 页面组件：`mattmes-ui/src/views/material/MaterialList.vue`
- 路由配置：`mattmes-ui/src/router/index.ts`（添加路由）

### 参考先例

参考现有项目管理模块的实现模式：
- 实体类：`MesProject.java`
- Service：`ProjectService.java` / `ProjectServiceImpl.java`
- Controller：`ProjectController.java`
- 前端页面：`ProjectList.vue`

## Comments

(None yet)