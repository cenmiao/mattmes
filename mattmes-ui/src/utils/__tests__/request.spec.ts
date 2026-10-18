import { describe, it, expect, vi, beforeEach } from 'vitest'

// 拦截 axios 实例：捕获已注册的响应成功回调，便于直接单测其行为
const { responseHandlers } = vi.hoisted(() => ({
  responseHandlers: {} as { fulfilled?: (r: any) => any }
}))

vi.mock('axios', () => {
  const instance = {
    interceptors: {
      request: { use: vi.fn() },
      response: {
        use: (fulfilled: (r: any) => any) => {
          responseHandlers.fulfilled = fulfilled
        }
      }
    },
    request: vi.fn()
  }
  return { default: { create: () => instance } }
})

import '@/utils/request'

const blobResponse = (blob: Blob, headers: Record<string, string> = {}) => ({
  data: blob,
  headers,
  config: { responseType: 'blob' }
})

describe('request 拦截器：blob 下载响应', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('返回的下载响应保留响应头，供页面读取服务端文件名', () => {
    const blob = new Blob(['\uFEFFID'], { type: 'text/csv' })
    const disposition = 'attachment;filename=%E7%BA%BF%E4%BD%93.csv'

    const result = responseHandlers.fulfilled!(blobResponse(blob, { 'content-disposition': disposition }))

    expect(result.headers['content-disposition']).toBe(disposition)
    expect(result.data).toBe(blob)
  })

  it('返回对象仍可直接当 Blob 使用（兼容既有 new Blob([res]) 写法）', () => {
    const blob = new Blob(['\uFEFFID'], { type: 'text/csv' })

    const result = responseHandlers.fulfilled!(blobResponse(blob))

    expect(result).toBe(blob)
    // 既有调用点 ProcessList 的写法：new Blob([response]) 不应退化为 "[object Object]"
    const rebuilt = new Blob([result as any])
    expect(rebuilt).not.toBeNull()
    expect(rebuilt.size).toBeGreaterThan(0)
  })

  it('非 blob 响应仍走统一 code/message/data 解包', () => {
    const result = responseHandlers.fulfilled!({
      data: { code: 200, message: 'ok', data: { id: 1 } },
      headers: {},
      config: {}
    })

    expect(result).toEqual({ id: 1 })
  })
})
