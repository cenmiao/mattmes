# Issue: 权限初始化与菜单配置

Status: needs-triage

## Parent

[03-delete-status-export](issues/03-delete-status-export.md)

## What to build

初始化料号管理的权限数据，配置前端路由权限要求，添加侧边栏菜单项，并为所有功能按钮添加权限指令控制。确保权限体系完整，无权限用户看不到对应的菜单和按钮。

## Acceptance criteria

- [ ] 数据库 `sys_permission` 表中存在 5 条料号权限记录：material:read、material:add、material:update、material:delete、material:export
- [ ] 前端路由 `/materials` 配置了 `meta.permission: 'material:read'`
- [ ] 前端侧边栏"基础数据"分组下显示"料号管理"菜单项
- [ ] 前端菜单项配置了 `permission: 'material:read'`
- [ ] 前端菜单项图标使用 `Box`（Element Plus 图标）
- [ ] 前端新增按钮添加了 `v-permission="'material:add'"` 指令
- [ ] 前端编辑按钮添加了 `v-permission="'material:update'"` 指令
- [ ] 前端删除按钮添加了 `v-permission="'material:delete'"` 指令
- [ ] 前端批量删除按钮添加了 `v-permission="'material:delete'"` 指令
- [ ] 前端导出按钮添加了 `v-permission="'material:export'"` 指令
- [ ] 无 material:read 权限的用户不显示"料号管理"菜单项
- [ ] 无 material:add 权限的用户看不到新增按钮
- [ ] 无 material:delete 权限的用户看不到删除和批量删除按钮
- [ ] 无 material:export 权限的用户看不到导出按钮
- [ ] 无 material:read 权限的用户访问 `/materials` 跳转到 403 页面

## Blocked by

- [03-delete-status-export](issues/03-delete-status-export.md) - 需要功能实现完成

## Technical Details

### 权限初始化数据

在数据库 `sys_permission` 表插入以下记录：

| permission_name | permission_code |
|-----------------|-----------------|
| 查看料号 | material:read |
| 新增料号 | material:add |
| 编辑料号 | material:update |
| 删除料号 | material:delete |
| 导出料号 | material:export |

权限采用扁平化命名 `{模块}:{操作}`，符合 CONTEXT.md 定义。

可通过 SQL 初始化脚本或后端初始化逻辑执行。

### 前端路由配置

在 `mattmes-ui/src/router/index.ts` 添加路由：

```typescript
{
  path: '/materials',
  name: 'Materials',
  component: () => import('@/views/material/MaterialList.vue'),
  meta: {
    title: '料号管理',
    permission: 'material:read'
  }
}
```

### 前端菜单配置

在 `mattmes-ui/src/router/menus.ts` 的"基础数据"分组下添加菜单项：

```typescript
{
  path: '/materials',
  title: '料号管理',
  icon: 'Box',
  permission: 'material:read'
}
```

### 权限指令使用

在按钮上使用 `v-permission` 指令：
- `v-permission="'material:add'"` - 新增按钮
- `v-permission="'material:update'"` - 编辑按钮、状态开关
- `v-permission="'material:delete'"` - 删除按钮、批量删除按钮
- `v-permission="'material:export'"` - 导出按钮

### 参考先例

- 权限数据：参考 `sys_permission` 表现有数据结构
- 路由配置：参考 `/projects` 路由配置
- 菜单配置：参考 `menus.ts` 中项目管理菜单项
- 权限指令：参考 `ProjectList.vue` 中按钮权限指令

## Comments

(None yet)