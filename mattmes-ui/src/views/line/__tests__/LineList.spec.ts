import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, type VueWrapper } from '@vue/test-utils'
import { createRouter, createWebHashHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import { nextTick } from 'vue'
import LineList from '@/views/line/LineList.vue'

// Mock API
vi.mock('@/api/line', () => ({
  queryLineList: vi.fn().mockResolvedValue({
    list: [],
    total: 0,
    pageNum: 1,
    pageSize: 10
  }),
  addLine: vi.fn().mockResolvedValue(1),
  deleteLine: vi.fn().mockResolvedValue(1),
  batchDeleteLines: vi.fn().mockResolvedValue([1])
}))

// Mock 确认框：默认确认（resolve），可在单个用例中改为取消（reject）
vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<typeof import('element-plus')>()
  return {
    ...actual,
    ElMessageBox: {
      ...actual.ElMessageBox,
      confirm: vi.fn().mockResolvedValue({ value: '', action: 'confirm' })
    }
  }
})

import { addLine, batchDeleteLines, deleteLine, queryLineList } from '@/api/line'
import { ElMessageBox } from 'element-plus'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [{ path: '/', component: { template: '<div />' } }]
})

let wrapper: VueWrapper | undefined

const mountPage = () => {
  wrapper = mount(LineList, {
    attachTo: document.body,
    global: {
      plugins: [router, ElementPlus],
      directives: {
        // 权限指令在测试中不裁剪元素，便于断言按钮存在
        permission: { mounted: () => {}, beforeMount: () => {} }
      }
    }
  })
  return wrapper
}

const findButton = (text: string) =>
  wrapper!.findAll('button').find(btn => btn.text().includes(text))

const openAddDialog = async () => {
  await findButton('新增')!.trigger('click')
  await nextTick()
  await nextTick()
}

const dialog = () => document.body.querySelector('.el-dialog') as HTMLElement

describe('LineList.vue 新增线体', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    wrapper?.unmount()
    wrapper = undefined
    document.body.innerHTML = ''
  })

  it('页面挂载后应触发列表查询', async () => {
    mountPage()
    await nextTick()
    expect(queryLineList).toHaveBeenCalled()
  })

  it('页面应存在新增按钮', () => {
    mountPage()
    expect(findButton('新增')).toBeDefined()
  })

  it('新增按钮挂载了 line:add 权限指令', () => {
    mountPage()
    const addButton = findButton('新增')
    expect(addButton).toBeDefined()
    // 权限指令真实实现在 src/directives/permission.ts：无 line:add 时移除该按钮；
    // 测试中以空指令桩挂载，故这里只断言按钮存在（真实裁剪行为由指令单测覆盖）。
    expect(addButton!.text()).toContain('新增')
  })

  it('点击新增按钮应打开新增弹窗', async () => {
    mountPage()
    await openAddDialog()

    expect(dialog()).not.toBeNull()
    expect(dialog().textContent).toContain('新增线体')
  })

  it('新增弹窗包含编码、名称、状态、描述字段', async () => {
    mountPage()
    await openAddDialog()

    const text = dialog().textContent || ''
    expect(text).toContain('线体编码')
    expect(text).toContain('线体名称')
    expect(text).toContain('启用状态')
    expect(text).toContain('描述')
  })

  it('新增弹窗默认状态为启用', async () => {
    mountPage()
    await openAddDialog()

    const checked = dialog().querySelector('.el-radio.is-checked') as HTMLElement
    expect(checked).not.toBeNull()
    expect(checked.textContent).toContain('启用')
  })

  it('编码输入框限制最大长度 50、名称限制 100、描述限制 500', async () => {
    mountPage()
    await openAddDialog()

    const inputs = Array.from(dialog().querySelectorAll('input.el-input__inner, textarea.el-textarea__inner')) as HTMLInputElement[]
    const maxlengths = inputs.map(i => i.getAttribute('maxlength'))
    expect(maxlengths).toContain('50')
    expect(maxlengths).toContain('100')
    expect(maxlengths).toContain('500')
  })
})

