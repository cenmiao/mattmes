import { describe, it, expect } from 'vitest'

describe('v-permission 指令逻辑', () => {
  // 提取的核心判断逻辑
  const hasPermission = (
    value: string | string[],
    userInfoStr: string | null
  ): boolean => {
    if (!userInfoStr) return false

    const parsed = JSON.parse(userInfoStr)
    const permissions = parsed.permissions || []
    const roles = parsed.roles || []
    const isSuperAdmin = roles.some((r: { roleCode: string }) => r.roleCode === 'SUPER_ADMIN')

    if (isSuperAdmin) return true

    if (Array.isArray(value)) {
      return value.some(p => permissions.includes(p))
    } else {
      return permissions.includes(value)
    }
  }

  describe('单个权限', () => {
    it('有权限时返回 true', () => {
      const userInfo = JSON.stringify({ permissions: ['user:create'] })
      expect(hasPermission('user:create', userInfo)).toBe(true)
    })

    it('无权限时返回 false', () => {
      const userInfo = JSON.stringify({ permissions: ['user:read'] })
      expect(hasPermission('user:create', userInfo)).toBe(false)
    })
  })

  describe('数组权限（OR 逻辑）', () => {
    it('有任一权限时返回 true', () => {
      const userInfo = JSON.stringify({ permissions: ['user:read'] })
      expect(hasPermission(['user:create', 'user:read'], userInfo)).toBe(true)
    })

    it('无任何权限时返回 false', () => {
      const userInfo = JSON.stringify({ permissions: [] })
      expect(hasPermission(['user:create', 'user:delete'], userInfo)).toBe(false)
    })

    it('数组中所有权限都有时返回 true', () => {
      const userInfo = JSON.stringify({ permissions: ['user:create', 'user:read'] })
      expect(hasPermission(['user:create', 'user:read'], userInfo)).toBe(true)
    })
  })

  describe('超级管理员', () => {
    it('超级管理员返回 true', () => {
      const userInfo = JSON.stringify({
        permissions: [],
        roles: [{ roleCode: 'SUPER_ADMIN' }]
      })
      expect(hasPermission('admin:delete', userInfo)).toBe(true)
    })

    it('超级管理员对数组权限返回 true', () => {
      const userInfo = JSON.stringify({
        permissions: [],
        roles: [{ roleCode: 'SUPER_ADMIN' }]
      })
      expect(hasPermission(['admin:delete', 'system:config'], userInfo)).toBe(true)
    })
  })

  describe('无用户信息', () => {
    it('无 userInfo 时返回 false', () => {
      expect(hasPermission('user:create', null)).toBe(false)
    })

    it('空字符串 userInfo 时返回 false', () => {
      expect(hasPermission('user:create', '')).toBe(false)
    })
  })
})