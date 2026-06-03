import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHashHistory } from 'vue-router'
import { defineComponent } from 'vue'
import Login from '@/views/Login.vue'
import ElementPlus from 'element-plus'
import * as authApi from '@/api/auth'

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key]
    }),
    clear: vi.fn(() => {
      store = {}
    })
  }
})()

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

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

describe('Login.vue', () => {
  let router: ReturnType<typeof createRouter>

  beforeEach(() => {
    vi.clearAllMocks()
    localStorageMock.clear()

    const EmptyComponent = defineComponent({
      template: '<div></div>'
    })

    router = createRouter({
      history: createWebHashHistory(),
      routes: [
        { path: '/login', name: 'Login', component: EmptyComponent },
        { path: '/dashboard', name: 'Dashboard', component: EmptyComponent },
        { path: '/change-password', name: 'ChangePassword', component: EmptyComponent }
      ]
    })
  })

  const mountLogin = async () => {
    await router.push('/login')
    await router.isReady()

    const wrapper = mount(Login, {
      global: {
        plugins: [router, ElementPlus]
      }
    })

    // 等待组件渲染
    await wrapper.vm.$nextTick()
    return wrapper
  }

  describe('表单渲染', () => {
    it('应渲染工号输入框', async () => {
      const wrapper = await mountLogin()
      const inputs = wrapper.findAll('input')
      expect(inputs.length).toBeGreaterThanOrEqual(2)
    })

    it('应渲染密码输入框', async () => {
      const wrapper = await mountLogin()
      const passwordInput = wrapper.find('input[type="password"]')
      expect(passwordInput.exists()).toBe(true)
    })

    it('应渲染登录按钮', async () => {
      const wrapper = await mountLogin()
      const button = wrapper.find('button')
      expect(button.exists()).toBe(true)
      expect(button.text()).toContain('登录')
    })
  })

  describe('登录成功流程', () => {
    it('登录成功后应存储 token 和 userInfo', async () => {
      const mockResponse = {
        token: 'test-token',
        userId: 1,
        userNo: 'EMP001',
        name: '测试用户',
        needChangePassword: false,
        permissions: ['user:read'],
        roles: []
      }

      vi.spyOn(authApi, 'login').mockResolvedValue(mockResponse)

      const wrapper = await mountLogin()

      // 填写表单
      const inputs = wrapper.findAll('input')
      await inputs[0].setValue('EMP001')
      await inputs[1].setValue('Password123')

      // 等待表单更新
      await wrapper.vm.$nextTick()

      // 点击登录
      const button = wrapper.find('button')
      await button.trigger('click')

      // 等待异步操作完成
      await new Promise(resolve => setTimeout(resolve, 50))
      await wrapper.vm.$nextTick()

      // 验证 localStorage
      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'test-token')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('userInfo', JSON.stringify({
        id: 1,
        userNo: 'EMP001',
        name: '测试用户',
        permissions: ['user:read'],
        roles: []
      }))
    })

    it('needChangePassword=true 时应跳转到修改密码页', async () => {
      const mockResponse = {
        token: 'test-token',
        userId: 1,
        userNo: 'EMP001',
        name: '测试用户',
        needChangePassword: true,
        permissions: [],
        roles: []
      }

      vi.spyOn(authApi, 'login').mockResolvedValue(mockResponse)

      const wrapper = await mountLogin()

      // 填写表单
      const inputs = wrapper.findAll('input')
      await inputs[0].setValue('EMP001')
      await inputs[1].setValue('Password123')

      // 等待表单更新
      await wrapper.vm.$nextTick()

      // 点击登录
      const button = wrapper.find('button')
      await button.trigger('click')

      // 等待导航
      await new Promise(resolve => setTimeout(resolve, 100))

      expect(router.currentRoute.value.path).toBe('/change-password')
    })
  })

  describe('错误处理', () => {
    it('登录失败时应显示错误消息', async () => {
      const error = new Error('用户名或密码错误')
      vi.spyOn(authApi, 'login').mockRejectedValue(error)

      const { ElMessage } = await import('element-plus')
      const wrapper = await mountLogin()

      // 填写表单
      const inputs = wrapper.findAll('input')
      await inputs[0].setValue('EMP001')
      await inputs[1].setValue('WrongPassword')
      await wrapper.vm.$nextTick()

      // 点击登录
      const button = wrapper.find('button')
      await button.trigger('click')

      // 等待异步操作
      await new Promise(resolve => setTimeout(resolve, 50))
      await wrapper.vm.$nextTick()

      // 验证错误消息显示
      expect(ElMessage.error).toHaveBeenCalled()
    })

    it('密码错误时应显示剩余尝试次数', async () => {
      // 模拟后端返回的密码错误响应
      const errorResponse = {
        message: '密码错误，剩余3次机会',
        remainingAttempts: 3
      }
      vi.spyOn(authApi, 'login').mockRejectedValue(errorResponse)

      const { ElMessage } = await import('element-plus')
      const wrapper = await mountLogin()

      const inputs = wrapper.findAll('input')
      await inputs[0].setValue('EMP001')
      await inputs[1].setValue('WrongPassword')
      await wrapper.vm.$nextTick()

      const button = wrapper.find('button')
      await button.trigger('click')

      await new Promise(resolve => setTimeout(resolve, 50))
      await wrapper.vm.$nextTick()

      expect(ElMessage.error).toHaveBeenCalledWith(
        expect.stringContaining('剩余')
      )
    })

    it('账号锁定时应显示剩余锁定时间', async () => {
      const errorResponse = {
        message: '账号已锁定',
        lockTimeRemaining: 300000 // 5分钟
      }
      vi.spyOn(authApi, 'login').mockRejectedValue(errorResponse)

      const { ElMessage } = await import('element-plus')
      const wrapper = await mountLogin()

      const inputs = wrapper.findAll('input')
      await inputs[0].setValue('EMP001')
      await inputs[1].setValue('WrongPassword')
      await wrapper.vm.$nextTick()

      const button = wrapper.find('button')
      await button.trigger('click')

      await new Promise(resolve => setTimeout(resolve, 50))
      await wrapper.vm.$nextTick()

      expect(ElMessage.error).toHaveBeenCalled()
    })
  })
})
