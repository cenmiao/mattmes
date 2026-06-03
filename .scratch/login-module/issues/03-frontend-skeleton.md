---
status: completed
---

# 前端项目骨架搭建

## Parent

参考 `.scratch/login-module/PRD.md`

## What to build

搭建Vue3单页应用项目骨架，为后续页面开发提供基础设施。

技术栈：Vue 3.3+ + TypeScript 5.x + Vite 4.x + Vue Router 4.x(Hash模式) + Pinia 2.x + Element Plus 2.x + Axios

## Acceptance criteria

- [x] 使用Vite创建Vue3+TS项目 `mattmes-ui/`
- [x] 配置 `package.json` 包含所有关键依赖：vue, vue-router, pinia, element-plus, @element-plus/icons-vue, axios
- [x] 配置 `vite.config.ts`（Hash路由基础配置）
- [x] 配置 `tsconfig.json` TypeScript编译选项
- [x] 创建基础目录结构：
  - `src/api/` - API请求封装（预留）
  - `src/components/` - 公共组件（预留）
  - `src/directives/` - 自定义指令（预留）
  - `src/router/` - 路由配置（预留）
  - `src/store/` - Pinia状态管理（预留）
  - `src/styles/` - 样式文件
  - `src/utils/` - 工具函数
  - `src/views/` - 页面组件（预留）
- [x] 创建 `src/utils/request.ts` Axios请求封装（基础配置、响应拦截、错误处理）
- [x] 创建 `src/styles/theme.css` 工业精密风格主题CSS（包含PRD定义的色板变量）
  - 主色: #0EA5E9 (Sky-500)
  - 成功: #10B981, 警告: #F59E0B, 错误: #F43F5E
  - 表面色: L0=#0F172A, L1=#1E293B, L2=#334155, L3=#475569
  - 内容色: C1=#F8FAFC, C2=#CBD5E1, C3=#94A3B8, C4=#64748B
- [x] 配置Element Plus全局注册和主题覆盖（使用工业精密风格）
- [x] 创建 `src/main.ts` 入口文件（注册路由、Pinia、Element Plus、全局指令）
- [x] 创建 `src/App.vue` 根组件（基础模板）
- [x] 项目可成功执行 `npm install && npm run dev` 启动开发服务器
- [x] 访问开发服务器显示基础Vue应用页面

## Blocked by

None - 可立即开始

## Comments

_在此追加对话历史和讨论记录_