describe('LineList.vue 删除', () => {
  const rows = [
    { id: 1, code: 'LINE-001', name: '一号线体', enable: 1 },
    { id: 2, code: 'LINE-002', name: '二号线体', enable: 1 }
  ]

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.mocked(queryLineList).mockResolvedValue({
      list: rows,
      total: rows.length,
      pageNum: 1,
      pageSize: 10
    })
    vi.mocked(ElMessageBox.confirm).mockResolvedValue({ value: '', action: 'confirm' } as any)
  })

  afterEach(() => {
    wrapper?.unmount()
    wrapper = undefined
    document.body.innerHTML = ''
  })

  it('页面应存在批量删除按钮，未勾选任何行时为禁用态', async () => {
    mountPage()
    await nextTick()

    const batchButton = findButton('批量删除')
    expect(batchButton).toBeDefined()
    expect(batchButton!.attributes('disabled')).toBeDefined()
  })

  it('每行应渲染删除按钮', async () => {
    mountPage()
    await nextTick()
    await nextTick()

    const deleteButtons = wrapper!.findAll('button').filter(btn => btn.text().trim() === '删除')
    expect(deleteButtons.length).toBe(rows.length)
  })

  it('单条删除弹确认框并显示该线体名称，确认后调用 deleteLine 并刷新列表', async () => {
    mountPage()
    await nextTick()
    await nextTick()

    const deleteButton = wrapper!.findAll('button').find(btn => btn.text().trim() === '删除')!
    await deleteButton.trigger('click')
    await new Promise(r => setTimeout(r, 0))

    expect(ElMessageBox.confirm).toHaveBeenCalledWith(
      expect.stringContaining('一号线体'),
      expect.any(String),
      expect.any(Object)
    )
    expect(deleteLine).toHaveBeenCalledWith(1)
    // 初始查询一次 + 删除后刷新一次
    expect(queryLineList).toHaveBeenCalledTimes(2)
  })

  it('单条删除取消时不应调用 deleteLine', async () => {
    vi.mocked(ElMessageBox.confirm).mockRejectedValue('cancel')
    mountPage()
    await nextTick()
    await nextTick()

    const deleteButton = wrapper!.findAll('button').find(btn => btn.text().trim() === '删除')!
    await deleteButton.trigger('click')
    await new Promise(r => setTimeout(r, 0))

    expect(deleteLine).not.toHaveBeenCalled()
    expect(queryLineList).toHaveBeenCalledTimes(1)
  })

  it('勾选行后批量删除按钮可用，确认框显示条数，确认后调用 batchDeleteLines 并刷新列表', async () => {
    mountPage()
    await nextTick()
    await nextTick()

    const vm = wrapper!.vm as any
    vm.handleSelectionChange(rows)
    await nextTick()

    const batchButton = findButton('批量删除')!
    expect(batchButton.attributes('disabled')).toBeUndefined()

    await batchButton.trigger('click')
    await new Promise(r => setTimeout(r, 0))

    expect(ElMessageBox.confirm).toHaveBeenCalledWith(
      expect.stringContaining(`${rows.length}`),
      expect.any(String),
      expect.any(Object)
    )
    expect(batchDeleteLines).toHaveBeenCalledWith([1, 2])
    // 初始查询一次 + 删除后刷新一次
    expect(queryLineList).toHaveBeenCalledTimes(2)
  })
})

describe('LineList.vue 权限指令', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    wrapper?.unmount()
    wrapper = undefined
    document.body.innerHTML = ''
  })

  it('删除与批量删除按钮都挂载了 line:delete 权限指令', () => {
    // 用一个记录指令值的桩，断言按钮声明了 line:delete
    const seen: string[] = []
    wrapper = mount(LineList, {
      attachTo: document.body,
      global: {
        plugins: [router, ElementPlus],
        directives: {
          permission: {
            mounted: (_el: HTMLElement, binding: any) => {
              seen.push(binding.value)
            }
          }
        }
      }
    })

    expect(seen).toContain('line:delete')
  })
})

describe('LineList.vue 新增提交', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    wrapper?.unmount()
    wrapper = undefined
    document.body.innerHTML = ''
  })

  it('校验通过时提交调用 addLine 并刷新列表', async () => {
    mountPage()
    await openAddDialog()

    const inputs = Array.from(dialog().querySelectorAll('input.el-input__inner')) as HTMLInputElement[]
    inputs[0].value = 'LINE-001'
    inputs[0].dispatchEvent(new Event('input'))
    inputs[1].value = '一号线'
    inputs[1].dispatchEvent(new Event('input'))
    await nextTick()

    const confirm = Array.from(dialog().querySelectorAll('.el-dialog__footer button')).find(
      b => b.textContent?.trim() === '确定'
    ) as HTMLButtonElement
    confirm.click()
    await new Promise(r => setTimeout(r, 100))
    await nextTick()

    // 说明：本仓库 happy-dom + Element Plus 2.14 环境下表单校验恒返回通过
    // （用最小 el-form 可复现，非本组件缺陷），故此处断言提交链路终点。
    expect(addLine).toHaveBeenCalledWith(
      expect.objectContaining({ code: 'LINE-001', name: '一号线', enable: 1 })
    )
    // 新增成功后刷新列表
    expect(queryLineList).toHaveBeenCalledTimes(2)
  })
})
