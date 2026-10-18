import { describe, it, expect, vi, beforeEach } from 'vitest'
import { queryLineList, addLine, updateLineStatus } from '@/api/line'

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
})
