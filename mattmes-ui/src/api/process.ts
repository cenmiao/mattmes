import request from '@/utils/request'

/**
 * 工序查询请求参数
 */
export interface ProcessQueryRequest {
  code?: string
  name?: string
  processType?: string
  enable?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 工序响应数据
 */
export interface ProcessResponse {
  id: number
  code: string
  name: string
  processType: string
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
export interface ProcessPageResult {
  list: ProcessResponse[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * 工序新增请求参数
 */
export interface ProcessAddRequest {
  code: string
  name: string
  processType: string
  description?: string
  enable?: number
  remark?: string
}

/**
 * 查询工序列表
 */
export function queryProcessList(params: ProcessQueryRequest) {
  return request<ProcessPageResult>({
    url: '/process/list',
    method: 'post',
    data: params
  })
}

/**
 * 新增工序
 */
export function addProcess(data: ProcessAddRequest) {
  return request<{ id: number }>({
    url: '/process/add',
    method: 'post',
    data
  })
}
