---
title: 实现项目管理功能
triage-label: ready-for-agent
created: 2026-06-06
prd-path: .scratch/project-management/prd.md
---

# 实现项目管理功能

## 概述

实现MES系统的项目管理模块,用于管理产品型号系列(如iPhone 17)。项目是主数据的顶层分类,料号需要归属于项目。

## PRD文档

完整的PRD文档已创建在: `.scratch/project-management/prd.md`

PRD包含:
- 问题陈述
- 解决方案
- 52个用户故事(覆盖数据管理、查询、导出、权限控制、数据约束、界面交互、菜单导航)
- 实现决策(数据库设计、后端模块、前端模块、权限配置、业务逻辑)
- 测试决策
- 范围界定
- 后续依赖说明

## 开发规范

遵循 `docs/adr/0008-基础数据模块开发规范.md`

## 主要模块

1. **数据库**: `mes_project` 表
2. **后端**:
   - Entity: `MesProject`
   - Mapper: `ProjectMapper`
   - Service: `ProjectService` + `ProjectServiceImpl`
   - Controller: `ProjectController`
   - DTO: Query/Add/Edit Request, Response, PageResult
3. **前端**:
   - API: `src/api/project.ts`
   - 页面: `src/views/project/ProjectList.vue`
   - 路由: `/projects`
   - 菜单: "项目管理" (Folder图标)
4. **权限**: project:read, project:add, project:update, project:delete, project:export

## 关键特性

- 编码唯一性,创建后不可修改
- 删除保护:有料号的项目禁止删除
- 批量删除支持结果汇总
- 启用/禁用状态随时切换
- 支持模糊搜索、分页查询、Excel导出

## 依赖关系

- 项目是独立主数据,不依赖其他模块
- 后续模块依赖项目:料号管理、工单管理、SN管理

## 状态

🟡 ready-for-agent - 等待开发执行