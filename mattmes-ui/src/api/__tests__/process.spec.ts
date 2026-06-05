import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  queryProcessList,
  addProcess,
  type ProcessQueryRequest,
  type ProcessAddRequest,
  type ProcessPageResult
} from '@/api/process'

// Mock整个request模块
vi.mock('@/utils/request', () => ({
  default: vi.fn()
}))

// 导入mock后的request
import request from '@/utils/request'

describe('Process API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('queryProcessList', () => {
    it('应调用 POST /process/list 并传递查询参数', async () => {
      const mockPage: ProcessPageResult = {
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 10
      }
      vi.mocked(request).mockResolvedValue(mockPage)

      const params: ProcessQueryRequest = { code: 'ASM', pageNum: 1, pageSize: 10 }
      const result = await queryProcessList(params)

      expect(request).toHaveBeenCalledWith({
        url: '/process/list',
        method: 'post',
        data: params
      })
      expect(result).toEqual(mockPage)
    })
  })

  describe('addProcess', () => {
    it('应调用 POST /process/add 并传递新增数据', async () => {
      // Mock返回新增工序的ID
      vi.mocked(request).mockResolvedValue({ id: 1 })

      const data: ProcessAddRequest = {
        code: 'NEW-001',
        name: '新工序',
        processType: 'ASSEMBLY',
        enable: 1
      }
      const result = await addProcess(data)

      expect(request).toHaveBeenCalledWith({
        url: '/process/add',
        method: 'post',
        data
      })
      expect(result).toEqual({ id: 1 })
    })

    it('新增请求应包含必填字段', async () => {
      vi.mocked(request).mockResolvedValue({ id: 2 })

      const data: ProcessAddRequest = {
        code: 'NEW-002',
        name: '测试工序',
        processType: 'INSPECTION'
      }
      await addProcess(data)

      // 验证请求包含code, name, processType
      expect(request).toHaveBeenCalledWith({
        url: '/process/add',
        method: 'post',
        data: {
          code: 'NEW-002',
          name: '测试工序',
          processType: 'INSPECTION'
        }
      })
    })
  })
})