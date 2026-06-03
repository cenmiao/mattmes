import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import { defineComponent } from 'vue'

const EmptyComponent = defineComponent({
  template: '<div></div>'
})

describe('Nested Routes with Layout', () => {
  let router: ReturnType<typeof createRouter>

  const MockLayout = defineComponent({
    name: 'MockLayout',
    template: `
      <div class="layout-container" data-testid="layout">
        <aside data-testid="sidebar">Sidebar</aside>
        <div class="layout-main">
          <header data-testid="header">Header</header>
          <main data-testid="content">
            <router-view />
          </main>
        </div>
      </div>
    `
  })

  const MockDashboard = defineComponent({
    name: 'MockDashboard',
    template: '<div class="dashboard-page" data-testid="dashboard">Dashboard Content</div>'
  })

  const routes: RouteRecordRaw[] = [
    {
      path: '/',
      component: MockLayout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: MockDashboard
        }
      ]
    },
    {
      path: '/login',
      name: 'Login',
      component: EmptyComponent,
      meta: { public: true }
    }
  ]

  beforeEach(() => {
    localStorage.clear()
    router = createRouter({
      history: createWebHashHistory(),
      routes
    })
  })

  it('访问 /dashboard → 渲染在 Layout 内', async () => {
    localStorage.setItem('token', 'valid-token')
    await router.push('/dashboard')
    await router.isReady()

    const App = defineComponent({
      template: '<router-view />'
    })
    const wrapper = mount(App, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.find('[data-testid="layout"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="sidebar"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="header"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="content"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="dashboard"]').exists()).toBe(true)
  })

  it('访问 / → 重定向到 /dashboard', async () => {
    localStorage.setItem('token', 'valid-token')
    await router.push('/')
    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('Layout 包含 sidebar、header、content 三区域', async () => {
    localStorage.setItem('token', 'valid-token')
    await router.push('/dashboard')
    await router.isReady()

    const App = defineComponent({
      template: '<router-view />'
    })
    const wrapper = mount(App, {
      global: {
        plugins: [router]
      }
    })

    const layout = wrapper.find('[data-testid="layout"]')
    expect(layout.find('[data-testid="sidebar"]').exists()).toBe(true)
    expect(layout.find('[data-testid="header"]').exists()).toBe(true)
    expect(layout.find('[data-testid="content"]').exists()).toBe(true)
  })
})