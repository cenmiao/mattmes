# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

MES管理系统前端 - Vue 3 + TypeScript + Vite + Element Plus

技术栈:
- Vue 3.5 + TypeScript 6.0
- Vite 8.0 + vue-tsc 3.2
- Element Plus 2.3 + @element-plus/icons-vue
- Vue Router 4 (Hash模式)
- Pinia 2.1 状态管理
- Vitest 4.1 + @vue/test-utils 2.4 + happy-dom

## 常用命令

```bash
# 开发服务器 (http://localhost:3000)
npm run dev

# 类型检查 + 生产构建
npm run build

# 运行测试
npm run test

# 单次运行测试
npm run test:run

# 测试覆盖率
npm run test:coverage
```

## 架构概览

### 目录结构

```
src/
├── api/              # API接口定义
├── components/       # 公共组件
├── directives/       # 自定义指令
├── router/           # 路由配置 (index.ts, menus.ts)
├── store/            # Pinia状态管理
├── styles/           # 全局样式 (theme.css)
├── utils/            # 工具函数 (request.ts)
└── views/            # 页面组件
```

### 路由架构

路由分三类:
- **公共路由**: `/login`, `/403`, `/404` - 无需认证
- **认证路由**: `/dashboard`, `/change-password` - 需登录
- **权限路由**: `/users`, `/roles` 等 - 需特定权限

路由守卫逻辑见 `src/router/index.ts:76-117`:
1. 未登录访问受保护路由 → 跳转 `/login`
2. 已登录访问 `/login` → 跳转 `/dashboard`
3. `needChangePassword=true` → 强制跳转 `/change-password`
4. 无权限访问 → 跳转 `/403`

### 权限控制

三层权限控制:
1. **路由级**: `meta.permission` 字段声明所需权限
2. **菜单级**: `src/router/menus.ts` 定义菜单权限映射
3. **按钮级**: `v-permission` 指令 (见 `src/directives/permission.ts`)

超级管理员(`SUPER_ADMIN`角色)自动拥有所有权限。

### HTTP请求

`src/utils/request.ts` 封装axios:
- 自动添加 `Authorization: Bearer <token>` 头
- 响应拦截器处理 401(跳转登录)、403(权限不足)、409(并发登录冲突)
- 统一响应格式: `{ code, message, data }`

### 设计系统

`src/styles/theme.css` 定义CSS变量:
- 主色: `--primary-color: #0EA5E9` (Sky-500)
- 背景层级: `--surface-l0/l1/l2/l3` (#0F172A → #475569)
- 内容层级: `--content-c1/c2/c3/c4` (#F8FAFC → #64748B)
- 字体: 品牌用 JetBrains Mono, 数据用 IBM Plex Mono

### 测试约定

测试文件放在对应模块的 `__tests__` 目录下:
- `src/router/__tests__/` - 路由守卫测试
- `src/components/__tests__/` - 组件测试
- `src/api/__tests__/` - API测试

测试工具: Vitest + @vue/test-utils + happy-dom

## 开发约定

### API模块

新增API时:
1. 在 `src/api/` 下创建或更新模块
2. 定义接口类型 (Request/Response)
3. 使用 `get/post/put/del` 封装函数

### 新增页面

1. 在 `src/views/` 下创建组件
2. 在 `src/router/index.ts` 添加路由
3. 在 `src/router/menus.ts` 添加菜单项(如需侧边栏显示)
4. 路由meta声明权限: `meta: { title: '页面标题', permission: 'xxx:read' }`

### 权限命名

权限采用扁平化格式: `模块:操作`
- 示例: `user:read`, `user:add`, `role:update`, `permission:delete`

### 并发登录处理

登录时遇到409状态码表示账号已在其他设备登录，需显示冲突弹窗让用户选择是否强制登录。实现见 `src/views/Login.vue:154-167`。

## 后端API基础地址

通过环境变量 `VITE_API_BASE_URL` 配置，默认为空(相对路径)。

## 领域上下文

详见 `../CONTEXT.md`，核心概念:
- **SN**: 序列号，产品唯一追溯键
- **过站**: 产品经过工序站点的记录
- **权限模型**: 扁平化 `模块:操作` 格式，无继承
