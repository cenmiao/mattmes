import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  queryProcessList,
  addProcess,
  editProcess,
  type ProcessQueryRequest,
  type ProcessAddRequest,
  type ProcessEditRequest,
  type ProcessPageResult
} from '@/api/process'

// Mock整个request模块
vi.mock('@/utils/request', () => ({
  request: vi.fn()
}))

// 导入mock后的request
import { request } from '@/utils/request'

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

  describe('editProcess', () => {
    it('应调用 PUT /process/edit 并传递编辑数据', async () => {
      // Mock返回更新工序的ID
      vi.mocked(request).mockResolvedValue({ id: 1 })

      const data: ProcessEditRequest = {
        id: 1,
        name: '更新后的工序',
        processType: 'INSPECTION',
        enable: 0,
        description: '更新后的描述',
        remark: '更新后的备注'
      }
      const result = await editProcess(data)

      expect(request).toHaveBeenCalledWith({
        url: '/process/edit',
        method: 'put',
        data
      })
      expect(result).toEqual({ id: 1 })
    })

    it('编辑请求应包含工序ID', async () => {
      vi.mocked(request).mockResolvedValue({ id: 100 })

      const data: ProcessEditRequest = {
        id: 100,
        name: '测试工序',
        processType: 'ASSEMBLY'
      }
      await editProcess(data)

      // 验证请求包含id
      expect(request).toHaveBeenCalledWith({
        url: '/process/edit',
        method: 'put',
        data: {
          id: 100,
          name: '测试工序',
          processType: 'ASSEMBLY'
        }
      })
    })
  })
})