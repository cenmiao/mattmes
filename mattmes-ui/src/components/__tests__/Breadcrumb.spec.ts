import { describe, it, expect } from 'vitest'

describe('面包屑导航逻辑', () => {
  interface RouteMatched {
    path: string
    meta: {
      title?: string
    }
  }

  const computeBreadcrumbs = (matched: RouteMatched[]) => {
    return matched
      .filter(r => r.meta?.title)
      .map(r => ({
        path: r.path,
        title: r.meta.title as string
      }))
  }

  describe('面包屑生成', () => {
    it('从路由 matched 生成面包屑', () => {
      const mockRouteMatched: RouteMatched[] = [
        { path: '/', meta: { title: '首页' } },
        { path: '/users', meta: { title: '用户管理' } },
        { path: '/users/create', meta: { title: '创建用户' } }
      ]
      const breadcrumbs = computeBreadcrumbs(mockRouteMatched)
      expect(breadcrumbs.length).toBe(3)
      expect(breadcrumbs[0].title).toBe('首页')
      expect(breadcrumbs[1].title).toBe('用户管理')
      expect(breadcrumbs[2].title).toBe('创建用户')
    })

    it('过滤无 title 的路由', () => {
      const matched: RouteMatched[] = [
        { path: '/', meta: {} },
        { path: '/users', meta: { title: '用户管理' } }
      ]
      const breadcrumbs = computeBreadcrumbs(matched)
      expect(breadcrumbs.length).toBe(1)
      expect(breadcrumbs[0].title).toBe('用户管理')
    })

    it('空路由返回空数组', () => {
      const breadcrumbs = computeBreadcrumbs([])
      expect(breadcrumbs.length).toBe(0)
    })
  })

  describe('面包屑路径', () => {
    it('每个面包屑项包含 path 和 title', () => {
      const mockRouteMatched: RouteMatched[] = [
        { path: '/', meta: { title: '首页' } },
        { path: '/users', meta: { title: '用户管理' } }
      ]
      const breadcrumbs = computeBreadcrumbs(mockRouteMatched)
      breadcrumbs.forEach(item => {
        expect(item).toHaveProperty('path')
        expect(item).toHaveProperty('title')
      })
    })
  })
})