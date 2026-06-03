import { describe, it, expect, vi, beforeEach } from 'vitest'
import { login, changePassword, type LoginRequest, type LoginResponse, type ChangePasswordRequest } from '@/api/auth'
import * as requestModule from '@/utils/request'

// Mock post 函数
vi.mock('@/utils/request', () => ({
  post: vi.fn()
}))

describe('Auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('login', () => {
    it('应以 userNo 和 password 调用 POST /login', async () => {
      const mockResponse: LoginResponse = {
        token: 'test-token',
        userId: 1,
        userNo: 'EMP001',
        name: '测试用户',
        needChangePassword: false,
        permissions: ['user:read'],
        roles: null
      }

      vi.mocked(requestModule.post).mockResolvedValue(mockResponse)

      const data: LoginRequest = {
        userNo: 'EMP001',
        password: 'Password123'
      }

      const result = await login(data)

      expect(requestModule.post).toHaveBeenCalledWith('/login', data)
      expect(result).toEqual(mockResponse)
    })

    it('支持 forceLogin 参数', async () => {
      const mockResponse: LoginResponse = {
        token: 'test-token',
        userId: 1,
        userNo: 'EMP001',
        name: '测试用户',
        needChangePassword: false,
        permissions: [],
        roles: null
      }

      vi.mocked(requestModule.post).mockResolvedValue(mockResponse)

      const data: LoginRequest = {
        userNo: 'EMP001',
        password: 'Password123',
        forceLogin: true
      }

      await login(data)

      expect(requestModule.post).toHaveBeenCalledWith('/login', data)
    })
  })

  describe('changePassword', () => {
    it('应以完整参数调用 POST /change-password', async () => {
      vi.mocked(requestModule.post).mockResolvedValue(undefined)

      const data: ChangePasswordRequest = {
        oldPassword: 'OldPass123',
        newPassword: 'NewPass456',
        confirmPassword: 'NewPass456',
        isFirstLogin: false
      }

      await changePassword(data)

      expect(requestModule.post).toHaveBeenCalledWith('/change-password', data)
    })

    it('首次登录时 oldPassword 可省略', async () => {
      vi.mocked(requestModule.post).mockResolvedValue(undefined)

      const data: ChangePasswordRequest = {
        newPassword: 'NewPass456',
        confirmPassword: 'NewPass456',
        isFirstLogin: true
      }

      await changePassword(data)

      expect(requestModule.post).toHaveBeenCalledWith('/change-password', data)
      expect(data.oldPassword).toBeUndefined()
    })
  })
})
