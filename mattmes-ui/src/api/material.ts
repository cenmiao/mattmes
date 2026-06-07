import { request } from '@/utils/request'

/**
 * 料号查询请求参数
 */
export interface MaterialQueryRequest {
  code?: string
  name?: string
  projectId?: number
  enable?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 料号响应数据
 */
export interface MaterialResponse {
  id: number
  code: string
  name: string
  projectId: number
  projectName?: string
  routeId?: number
  routeName?: string
  color?: string
  size?: string
  spec1?: string
  spec2?: string
  spec3?: string
  description?: string
  remark?: string
  enable: number
  createdBy?: string
  createTime?: string
  updatedBy?: string
  updateTime?: string
}

/**
 * 分页结果
 */
export interface MaterialPageResult {
  list: MaterialResponse[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * 料号新增请求参数
 */
export interface MaterialAddRequest {
  code: string
  name: string
  projectId: number
  color?: string
  size?: string
  spec1?: string
  spec2?: string
  spec3?: string
  description?: string
  remark?: string
}

/**
 * 料号编辑请求参数
 */
export interface MaterialEditRequest {
  id: number
  name: string
  color?: string
  size?: string
  spec1?: string
  spec2?: string
  spec3?: string
  description?: string
  remark?: string
}

/**
 * 料号简单响应对象（下拉列表用）
 */
export interface MaterialSimpleResponse {
  id: number
  code: string
  name: string
}

/**
 * 查询料号列表
 */
export function queryMaterialList(params: MaterialQueryRequest) {
  return request<MaterialPageResult>({
    url: '/material/list',
    method: 'post',
    data: params
  })
}

/**
 * 获取料号详情
 */
export function getMaterialDetail(id: number) {
  return request<MaterialResponse>({
    url: `/material/${id}`,
    method: 'get'
  })
}

/**
 * 新增料号
 */
export function addMaterial(data: MaterialAddRequest) {
  return request<{ id: number }>({
    url: '/material',
    method: 'post',
    data
  })
}

/**
 * 编辑料号
 */
export function editMaterial(id: number, data: Omit<MaterialEditRequest, 'id'>) {
  return request<{ id: number }>({
    url: `/material/${id}`,
    method: 'put',
    data
  })
}

/**
 * 更新料号启用状态
 */
export function updateMaterialStatus(id: number, enable: number) {
  return request<void>({
    url: `/material/${id}/status`,
    method: 'put',
    params: { enable }
  })
}

/**
 * 删除料号
 */
export function deleteMaterial(id: number) {
  return request<void>({
    url: `/material/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除料号
 */
export function batchDeleteMaterials(ids: number[]) {
  return request<void>({
    url: '/material/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 导出料号数据
 */
export function exportMaterial(params: MaterialQueryRequest) {
  return request<void>({
    url: '/material/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 按项目查询料号列表
 */
export function listMaterialsByProject(projectId: number) {
  return request<MaterialSimpleResponse[]>({
    url: `/material/list-by-project/${projectId}`,
    method: 'get'
  })
}