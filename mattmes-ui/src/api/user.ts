import { get, post, put, del } from '@/utils/request'

export interface UserQuery {
  userNo?: string
  name?: string
  status?: number
  roleId?: number
  page?: number
  size?: number
}

export interface UserCreateRequest {
  userNo: string
  name: string
  password: string
  phone?: string
  email?: string
  roleIds?: number[]
}

export interface UserUpdateRequest {
  name?: string
  phone?: string
  email?: string
  roleIds?: number[]
}

export interface RoleResponse {
  id: number
  roleName: string
  roleCode: string
}

export interface UserResponse {
  id: number
  userNo: string
  name: string
  phone: string | null
  email: string | null
  status: number
  disableReason?: string | null
  roles: RoleResponse[]
  lastLoginTime: string | null
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export function getUsers(params: UserQuery): Promise<PageResult<UserResponse>> {
  return get('/users', params)
}

export function getUser(id: number): Promise<UserResponse> {
  return get(`/users/${id}`)
}

export function createUser(data: UserCreateRequest): Promise<UserResponse> {
  return post('/users', data)
}

export function updateUser(id: number, data: UserUpdateRequest): Promise<UserResponse> {
  return put(`/users/${id}`, data)
}

export function assignRoles(id: number, roleIds: number[]): Promise<void> {
  return put(`/users/${id}/roles`, { roleIds })
}

export function resetPassword(id: number): Promise<{ password: string }> {
  return put(`/users/${id}/reset-password`)
}

export function disableUser(id: number, reason: string): Promise<void> {
  return put(`/users/${id}/disable`, { reason })
}

export function enableUser(id: number): Promise<void> {
  return put(`/users/${id}/enable`)
}

export function deleteUser(id: number): Promise<void> {
  return del(`/users/${id}`)
}
