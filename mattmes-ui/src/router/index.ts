import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { public: true }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { public: true }
  },
  {
    path: '/change-password',
    name: 'ChangePassword',
    component: () => import('@/views/ChangePassword.vue'),
    meta: { title: '修改密码' }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', permission: 'user:read' }
      },
      {
        path: 'roles',
        name: 'RoleList',
        component: () => import('@/views/role/RoleList.vue'),
        meta: { title: '角色管理', permission: 'role:read' }
      },
      {
        path: 'permissions',
        name: 'PermissionList',
        component: () => import('@/views/permission/PermissionList.vue'),
        meta: { title: '权限管理', permission: 'permission:read' }
      },
      {
        path: 'login-logs',
        name: 'LoginLogList',
        component: () => import('@/views/login-log/LoginLogList.vue'),
        meta: { title: '登录日志', permission: 'login-log:read' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

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
    const roles = userInfo ? JSON.parse(userInfo).roles || [] : []
    const isSuperAdmin = roles.some((r: { roleCode: string }) => r.roleCode === 'SUPER_ADMIN')

    if (!isSuperAdmin && !permissions.includes(to.meta.permission as string)) {
      next('/403')
      return
    }
  }

  next()
})

export default router