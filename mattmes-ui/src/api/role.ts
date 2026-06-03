import { get, post, put, del } from '@/utils/request'

export interface RoleQueryParams {
  roleName?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

export interface RoleCreateRequest {
  roleName: string
  roleCode: string
  description?: string
}

export interface RoleUpdateRequest {
  roleName?: string
  description?: string
}

export interface RoleResponse {
  id: number
  roleName: string
  roleCode: string
  description?: string
  userCount: number
  status: number
  createTime: string
  permissions?: { id: number; permissionName: string; permissionCode: string }[]
}

export interface RoleSimpleResponse {
  id: number
  roleName: string
  roleCode: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export function getRoles(params: RoleQueryParams): Promise<PageResult<RoleResponse>> {
  return get('/roles', params)
}

export function getRole(id: number): Promise<RoleResponse> {
  return get(`/roles/${id}`)
}

export function createRole(data: RoleCreateRequest): Promise<RoleResponse> {
  return post('/roles', data)
}

export function updateRole(id: number, data: RoleUpdateRequest): Promise<RoleResponse> {
  return put(`/roles/${id}`, data)
}

export function assignPermissions(id: number, permissionIds: number[]): Promise<void> {
  return put(`/roles/${id}/permissions`, { permissionIds })
}

export function disableRole(id: number): Promise<void> {
  return put(`/roles/${id}/disable`)
}

export function enableRole(id: number): Promise<void> {
  return put(`/roles/${id}/enable`)
}

export function deleteRole(id: number): Promise<void> {
  return del(`/roles/${id}`)
}

export function getAllEnabledRoles(): Promise<RoleSimpleResponse[]> {
  return get('/roles/all')
}
