import { describe, it, expect, vi, beforeEach } from 'vitest'
import { queryLineList, addLine, updateLineStatus, exportLine } from '@/api/line'

// Mock 整个 request 模块
vi.mock('@/utils/request', () => ({
  request: vi.fn()
}))

import { request } from '@/utils/request'

describe('Line API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('queryLineList 应调用 POST /line/list', async () => {
    vi.mocked(request).mockResolvedValue({ list: [], total: 0, pageNum: 1, pageSize: 10 })

    await queryLineList({ code: 'L1', pageNum: 1, pageSize: 10 })

    expect(request).toHaveBeenCalledWith({
      url: '/line/list',
      method: 'post',
      data: { code: 'L1', pageNum: 1, pageSize: 10 }
    })
  })

  it('addLine 应调用 POST /line/add', async () => {
    vi.mocked(request).mockResolvedValue(1)

    await addLine({ code: 'L1', name: '一号线', enable: 1 })

    expect(request).toHaveBeenCalledWith({
      url: '/line/add',
      method: 'post',
      data: { code: 'L1', name: '一号线', enable: 1 }
    })
  })

  it('updateLineStatus 应调用 PUT /line/status/{id} 并以 query 传 enable', async () => {
    vi.mocked(request).mockResolvedValue(7)

    const result = await updateLineStatus(7, 0)

    expect(result).toBe(7)
    expect(request).toHaveBeenCalledWith({
      url: '/line/status/7',
      method: 'put',
      params: { enable: 0 }
    })
  })

  it('exportLine 应以 blob 方式请求 GET /line/export 并透传筛选条件', async () => {
    const blob = new Blob(['\uFEFFID'], { type: 'text/csv;charset=UTF-8' })
    const contentDisposition = 'attachment;filename=%E7%BA%BF%E4%BD%93%E6%95%B0%E6%8D%AE_20260102_030405.csv'
    vi.mocked(request).mockResolvedValue({ data: blob, headers: { 'content-disposition': contentDisposition } })

    const result = await exportLine({ code: 'L1', name: '一号线', enable: 1 })

    expect(request).toHaveBeenCalledWith({
      url: '/line/export',
      method: 'get',
      params: { code: 'L1', name: '一号线', enable: 1 },
      responseType: 'blob'
    })
    // 响应头必须透传给调用方，供页面使用服务端文件名
    expect(result.data).toBe(blob)
    expect(result.headers['content-disposition']).toBe(contentDisposition)
  })

  it('exportLine 的 headers 在服务端未返回时为 undefined 也不报错', async () => {
    const blob = new Blob(['\uFEFFID'])
    vi.mocked(request).mockResolvedValue({ data: blob, headers: {} })

    const result = await exportLine({})

    expect(result.data).toBe(blob)
    expect(result.headers['content-disposition']).toBeUndefined()
  })
})
