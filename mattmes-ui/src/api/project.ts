import { request } from '@/utils/request'

/**
 * 项目查询请求参数
 */
export interface ProjectQueryRequest {
  code?: string
  name?: string
  enable?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 项目响应数据
 */
export interface ProjectResponse {
  id: number
  code: string
  name: string
  description?: string
  enable: number
  remark?: string
  createdBy?: string
  createTime?: string
  updatedBy?: string
  updateTime?: string
}

/**
 * 分页结果
 */
export interface ProjectPageResult {
  list: ProjectResponse[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * 项目新增请求参数
 */
export interface ProjectAddRequest {
  code: string
  name: string
  description?: string
  enable?: number
  remark?: string
}

/**
 * 项目编辑请求参数
 */
export interface ProjectEditRequest {
  id: number
  name: string
  description?: string
  enable?: number
  remark?: string
}

/**
 * 查询项目列表
 */
export function queryProjectList(params: ProjectQueryRequest) {
  return request<ProjectPageResult>({
    url: '/project/list',
    method: 'post',
    data: params
  })
}

/**
 * 新增项目
 */
export function addProject(data: ProjectAddRequest) {
  return request<{ id: number }>({
    url: '/project/add',
    method: 'post',
    data
  })
}

/**
 * 编辑项目
 */
export function editProject(data: ProjectEditRequest) {
  return request<{ id: number }>({
    url: '/project/edit',
    method: 'put',
    data
  })
}

/**
 * 更新项目启用状态
 */
export function updateProjectStatus(id: number, enable: number) {
  return request<void>({
    url: `/project/status/${id}`,
    method: 'put',
    params: { enable }
  })
}

/**
 * 删除项目
 */
export function deleteProject(id: number) {
  return request<void>({
    url: `/project/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除项目
 */
export function batchDeleteProjects(ids: number[]) {
  return request<void>({
    url: '/project/batchDelete',
    method: 'delete',
    data: ids
  })
}

/**
 * 导出项目数据
 */
export function exportProject(params: ProjectQueryRequest) {
  return request<void>({
    url: '/project/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
