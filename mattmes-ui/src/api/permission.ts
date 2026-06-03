import { get } from '@/utils/request'

export interface PermissionResponse {
  id: number
  permissionName: string
  permissionCode: string
  parentId?: number
  children?: PermissionResponse[]
}

export function getPermissions(): Promise<PermissionResponse[]> {
  return get('/permissions')
}
