import type { Component } from 'vue'
import {
  Odometer,
  User,
  UserFilled,
  Lock,
  Document,
  Setting,
  FolderOpened,
  Box
} from '@element-plus/icons-vue'

export interface MenuItem {
  path: string
  title: string
  icon?: Component
  permission?: string
  children?: MenuItem[]
}

export const menus: MenuItem[] = [
  {
    path: '/dashboard',
    title: '仪表盘',
    icon: Odometer
  },
  {
    path: '/users',
    title: '用户管理',
    icon: User,
    permission: 'user:read'
  },
  {
    path: '/roles',
    title: '角色管理',
    icon: UserFilled,
    permission: 'role:read'
  },
  {
    path: '/permissions',
    title: '权限管理',
    icon: Lock,
    permission: 'permission:read'
  },
  {
    path: '/login-logs',
    title: '登录日志',
    icon: Document,
    permission: 'login-log:read'
  },
  {
    path: '/process',
    title: '工序管理',
    icon: Setting
  },
  {
    path: '/projects',
    title: '项目管理',
    icon: FolderOpened
  },
  {
    path: '/materials',
    title: '料号管理',
    icon: Box
  }
]