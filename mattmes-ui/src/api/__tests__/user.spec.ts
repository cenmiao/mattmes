import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getUsers,
  getUser,
  createUser,
  updateUser,
  resetPassword,
  disableUser,
  enableUser,
  deleteUser,
  type UserQuery,
  type UserCreateRequest,
  type UserUpdateRequest,
  type UserResponse,
  type PageResult
} from '@/api/user'
import * as requestModule from '@/utils/request'

vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}))

describe('User API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getUsers', () => {
    it('应调用 GET /api/users 并传递查询参数', async () => {
      const mockPage: PageResult<UserResponse> = {
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 10
      }
      vi.mocked(requestModule.get).mockResolvedValue(mockPage)

      const params: UserQuery = { userNo: 'EMP001', page: 1, size: 10 }
      const result = await getUsers(params)

      expect(requestModule.get).toHaveBeenCalledWith('/users', params)
      expect(result).toEqual(mockPage)
    })
  })

  describe('getUser', () => {
    it('应调用 GET /api/users/{id}', async () => {
      const mockUser: UserResponse = {
        id: 1,
        userNo: 'EMP001',
        name: '张三',
        phone: '13800138000',
        email: 'zhangsan@test.com',
        status: 1,
        roles: [],
        lastLoginTime: null
      }
      vi.mocked(requestModule.get).mockResolvedValue(mockUser)

      const result = await getUser(1)

      expect(requestModule.get).toHaveBeenCalledWith('/users/1')
      expect(result).toEqual(mockUser)
    })
  })

  describe('createUser', () => {
    it('应调用 POST /api/users 并传递创建数据', async () => {
      const mockUser: UserResponse = {
        id: 2,
        userNo: 'EMP002',
        name: '李四',
        phone: null,
        email: null,
        status: 1,
        roles: [],
        lastLoginTime: null
      }
      vi.mocked(requestModule.post).mockResolvedValue(mockUser)

      const data: UserCreateRequest = {
        userNo: 'EMP002',
        name: '李四',
        password: 'Test@123',
        roleIds: []
      }
      const result = await createUser(data)

      expect(requestModule.post).toHaveBeenCalledWith('/users', data)
      expect(result).toEqual(mockUser)
    })
  })

  describe('updateUser', () => {
    it('应调用 PUT /api/users/{id} 并传递更新数据', async () => {
      const mockUser: UserResponse = {
        id: 1,
        userNo: 'EMP001',
        name: '张三改',
        phone: '13800138001',
        email: 'new@test.com',
        status: 1,
        roles: [],
        lastLoginTime: null
      }
      vi.mocked(requestModule.put).mockResolvedValue(mockUser)

      const data: UserUpdateRequest = {
        name: '张三改',
        phone: '13800138001',
        email: 'new@test.com'
      }
      const result = await updateUser(1, data)

      expect(requestModule.put).toHaveBeenCalledWith('/users/1', data)
      expect(result).toEqual(mockUser)
    })
  })

  describe('resetPassword', () => {
    it('应调用 PUT /api/users/{id}/reset-password 并返回新密码', async () => {
      vi.mocked(requestModule.put).mockResolvedValue({ password: 'Reset@123' })

      const result = await resetPassword(1)

      expect(requestModule.put).toHaveBeenCalledWith('/users/1/reset-password')
      expect(result).toEqual({ password: 'Reset@123' })
    })
  })

  describe('disableUser', () => {
    it('应调用 PUT /api/users/{id}/disable 并传递禁用原因', async () => {
      vi.mocked(requestModule.put).mockResolvedValue(undefined)

      await disableUser(1, '违规操作')

      expect(requestModule.put).toHaveBeenCalledWith('/users/1/disable', { reason: '违规操作' })
    })
  })

  describe('enableUser', () => {
    it('应调用 PUT /api/users/{id}/enable', async () => {
      vi.mocked(requestModule.put).mockResolvedValue(undefined)

      await enableUser(1)

      expect(requestModule.put).toHaveBeenCalledWith('/users/1/enable')
    })
  })

  describe('deleteUser', () => {
    it('应调用 DELETE /api/users/{id}', async () => {
      vi.mocked(requestModule.del).mockResolvedValue(undefined)

      await deleteUser(1)

      expect(requestModule.del).toHaveBeenCalledWith('/users/1')
    })
  })
})
