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
 * 线体编辑请求参数
 *
 * 不含编码字段：线体编码是业务唯一标识，创建后不可修改。
 */
export interface LineEditRequest {
  id: number
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
 * 编辑线体（编码不可修改）
 */
export function editLine(data: LineEditRequest) {
  return request<number>({
    url: '/line/edit',
    method: 'put',
    data
  })
}

/**
 * 切换线体启用状态
 */
export function updateLineStatus(id: number, enable: number) {
  return request<number>({
    url: `/line/status/${id}`,
    method: 'put',
    params: { enable }
  })
}

/**
 * 导出线体数据为 CSV
 *
 * 以 blob 方式接收（响应拦截器对 responseType=blob 直接返回原始数据），
 * 查询条件与列表一致，服务端一次性导出全部匹配记录。
 */
export function exportLine(params: LineQueryRequest) {
  return request<Blob>({
    url: '/line/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
