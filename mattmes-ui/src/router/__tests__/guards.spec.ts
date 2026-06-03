import { describe, it, expect, beforeEach } from 'vitest'
import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import { defineComponent } from 'vue'

const EmptyComponent = defineComponent({
  template: '<div></div>'
})

// 现有路由守卫逻辑
function setupRouterGuard(router: ReturnType<typeof createRouter>) {
  router.beforeEach((to, _from, next) => {
    const token = localStorage.getItem('token')
    const userInfo = localStorage.getItem('userInfo')
    const needChangePassword = localStorage.getItem('needChangePassword')

    if (to.path === '/login') {
      if (token) {
        next('/dashboard')
      } else {
        next()
      }
      return
    }

    if (to.meta.public) {
      next()
      return
    }

    if (!token) {
      next('/login')
      return
    }

    if (needChangePassword === 'true' && to.path !== '/change-password') {
      next('/change-password')
      return
    }

    if (to.meta.permission) {
      const permissions = userInfo ? JSON.parse(userInfo).permissions || [] : []
      if (!permissions.includes(to.meta.permission as string)) {
        next('/403')
        return
      }
    }

    next()
  })
}

describe('Router Guards', () => {
  let router: ReturnType<typeof createRouter>

  const baseRoutes: RouteRecordRaw[] = [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', name: 'Login', component: EmptyComponent, meta: { public: true } },
    { path: '/dashboard', name: 'Dashboard', component: EmptyComponent },
    { path: '/change-password', name: 'ChangePassword', component: EmptyComponent },
    { path: '/403', name: 'Forbidden', component: EmptyComponent, meta: { public: true } },
    { path: '/admin', name: 'Admin', component: EmptyComponent, meta: { permission: 'admin:read' } }
  ]

  beforeEach(() => {
    localStorage.clear()
    router = createRouter({
      history: createWebHashHistory(),
      routes: baseRoutes
    })
    setupRouterGuard(router)
  })

  describe('认证检查', () => {
    it('未认证用户访问受保护路由 → 重定向到 /login', async () => {
      await router.push('/dashboard')
      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('已认证用户访问 /login → 重定向到 /dashboard', async () => {
      localStorage.setItem('token', 'valid-token')
      await router.push('/login')
      expect(router.currentRoute.value.path).toBe('/dashboard')
    })
  })

  describe('公共路由', () => {
    it('未认证用户可访问公共路由', async () => {
      await router.push('/login')
      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('未认证用户可访问 /403', async () => {
      await router.push('/403')
      expect(router.currentRoute.value.path).toBe('/403')
    })
  })

  describe('强制改密检查', () => {
    it('needChangePassword=true → 强制跳转 /change-password', async () => {
      localStorage.setItem('token', 'valid-token')
      localStorage.setItem('needChangePassword', 'true')
      await router.push('/dashboard')
      expect(router.currentRoute.value.path).toBe('/change-password')
    })

    it('needChangePassword=true 时可直接访问 /change-password', async () => {
      localStorage.setItem('token', 'valid-token')
      localStorage.setItem('needChangePassword', 'true')
      await router.push('/change-password')
      expect(router.currentRoute.value.path).toBe('/change-password')
    })
  })

  describe('权限检查', () => {
    it('无权限访问需权限路由 → 重定向到 /403', async () => {
      localStorage.setItem('token', 'valid-token')
      localStorage.setItem('userInfo', JSON.stringify({ permissions: ['user:read'] }))
      await router.push('/admin')
      expect(router.currentRoute.value.path).toBe('/403')
    })

    it('有权限可访问需权限路由', async () => {
      localStorage.setItem('token', 'valid-token')
      localStorage.setItem('userInfo', JSON.stringify({ permissions: ['admin:read'] }))
      await router.push('/admin')
      expect(router.currentRoute.value.path).toBe('/admin')
    })
  })
})