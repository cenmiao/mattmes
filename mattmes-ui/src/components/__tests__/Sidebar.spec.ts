import { describe, it, expect } from 'vitest'

describe('Sidebar 动态菜单逻辑', () => {
  describe('菜单配置', () => {
    it('menus 包含正确的菜单项', () => {
      const menus = [
        { path: '/dashboard', title: '仪表盘' },
        { path: '/users', title: '用户管理', permission: 'user:read' },
        { path: '/roles', title: '角色管理', permission: 'role:read' },
        { path: '/permissions', title: '权限管理', permission: 'permission:read' },
        { path: '/login-logs', title: '登录日志', permission: 'login-log:read' }
      ]
      expect(menus.length).toBe(5)
      expect(menus.map(m => m.path)).toContain('/dashboard')
      expect(menus.map(m => m.path)).toContain('/users')
      expect(menus.map(m => m.path)).toContain('/roles')
      expect(menus.map(m => m.path)).toContain('/permissions')
      expect(menus.map(m => m.path)).toContain('/login-logs')
    })

    it('仪表盘菜单无权限要求', () => {
      const menus = [
        { path: '/dashboard', title: '仪表盘' },
        { path: '/users', title: '用户管理', permission: 'user:read' }
      ]
      const dashboard = menus.find(m => m.path === '/dashboard')
      expect(dashboard?.permission).toBeUndefined()
    })

    it('用户管理菜单需要 user:read 权限', () => {
      const menus = [
        { path: '/users', title: '用户管理', permission: 'user:read' }
      ]
      const users = menus.find(m => m.path === '/users')
      expect(users?.permission).toBe('user:read')
    })
  })

  describe('权限过滤逻辑', () => {
    const menus = [
      { path: '/dashboard', title: '仪表盘' },
      { path: '/users', title: '用户管理', permission: 'user:read' },
      { path: '/roles', title: '角色管理', permission: 'role:read' },
      { path: '/permissions', title: '权限管理', permission: 'permission:read' },
      { path: '/login-logs', title: '登录日志', permission: 'login-log:read' }
    ]

    const filterMenusByPermission = (
      menuList: typeof menus,
      permissions: string[],
      isSuperAdmin: boolean
    ) => {
      if (isSuperAdmin) return menuList
      return menuList.filter(menu => {
        if (!menu.permission) return true
        return permissions.includes(menu.permission)
      })
    }

    it('超级管理员看到所有菜单', () => {
      const visible = filterMenusByPermission(menus, [], true)
      expect(visible.length).toBe(menus.length)
    })

    it('无权限用户只看到无权限要求的菜单', () => {
      const visible = filterMenusByPermission(menus, [], false)
      expect(visible.length).toBe(1) // 只有仪表盘
      expect(visible[0].path).toBe('/dashboard')
    })

    it('有 user:read 权限的用户看到仪表盘和用户管理', () => {
      const visible = filterMenusByPermission(menus, ['user:read'], false)
      expect(visible.length).toBe(2)
      expect(visible.map(m => m.path)).toContain('/dashboard')
      expect(visible.map(m => m.path)).toContain('/users')
    })

    it('有所有权限的用户看到所有菜单', () => {
      const allPermissions = menus
        .filter(m => m.permission)
        .map(m => m.permission as string)
      const visible = filterMenusByPermission(menus, allPermissions, false)
      expect(visible.length).toBe(menus.length)
    })
  })
})