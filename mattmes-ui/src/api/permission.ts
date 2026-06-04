import { get, post, put, del } from '@/utils/request'

export interface PermissionResponse {
  id: number
  permissionName: string
  permissionCode: string
  permissionType: number  // 1=模块, 2=按钮
  parentId: number | null
  description: string | null
  createTime: string
}

export interface PermissionTreeResponse {
  id: number
  permissionName: string
  permissionCode: string
  children: PermissionResponse[]
}

export interface PermissionCreateRequest {
  permissionName: string
  permissionCode: string
  parentId?: number  // 有值表示按钮级权限
  description?: string
}

export interface PermissionUpdateRequest {
  permissionName: string
  description?: string
}

export function getPermissions(): Promise<PermissionResponse[]> {
  return get('/permissions')
}

export function getPermissionTree(): Promise<PermissionTreeResponse[]> {
  return get('/permissions/tree')
}

export function createPermission(data: PermissionCreateRequest): Promise<number> {
  return post('/permissions', data)
}

export function updatePermission(id: number, data: PermissionUpdateRequest): Promise<void> {
  return put(`/permissions/${id}`, data)
}

export function deletePermission(id: number): Promise<void> {
  return del(`/permissions/${id}`)
}