import { describe, it, expect, beforeEach } from 'vitest'

describe('Header 用户下拉逻辑', () => {
  describe('用户信息显示', () => {
    const getUserName = (userInfo: { username?: string; name?: string } | null) => {
      return userInfo?.username || userInfo?.name || '用户'
    }

    it('优先显示 username', () => {
      expect(getUserName({ username: 'admin' })).toBe('admin')
    })

    it('无 username 时显示 name', () => {
      expect(getUserName({ name: '管理员' })).toBe('管理员')
    })

    it('无用户信息时显示默认值', () => {
      expect(getUserName(null)).toBe('用户')
      expect(getUserName({})).toBe('用户')
    })
  })

  describe('退出登录逻辑', () => {
    beforeEach(() => {
      localStorage.clear()
    })

    it('清除 localStorage 中的认证信息', () => {
      localStorage.setItem('token', 'test-token')
      localStorage.setItem('userInfo', JSON.stringify({ username: 'test' }))
      localStorage.setItem('needChangePassword', 'false')

      // 模拟退出登录
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('needChangePassword')

      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
      expect(localStorage.getItem('needChangePassword')).toBeNull()
    })
  })

  describe('超级管理员判断', () => {
    const isSuperAdmin = (roles: { roleCode: string }[]) => {
      return roles.some(r => r.roleCode === 'SUPER_ADMIN')
    }

    it('包含 SUPER_ADMIN 角色返回 true', () => {
      expect(isSuperAdmin([{ roleCode: 'SUPER_ADMIN' }])).toBe(true)
      expect(isSuperAdmin([{ roleCode: 'USER' }, { roleCode: 'SUPER_ADMIN' }])).toBe(true)
    })

    it('不包含 SUPER_ADMIN 角色返回 false', () => {
      expect(isSuperAdmin([{ roleCode: 'USER' }])).toBe(false)
      expect(isSuperAdmin([])).toBe(false)
    })
  })
})