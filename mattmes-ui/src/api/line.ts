import { request } from '@/utils/request'

/**
 * 线体查询请求参数
 */
export interface LineQueryRequest {
  code?: string
  name?: string
  enable?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 线体响应数据
 */
export interface LineResponse {
  id: number
  code: string
  name: string
  description?: string
  enable: number
  createdBy?: string
  createTime?: string
  updatedBy?: string
  updateTime?: string
}

/**
 * 分页结果
 */
export interface LinePageResult {
  list: LineResponse[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * 线体新增请求参数
 */
export interface LineAddRequest {
  code: string
  name: string
  description?: string
  enable?: number
}

/**
 * 查询线体列表
 */
export function queryLineList(params: LineQueryRequest) {
  return request<LinePageResult>({
    url: '/line/list',
    method: 'post',
    data: params
  })
}

/**
 * 新增线体
 */
export function addLine(data: LineAddRequest) {
  return request<number>({
    url: '/line/add',
    method: 'post',
    data
  })
}

/**
 * 删除单条线体（逻辑删除）
 */
export function deleteLine(id: number) {
  return request<number>({
    url: `/line/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除线体（逻辑删除）
 */
export function batchDeleteLines(ids: number[]) {
  return request<number[]>({
    url: '/line/batchDelete',
    method: 'delete',
    data: ids
  })
}
