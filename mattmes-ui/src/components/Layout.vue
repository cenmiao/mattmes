<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
      <div class="sidebar-logo">
        <span v-if="!isCollapse" class="logo-text">MES系统</span>
        <span v-else class="logo-mini">M</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        class="sidebar-menu"
        background-color="var(--surface-l1)"
        text-color="var(--content-c2)"
        active-text-color="var(--primary-color)"
      >
        <el-menu-item
          v-for="menu in visibleMenus"
          :key="menu.path"
          :index="menu.path"
        >
          <el-icon>
            <component :is="menu.icon" />
          </el-icon>
          <template #title>{{ menu.title }}</template>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <span v-if="!isCollapse" class="version">v1.0.0</span>
      </div>
    </el-aside>
    <el-container class="layout-main">
      <el-header class="layout-header">
        <div class="header-left">
          <el-button
            :icon="isCollapse ? Expand : Fold"
            @click="toggleCollapse"
            circle
            size="small"
          />
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-icon><User /></el-icon>
              <span class="user-name">{{ userName }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, Expand, User, ArrowDown, Lock, SwitchButton } from '@element-plus/icons-vue'
import { menus } from '@/router/menus'
import { logout } from '@/api/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const isCollapse = ref(false)

const userInfo = computed(() => {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : {}
})

const userName = computed(() => userInfo.value.username || userInfo.value.name || '用户')

const isSuperAdmin = computed(() => {
  return userInfo.value.roles?.some((r: { roleCode: string }) => r.roleCode === 'SUPER_ADMIN')
})

const userPermissions = computed(() => {
  return userInfo.value.permissions || []
})

const visibleMenus = computed(() => {
  if (isSuperAdmin.value) return menus
  return menus.filter(menu => {
    if (!menu.permission) return true
    return userPermissions.value.includes(menu.permission)
  })
})

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(r => r.meta?.title)
  return matched.map(r => ({
    path: r.path,
    title: r.meta?.title as string
  }))
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      // TODO: 跳转个人信息页
      break
    case 'password':
      router.push('/change-password')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await logout()
  } catch (error) {
    // 即使 API 调用失败，也清除本地状态
  }
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('needChangePassword')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: var(--surface-l0);
}

.layout-aside {
  background: var(--surface-l1);
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--surface-l2);
  padding: 0 16px;
}

.logo-img {
  width: 32px;
  height: 32px;
}

.logo-text {
  margin-left: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--content-c1);
  font-family: var(--font-brand);
}

.logo-mini {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  font-family: var(--font-brand);
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

.sidebar-footer {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid var(--surface-l2);
}

.version {
  font-size: 12px;
  color: var(--content-c4);
}

.layout-main {
  display: flex;
  flex-direction: column;
}

.layout-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--surface-l1);
  border-bottom: 1px solid var(--surface-l2);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: var(--content-c2);
}

.user-name {
  margin: 0 8px;
}

.layout-content {
  background: var(--surface-l0);
  padding: 24px;
  overflow: auto;
}
</style>