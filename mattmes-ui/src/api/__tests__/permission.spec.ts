import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getPermissions,
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission,
  type PermissionResponse,
  type PermissionTreeResponse,
  type PermissionCreateRequest,
  type PermissionUpdateRequest
} from '@/api/permission'
import * as requestModule from '@/utils/request'

vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}))

describe('Permission API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getPermissions', () => {
    it('应调用 GET /api/permissions', async () => {
      const mockPermissions: PermissionResponse[] = [
        {
          id: 1,
          permissionName: '用户管理',
          permissionCode: 'user',
          permissionType: 1,
          parentId: null,
          description: '用户管理模块',
          createTime: '2026-01-01 00:00:00'
        }
      ]
      vi.mocked(requestModule.get).mockResolvedValue(mockPermissions)

      const result = await getPermissions()

      expect(requestModule.get).toHaveBeenCalledWith('/permissions')
      expect(result).toEqual(mockPermissions)
    })
  })

  describe('getPermissionTree', () => {
    it('应调用 GET /api/permissions/tree', async () => {
      const mockTree: PermissionTreeResponse[] = [
        {
          id: 1,
          permissionName: '用户管理',
          permissionCode: 'user',
          children: [
            {
              id: 2,
              permissionName: '新增用户',
              permissionCode: 'user:add',
              permissionType: 2,
              parentId: 1,
              description: '新增用户按钮',
              createTime: '2026-01-01 00:00:00'
            }
          ]
        }
      ]
      vi.mocked(requestModule.get).mockResolvedValue(mockTree)

      const result = await getPermissionTree()

      expect(requestModule.get).toHaveBeenCalledWith('/permissions/tree')
      expect(result).toEqual(mockTree)
    })
  })

  describe('createPermission', () => {
    it('应调用 POST /api/permissions 并返回新权限ID', async () => {
      vi.mocked(requestModule.post).mockResolvedValue(3)

      const data: PermissionCreateRequest = {
        permissionName: '编辑用户',
        permissionCode: 'user:edit',
        parentId: 1,
        description: '编辑用户按钮'
      }
      const result = await createPermission(data)

      expect(requestModule.post).toHaveBeenCalledWith('/permissions', data)
      expect(result).toBe(3)
    })

    it('创建模块级权限时 parentId 应为 undefined', async () => {
      vi.mocked(requestModule.post).mockResolvedValue(4)

      const data: PermissionCreateRequest = {
        permissionName: '角色管理',
        permissionCode: 'role'
      }
      const result = await createPermission(data)

      expect(requestModule.post).toHaveBeenCalledWith('/permissions', data)
      expect(result).toBe(4)
    })
  })

  describe('updatePermission', () => {
    it('应调用 PUT /api/permissions/{id}', async () => {
      vi.mocked(requestModule.put).mockResolvedValue(undefined)

      const data: PermissionUpdateRequest = {
        permissionName: '用户管理（改名）',
        description: '更新后的描述'
      }
      await updatePermission(1, data)

      expect(requestModule.put).toHaveBeenCalledWith('/permissions/1', data)
    })
  })

  describe('deletePermission', () => {
    it('应调用 DELETE /api/permissions/{id}', async () => {
      vi.mocked(requestModule.del).mockResolvedValue(undefined)

      await deletePermission(1)

      expect(requestModule.del).toHaveBeenCalledWith('/permissions/1')
    })
  })
})