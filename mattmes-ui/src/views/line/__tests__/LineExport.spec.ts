import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, type VueWrapper } from '@vue/test-utils'
import { createRouter, createWebHashHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import { nextTick } from 'vue'
import LineList from '@/views/line/LineList.vue'
import { permission } from '@/directives/permission'

// Mock API
vi.mock('@/api/line', () => ({
  queryLineList: vi.fn().mockResolvedValue({
    list: [
      {
        id: 7,
        code: 'LINE-007',
        name: '七号线体',
        description: '原始描述',
        enable: 1,
        createdBy: 'admin',
        createTime: '2026-01-01 10:00:00'
      }
    ],
    total: 1,
    pageNum: 1,
    pageSize: 10
  }),
  addLine: vi.fn().mockResolvedValue(1),
  editLine: vi.fn().mockResolvedValue(7),
  updateLineStatus: vi.fn().mockResolvedValue(7),
  exportLine: vi.fn()
}))

import { exportLine, queryLineList } from '@/api/line'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [{ path: '/', component: { template: '<div />' } }]
})

let wrapper: VueWrapper | undefined

const mountPage = (useRealPermission = false) => {
  wrapper = mount(LineList, {
    attachTo: document.body,
    global: {
      plugins: [router, ElementPlus],
      directives: useRealPermission
        ? { permission }
        : { permission: { mounted: () => {}, beforeMount: () => {} } }
    }
  })
  return wrapper
}

const flush = async () => {
  await new Promise(r => setTimeout(r, 0))
  await nextTick()
  await nextTick()
}

const findExportButton = () =>
  wrapper!.findAll('button').find(btn => btn.text().includes('导出'))

describe('LineList.vue 导出 CSV', () => {
  let createObjectURL: ReturnType<typeof vi.fn>
  let revokeObjectURL: ReturnType<typeof vi.fn>
  let clickedDownloads: string[]

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()

    createObjectURL = vi.fn(() => 'blob:mock-url')
    revokeObjectURL = vi.fn()
    ;(window.URL as any).createObjectURL = createObjectURL
    ;(window.URL as any).revokeObjectURL = revokeObjectURL

    clickedDownloads = []
    // 拦截 <a>.click()，避免 happy-dom 真的发起导航
    const originalClick = HTMLAnchorElement.prototype.click
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (this: HTMLAnchorElement) {
      clickedDownloads.push(this.download)
    })
    void originalClick
  })

  afterEach(() => {
    wrapper?.unmount()
    wrapper = undefined
    document.body.innerHTML = ''
    vi.restoreAllMocks()
    localStorage.clear()
  })

  it('渲染导出按钮', async () => {
    mountPage()
    await flush()

    expect(findExportButton()).toBeDefined()
  })

  it('导出按钮挂载 line:export 权限指令', async () => {
    // 真实权限指令：拥有 line:export 的用户可见
    localStorage.setItem(
      'userInfo',
      JSON.stringify({ permissions: ['line:read', 'line:export'], roles: [] })
    )
    mountPage(true)
    await flush()

    expect(findExportButton() && findExportButton()!.exists()).toBeTruthy()
  })

  it('无 line:export 权限时不渲染导出按钮', async () => {
    localStorage.setItem(
      'userInfo',
      JSON.stringify({ permissions: ['line:read'], roles: [] })
    )
    mountPage(true)
    await flush()

    expect(findExportButton()).toBeUndefined()
  })

  it('点击导出按当前筛选条件调用 exportLine 并触发下载，文件名取自响应头', async () => {
    vi.mocked(exportLine).mockResolvedValue({
      data: new Blob(['\uFEFFID'], { type: 'text/csv;charset=UTF-8' }),
      headers: {
        'content-disposition':
          'attachment;filename=%E7%BA%BF%E4%BD%93%E6%95%B0%E6%8D%AE_20260102_030405.csv'
      }
    })

    mountPage()
    await flush()

    await findExportButton()!.trigger('click')
    await flush()
    await new Promise(r => setTimeout(r, 0))

    // 使用当前查询条件（含分页无关的筛选关键字）
    expect(exportLine).toHaveBeenCalledWith(
      expect.objectContaining({ code: '', name: '', enable: undefined })
    )
    expect(createObjectURL).toHaveBeenCalledTimes(1)

    expect(clickedDownloads.length).toBe(1)
    // 文件名完全取自服务端 Content-Disposition（URL 解码后），不使用本地时钟
    expect(clickedDownloads[0]).toBe('线体数据_20260102_030405.csv')
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:mock-url')
  })

  it('响应头文件名含空格（%20）时正确解码', async () => {
    vi.mocked(exportLine).mockResolvedValue({
      data: new Blob(['\uFEFFID']),
      headers: {
        'content-disposition': 'attachment;filename=%E7%BA%BF%E4%BD%93%20%E6%95%B0%E6%8D%AE_20260102_030405.csv'
      }
    })

    mountPage()
    await flush()
    await findExportButton()!.trigger('click')
    await flush()
    await new Promise(r => setTimeout(r, 0))

    expect(clickedDownloads[0]).toBe('线体 数据_20260102_030405.csv')
  })

  it('响应头缺少 Content-Disposition 时回退为约定格式文件名', async () => {
    vi.mocked(exportLine).mockResolvedValue({
      data: new Blob(['\uFEFFID']),
      headers: {}
    })

    mountPage()
    await flush()
    await findExportButton()!.trigger('click')
    await flush()
    await new Promise(r => setTimeout(r, 0))

    expect(clickedDownloads.length).toBe(1)
    expect(clickedDownloads[0]).toMatch(/^线体数据_\d{8}_\d{6}\.csv$/)
  })

  it('导出失败时显示错误提示且不触发下载', async () => {
    vi.mocked(exportLine).mockRejectedValue(new Error('导出失败'))

    mountPage()
    await flush()

    await findExportButton()!.trigger('click')
    await new Promise(r => setTimeout(r, 50))
    await nextTick()

    expect(clickedDownloads.length).toBe(0)
    const message = document.body.querySelector('.el-message') as HTMLElement | null
    expect(message).not.toBeNull()
    expect(message!.textContent).toContain('导出失败')
  })

  it('导出不影响列表查询次数', async () => {
    vi.mocked(exportLine).mockResolvedValue({ data: new Blob(['\uFEFFID']), headers: {} })

    mountPage()
    await flush()
    const callsBefore = vi.mocked(queryLineList).mock.calls.length

    await findExportButton()!.trigger('click')
    await flush()

    expect(vi.mocked(queryLineList).mock.calls.length).toBe(callsBefore)
  })
})
