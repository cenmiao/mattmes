import { post } from '@/utils/request'

export interface LoginRequest {
  userNo: string
  password: string
  forceLogin?: boolean
}

export interface RoleInfo {
  roleId: number
  roleCode: string
  roleName: string
}

export interface LoginResponse {
  token: string
  userId: number
  userNo: string
  name: string
  needChangePassword: boolean
  permissions: string[] | null
  roles: RoleInfo[] | null
}

export interface LogoutResponse {
  success: boolean
}

export interface ChangePasswordRequest {
  oldPassword?: string
  newPassword: string
  confirmPassword: string
  isFirstLogin: boolean
}

export function login(data: LoginRequest): Promise<LoginResponse> {
  return post('/login', data)
}

export function logout(): Promise<LogoutResponse> {
  return post('/logout')
}

export function changePassword(data: ChangePasswordRequest): Promise<void> {
  return post('/change-password', data)
}