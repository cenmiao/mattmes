import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHashHistory } from 'vue-router'
import PermissionList from '@/views/permission/PermissionList.vue'
import * as permissionApi from '@/api/permission'

// Mock API
vi.mock('@/api/permission', () => ({
  getPermissionTree: vi.fn(),
  createPermission: vi.fn(),
  updatePermission: vi.fn(),
  deletePermission: vi.fn()
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  },
  ElMessageBox: {
    confirm: vi.fn()
  }
}))

// Mock localStorage
const mockUserInfo = {
  roles: [{ roleCode: 'SUPER_ADMIN' }],
  permissions: ['permission:add', 'permission:edit', 'permission:delete']
}

describe('PermissionList', () => {
  let router: any

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.setItem('userInfo', JSON.stringify(mockUserInfo))

    router = createRouter({
      history: createWebHashHistory(),
      routes: [{ path: '/', component: { template: '<div />' } }]
    })
  })

  // 创建一个简单的组件包装器，只测试逻辑
  const createComponent = () => {
    const wrapper = mount(
      {
        template: '<div></div>',
        ...PermissionList
      },
      {
        global: {
          plugins: [router],
          stubs: {
            'el-card': true,
            'el-button': true,
            'el-table': true,
            'el-table-column': true,
            'el-dialog': true,
            'el-form': true,
            'el-form-item': true,
            'el-input': true,
            'el-select': true,
            'el-option': true,
            'el-tag': true,
            'el-icon': true
          },
          directives: {
            permission: () => {},
            loading: () => {}
          }
        }
      }
    )
    return wrapper
  }

  describe('数据加载', () => {
    it('页面挂载时应调用 getPermissionTree 获取数据', async () => {
      const mockTree = [
        {
          id: 1,
          permissionName: '用户管理',
          permissionCode: 'user',
          children: []
        }
      ]
      vi.mocked(permissionApi.getPermissionTree).mockResolvedValue(mockTree)

      createComponent()
      await router.isReady()
      await vi.waitFor(() => {
        expect(permissionApi.getPermissionTree).toHaveBeenCalledTimes(1)
      })
    })
  })

  describe('新增权限', () => {
    it('handleAdd 应重置表单并显示弹窗', async () => {
      vi.mocked(permissionApi.getPermissionTree).mockResolvedValue([])

      const wrapper = createComponent()
      await router.isReady()

      // 调用新增方法
      ;(wrapper.vm as any).handleAdd()

      expect((wrapper.vm as any).dialogVisible).toBe(true)
      expect((wrapper.vm as any).isEdit).toBe(false)
      expect((wrapper.vm as any).permissionForm.permissionName).toBe('')
      expect((wrapper.vm as any).permissionForm.permissionCode).toBe('')
    })
  })

  describe('编辑权限', () => {
    it('handleEdit 应填充表单并显示弹窗', async () => {
      vi.mocked(permissionApi.getPermissionTree).mockResolvedValue([])

      const wrapper = createComponent()
      await router.isReady()

      const mockRow = {
        id: 1,
        permissionName: '用户管理',
        permissionCode: 'user',
        permissionType: 1,
        parentId: null,
        description: '测试描述',
        createTime: '2026-01-01'
      }

      ;(wrapper.vm as any).handleEdit(mockRow)

      expect((wrapper.vm as any).dialogVisible).toBe(true)
      expect((wrapper.vm as any).isEdit).toBe(true)
      expect((wrapper.vm as any).permissionForm.permissionName).toBe('用户管理')
      expect((wrapper.vm as any).permissionForm.permissionCode).toBe('user')
      expect((wrapper.vm as any).editingPermissionId).toBe(1)
    })
  })

  describe('新增子权限', () => {
    it('handleAddChild 应自动填充父权限ID', async () => {
      vi.mocked(permissionApi.getPermissionTree).mockResolvedValue([])

      const wrapper = createComponent()
      await router.isReady()

      const mockRow = {
        id: 1,
        permissionName: '用户管理',
        permissionCode: 'user',
        permissionType: 1
      }

      ;(wrapper.vm as any).handleAddChild(mockRow)

      expect((wrapper.vm as any).dialogVisible).toBe(true)
      expect((wrapper.vm as any).isEdit).toBe(false)
      expect((wrapper.vm as any).permissionForm.parentId).toBe(1)
    })
  })
})