import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getRoles,
  getRole,
  createRole,
  updateRole,
  assignPermissions,
  disableRole,
  enableRole,
  deleteRole,
  type RoleQueryParams,
  type RoleCreateRequest,
  type RoleUpdateRequest,
  type RoleResponse,
  type PageResult
} from '@/api/role'
import * as requestModule from '@/utils/request'

vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}))

describe('Role API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getRoles', () => {
    it('应调用 GET /api/roles 并传递查询参数', async () => {
      const mockPage: PageResult<RoleResponse> = {
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 10
      }
      vi.mocked(requestModule.get).mockResolvedValue(mockPage)

      const params: RoleQueryParams = { roleName: '管理员', pageNum: 1, pageSize: 10 }
      const result = await getRoles(params)

      expect(requestModule.get).toHaveBeenCalledWith('/roles', params)
      expect(result).toEqual(mockPage)
    })
  })

  describe('getRole', () => {
    it('应调用 GET /api/roles/{id}', async () => {
      const mockRole: RoleResponse = {
        id: 1,
        roleName: '管理员',
        roleCode: 'ADMIN',
        description: '系统管理员',
        userCount: 5,
        status: 1,
        createTime: '2026-01-01 00:00:00'
      }
      vi.mocked(requestModule.get).mockResolvedValue(mockRole)

      const result = await getRole(1)

      expect(requestModule.get).toHaveBeenCalledWith('/roles/1')
      expect(result).toEqual(mockRole)
    })
  })

  describe('createRole', () => {
    it('应调用 POST /api/roles 并传递创建数据', async () => {
      const mockRole: RoleResponse = {
        id: 2,
        roleName: '操作员',
        roleCode: 'OPERATOR',
        description: '普通操作员',
        userCount: 0,
        status: 1,
        createTime: '2026-06-03 00:00:00'
      }
      vi.mocked(requestModule.post).mockResolvedValue(mockRole)

      const data: RoleCreateRequest = {
        roleName: '操作员',
        roleCode: 'OPERATOR',
        description: '普通操作员'
      }
      const result = await createRole(data)

      expect(requestModule.post).toHaveBeenCalledWith('/roles', data)
      expect(result).toEqual(mockRole)
    })
  })

  describe('updateRole', () => {
    it('应调用 PUT /api/roles/{id} 并传递更新数据', async () => {
      const mockRole: RoleResponse = {
        id: 1,
        roleName: '管理员改',
        roleCode: 'ADMIN',
        description: '系统管理员（已修改）',
        userCount: 5,
        status: 1,
        createTime: '2026-01-01 00:00:00'
      }
      vi.mocked(requestModule.put).mockResolvedValue(mockRole)

      const data: RoleUpdateRequest = {
        roleName: '管理员改',
        description: '系统管理员（已修改）'
      }
      const result = await updateRole(1, data)

      expect(requestModule.put).toHaveBeenCalledWith('/roles/1', data)
      expect(result).toEqual(mockRole)
    })
  })

  describe('assignPermissions', () => {
    it('应调用 PUT /api/roles/{id}/permissions 并传递权限ID列表', async () => {
      vi.mocked(requestModule.put).mockResolvedValue(undefined)

      await assignPermissions(1, [1, 2, 3])

      expect(requestModule.put).toHaveBeenCalledWith('/roles/1/permissions', { permissionIds: [1, 2, 3] })
    })
  })

  describe('disableRole', () => {
    it('应调用 PUT /api/roles/{id}/disable', async () => {
      vi.mocked(requestModule.put).mockResolvedValue(undefined)

      await disableRole(1)

      expect(requestModule.put).toHaveBeenCalledWith('/roles/1/disable')
    })
  })

  describe('enableRole', () => {
    it('应调用 PUT /api/roles/{id}/enable', async () => {
      vi.mocked(requestModule.put).mockResolvedValue(undefined)

      await enableRole(1)

      expect(requestModule.put).toHaveBeenCalledWith('/roles/1/enable')
    })
  })

  describe('deleteRole', () => {
    it('应调用 DELETE /api/roles/{id}', async () => {
      vi.mocked(requestModule.del).mockResolvedValue(undefined)

      await deleteRole(1)

      expect(requestModule.del).toHaveBeenCalledWith('/roles/1')
    })
  })
})
