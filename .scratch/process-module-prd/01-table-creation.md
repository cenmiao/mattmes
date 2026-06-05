---
name: process-table-creation
description: 创建工序管理数据库表
labels: [done]
completed: 2026-06-04
---

## Parent

工序管理模块PRD - `.scratch/process-module-prd.md`

## What to build

创建`mes_process`数据库表，为工序管理模块提供数据存储基础。

表结构：
- `id` BIGINT 主键自增
- `code` VARCHAR(50) 唯一索引，工序编码
- `name` VARCHAR(100) 工序名称
- `process_type` VARCHAR(20) 工序类型枚举(INSPECTION/ASSEMBLY/PACKAGING/OTHER)
- `description` VARCHAR(500) 工序描述
- `enable` TINYINT 启用状态(1启用/0禁用)，默认1
- `remark` VARCHAR(500) 备注
- 审计字段: `created_by`, `create_time`, `updated_by`, `update_time`
- `deleted` TINYINT 逻辑删除(0未删除/1已删除)，默认0

索引：
- UNIQUE INDEX on `code`
- INDEX on `process_type`
- INDEX on `enable`

## Acceptance criteria

- [x] 数据库表`mes_process`创建成功
- [x] 所有字段类型和约束正确
- [x] 唯一索引和普通索引创建成功
- [x] 表符合审计字段规范

## Blocked by

None - 可立即开始

---

## Completion Notes

**2026-06-04 完成记录:**

- 状态: `ready-for-agent` → `done`
- 实现方式: 直接使用MCP数据库工具创建表，同时创建对应的Java实体类和枚举类

**创建的文件:**
1. `mattmes/mattmes-business/src/main/resources/db/migration/V1__create_process_table.sql` - SQL迁移脚本
2. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/entity/MesProcess.java` - 工序实体类
3. `mattmes/mattmes-business/src/main/java/com/matt/mes/business/enums/ProcessType.java` - 工序类型枚举
4. `mattmes/mattmes-business/pom.xml` - 业务模块配置

**验证结果:**
- 项目编译成功 (`mvn clean compile`)
- 数据库表已创建并验证