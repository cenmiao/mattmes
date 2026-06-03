import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHashHistory } from 'vue-router'
import { defineComponent } from 'vue'
import ChangePassword from '@/views/ChangePassword.vue'
import ElementPlus from 'element-plus'
import * as authApi from '@/api/auth'

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => { store[key] = value }),
    removeItem: vi.fn((key: string) => { delete store[key] }),
    clear: vi.fn(() => { store = {} })
  }
})()

Object.defineProperty(window, 'localStorage', { value: localStorageMock })

// Mock Element Plus message
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn()
    }
  }
})

describe('ChangePassword.vue', () => {
  let router: ReturnType<typeof createRouter>

  beforeEach(() => {
    vi.clearAllMocks()
    localStorageMock.clear()
    localStorageMock.setItem('token', 'test-token')

    const EmptyComponent = defineComponent({ template: '<div></div>' })

    router = createRouter({
      history: createWebHashHistory(),
      routes: [
        { path: '/login', name: 'Login', component: EmptyComponent },
        { path: '/change-password', name: 'ChangePassword', component: EmptyComponent },
        { path: '/dashboard', name: 'Dashboard', component: EmptyComponent }
      ]
    })
  })

  const mountComponent = async (scenario: 'firstLogin' | 'expired' | 'normal' = 'normal') => {
    if (scenario === 'firstLogin') {
      localStorageMock.setItem('needChangePassword', 'true')
    }

    const route = scenario === 'expired'
      ? '/change-password?expired=true'
      : '/change-password'

    await router.push(route)
    await router.isReady()

    const wrapper = mount(ChangePassword, {
      global: {
        plugins: [router, ElementPlus]
      }
    })

    await flushPromises()
    return wrapper
  }

  describe('页面渲染', () => {
    it('应渲染修改密码标题', async () => {
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('修改密码')
    })

    it('应渲染新密码输入框', async () => {
      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      expect(inputs.length).toBeGreaterThanOrEqual(2)
    })

    it('应渲染确认密码输入框', async () => {
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('确认密码')
    })

    it('应渲染提交按钮', async () => {
      const wrapper = await mountComponent()
      const button = wrapper.find('button')
      expect(button.exists()).toBe(true)
    })
  })

  describe('场景切换', () => {
    it('首次登录时应显示"首次登录，请设置您的密码"', async () => {
      const wrapper = await mountComponent('firstLogin')
      expect(wrapper.text()).toContain('首次登录')
    })

    it('首次登录时应隐藏旧密码输入框', async () => {
      const wrapper = await mountComponent('firstLogin')
      expect(wrapper.text()).not.toContain('旧密码')
    })

    it('普通修改时应显示旧密码输入框', async () => {
      const wrapper = await mountComponent('normal')
      expect(wrapper.text()).toContain('旧密码')
    })

    it('普通修改时应显示"修改您的密码"', async () => {
      const wrapper = await mountComponent('normal')
      expect(wrapper.text()).toContain('修改您的密码')
    })

    it('密码过期时应显示"密码已超过30天未修改"', async () => {
      const wrapper = await mountComponent('expired')
      expect(wrapper.text()).toContain('30天')
    })
  })

  describe('密码强度指示器', () => {
    it('输入弱密码时应显示"弱"', async () => {
      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      // 新密码输入框（第二个，第一个是旧密码）
      const newPasswordInput = inputs[1]
      await newPasswordInput.setValue('abc')
      await flushPromises()

      expect(wrapper.text()).toContain('弱')
    })

    it('输入中等密码时应显示"中等"', async () => {
      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      const newPasswordInput = inputs[1]
      await newPasswordInput.setValue('abcdefgh')
      await flushPromises()

      expect(wrapper.text()).toContain('中等')
    })

    it('输入强密码时应显示"强"', async () => {
      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      const newPasswordInput = inputs[1]
      await newPasswordInput.setValue('Abcdefg1')
      await flushPromises()

      expect(wrapper.text()).toContain('强')
    })
  })

  describe('密码要求检查列表', () => {
    it('应显示三项密码要求', async () => {
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('至少8位字符')
      expect(wrapper.text()).toContain('包含字母')
      expect(wrapper.text()).toContain('包含数字')
    })

    it('满足8位要求时应显示勾选', async () => {
      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      await inputs[1].setValue('abcdefgh')
      await flushPromises()

      const items = wrapper.findAll('.requirement-item')
      expect(items[0].classes()).toContain('met')
    })

    it('满足字母要求时应显示勾选', async () => {
      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      await inputs[1].setValue('abc')
      await flushPromises()

      const items = wrapper.findAll('.requirement-item')
      expect(items[1].classes()).toContain('met')
    })

    it('满足数字要求时应显示勾选', async () => {
      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      await inputs[1].setValue('123')
      await flushPromises()

      const items = wrapper.findAll('.requirement-item')
      expect(items[2].classes()).toContain('met')
    })
  })

  describe('提交按钮启用/禁用', () => {
    it('未填写时提交按钮应禁用', async () => {
      const wrapper = await mountComponent()
      const button = wrapper.find('button')
      expect(button.attributes('disabled')).toBeDefined()
    })

    it('所有条件满足时提交按钮应启用', async () => {
      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      // 旧密码
      await inputs[0].setValue('OldPass123')
      // 新密码
      await inputs[1].setValue('NewPass456')
      // 确认密码
      await inputs[2].setValue('NewPass456')
      await flushPromises()

      const button = wrapper.find('button')
      expect(button.attributes('disabled')).toBeUndefined()
    })
  })

  describe('提交成功流程', () => {
    it('提交成功后应清空 token 并跳转登录页', async () => {
      vi.spyOn(authApi, 'changePassword').mockResolvedValue(undefined)

      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      await inputs[0].setValue('OldPass123')
      await inputs[1].setValue('NewPass456')
      await inputs[2].setValue('NewPass456')
      await flushPromises()

      const button = wrapper.find('button')
      await button.trigger('click')
      await flushPromises()

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token')
      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('提交成功后应显示成功提示', async () => {
      vi.spyOn(authApi, 'changePassword').mockResolvedValue(undefined)

      const { ElMessage } = await import('element-plus')
      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      await inputs[0].setValue('OldPass123')
      await inputs[1].setValue('NewPass456')
      await inputs[2].setValue('NewPass456')
      await flushPromises()

      const button = wrapper.find('button')
      await button.trigger('click')
      await flushPromises()

      expect(ElMessage.success).toHaveBeenCalledWith('密码修改成功，请重新登录')
    })
  })

  describe('错误处理', () => {
    it('提交失败时应显示错误信息', async () => {
      const error = Object.assign(new Error('旧密码错误'), { code: 400 })
      vi.spyOn(authApi, 'changePassword').mockRejectedValue(error)

      const { ElMessage } = await import('element-plus')
      const wrapper = await mountComponent()
      const inputs = wrapper.findAll('input')
      await inputs[0].setValue('WrongOld123')
      await inputs[1].setValue('NewPass456')
      await inputs[2].setValue('NewPass456')
      await flushPromises()

      const button = wrapper.find('button')
      await button.trigger('click')
      await flushPromises()

      expect(ElMessage.error).toHaveBeenCalled()
    })
  })
})
