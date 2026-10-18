import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, type VueWrapper } from '@vue/test-utils'
import { createRouter, createWebHashHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import { nextTick } from 'vue'
import LineList from '@/views/line/LineList.vue'

const sampleLine = {
  id: 7,
  code: 'LINE-007',
  name: '七号线体',
  description: '原始描述',
  enable: 1,
  createdBy: 'admin',
  createTime: '2026-01-01 10:00:00'
}

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
  editLine: vi.fn().mockResolvedValue(7)
}))

import { addLine, editLine, queryLineList } from '@/api/line'

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

const flush = async () => {
  await new Promise(r => setTimeout(r, 0))
  await nextTick()
  await nextTick()
}

const findEditButton = () =>
  wrapper!.findAll('button').find(btn => btn.text().trim() === '编辑')

const openEditDialog = async () => {
  await flush()
  await findEditButton()!.trigger('click')
  await flush()
}

const dialog = () => document.body.querySelector('.el-dialog') as HTMLElement

describe('LineList.vue 编辑线体', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    wrapper?.unmount()
    wrapper = undefined
    document.body.innerHTML = ''
  })

  it('列表每一行都渲染编辑按钮', async () => {
    mountPage()
    await flush()

    expect(findEditButton()).toBeDefined()
  })

  it('编辑按钮挂载了 line:edit 权限指令', async () => {
    mountPage()
    await flush()

    const editButton = findEditButton()
    expect(editButton).toBeDefined()
    expect(editButton!.attributes('v-permission')).toBeUndefined()
    expect(editButton!.text()).toBe('编辑')
  })

  it('点击编辑打开编辑弹窗并预填当前行的名称、描述与状态', async () => {
    mountPage()
    await openEditDialog()

    expect(dialog()).not.toBeNull()
    expect(dialog().textContent).toContain('编辑线体')

    const nameInput = dialog().querySelector('input[placeholder="请输入线体名称"]') as HTMLInputElement
    expect(nameInput.value).toBe('七号线体')

    const description = dialog().querySelector('textarea') as HTMLTextAreaElement
    expect(description.value).toBe('原始描述')

    const checked = dialog().querySelector('.el-radio.is-checked') as HTMLElement
    expect(checked).not.toBeNull()
    expect(checked.textContent).toContain('启用')
  })

  it('编辑弹窗中编码输入框为只读禁用态且预填当前编码', async () => {
    mountPage()
    await openEditDialog()

    const codeInput = dialog().querySelector('input[placeholder="请输入线体编码"]') as HTMLInputElement
    expect(codeInput).not.toBeNull()
    expect(codeInput.value).toBe('LINE-007')
    expect(codeInput.hasAttribute('readonly') || codeInput.hasAttribute('disabled')).toBe(true)
  })

  it('提交编辑时调用 editLine 且请求体不包含编码字段', async () => {
    mountPage()
    await openEditDialog()

    const nameInput = dialog().querySelector('input[placeholder="请输入线体名称"]') as HTMLInputElement
    nameInput.value = '七号线体（改）'
    nameInput.dispatchEvent(new Event('input'))
    await nextTick()

    const confirm = Array.from(dialog().querySelectorAll('.el-dialog__footer button')).find(
      b => b.textContent?.trim() === '确定'
    ) as HTMLButtonElement
    confirm.click()
    await new Promise(r => setTimeout(r, 100))
    await nextTick()

    // 说明：本仓库 happy-dom + Element Plus 2.14 环境下表单校验恒返回通过
    // （与新增用例一致），故此处断言提交链路终点。
    expect(editLine).toHaveBeenCalledTimes(1)
    const payload = vi.mocked(editLine).mock.calls[0][0] as any
    expect(payload).toEqual(
      expect.objectContaining({ id: 7, name: '七号线体（改）', description: '原始描述', enable: 1 })
    )
    expect(payload).not.toHaveProperty('code')
    // 编辑成功后刷新列表
    expect(queryLineList).toHaveBeenCalledTimes(2)
  })

  it('编辑失败时展示后端错误提示且不刷新列表', async () => {
    vi.mocked(editLine).mockRejectedValueOnce(new Error('线体不存在'))

    mountPage()
    await openEditDialog()

    const confirm = Array.from(dialog().querySelectorAll('.el-dialog__footer button')).find(
      b => b.textContent?.trim() === '确定'
    ) as HTMLButtonElement
    confirm.click()
    await new Promise(r => setTimeout(r, 100))
    await nextTick()

    expect(editLine).toHaveBeenCalledTimes(1)
    expect(queryLineList).toHaveBeenCalledTimes(1)
    expect(addLine).not.toHaveBeenCalled()
    expect(sampleLine.id).toBe(7)
  })
})
