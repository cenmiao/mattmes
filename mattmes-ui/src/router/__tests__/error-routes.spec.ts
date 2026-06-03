import { describe, it, expect, beforeEach } from 'vitest'
import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import { defineComponent } from 'vue'

const EmptyComponent = defineComponent({
  template: '<div></div>'
})

describe('Error Routes', () => {
  let router: ReturnType<typeof createRouter>

  const routes: RouteRecordRaw[] = [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', name: 'Login', component: EmptyComponent, meta: { public: true } },
    { path: '/dashboard', name: 'Dashboard', component: EmptyComponent },
    { path: '/403', name: 'Forbidden', component: EmptyComponent, meta: { public: true } },
    { path: '/404', name: 'NotFound', component: EmptyComponent, meta: { public: true } },
    { path: '/:pathMatch(.*)*', redirect: '/404' }
  ]

  beforeEach(() => {
    localStorage.clear()
    router = createRouter({
      history: createWebHashHistory(),
      routes
    })
  })

  describe('/403 路由', () => {
    it('访问 /403 → 匹配 Forbidden 路由', async () => {
      await router.push('/403')
      expect(router.currentRoute.value.name).toBe('Forbidden')
      expect(router.currentRoute.value.meta.public).toBe(true)
    })
  })

  describe('/404 路由', () => {
    it('访问 /404 → 匹配 NotFound 路由', async () => {
      await router.push('/404')
      expect(router.currentRoute.value.name).toBe('NotFound')
      expect(router.currentRoute.value.meta.public).toBe(true)
    })

    it('访问未知路由 → 重定向到 /404', async () => {
      await router.push('/unknown-route-xyz')
      expect(router.currentRoute.value.path).toBe('/404')
    })
  })
})