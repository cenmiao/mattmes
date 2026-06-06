import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ProcessList from '@/views/process/ProcessList.vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'

// Mock API
vi.mock('@/api/process', () => ({
  queryProcessList: vi.fn().mockResolvedValue({
    list: [],
    total: 0,
    pageNum: 1,
    pageSize: 10
  }),
  exportProcess: vi.fn().mockResolvedValue(undefined)
}))

// Mock permission directive
vi.mock('@/directives/permission', () => ({
  vPermission: {
    mounted: () => {},
    beforeMount: () => {}
  }
}))

import { exportProcess } from '@/api/process'

// 创建测试路由
const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', component: { template: '<div />' } },
    { path: '/process', component: ProcessList }
  ]
})

describe('ProcessList.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('页面应存在导出按钮', async () => {
    const wrapper = mount(ProcessList, {
      global: {
        plugins: [router, ElementPlus]
      }
    })

    // 查找导出按钮
    const buttons = wrapper.findAll('button')
    const exportButton = buttons.find(btn => btn.text().includes('导出'))

    expect(exportButton).toBeDefined()
  })

  it('点击导出按钮应调用 exportProcess 方法', async () => {
    const wrapper = mount(ProcessList, {
      global: {
        plugins: [router, ElementPlus]
      }
    })

    // 找到导出按钮并点击
    const buttons = wrapper.findAll('button')
    const exportButton = buttons.find(btn => btn.text().includes('导出'))

    if (exportButton) {
      await exportButton.trigger('click')

      // 验证 exportProcess 被调用
      expect(exportProcess).toHaveBeenCalled()
    }
  })
})
