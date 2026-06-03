<template>
  <div class="login-container">
    <!-- 背景效果 -->
    <div class="login-background">
      <div class="grid-pattern"></div>
      <div class="glow-orb"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <h1 class="login-title">MES系统登录</h1>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <!-- 工号输入框 -->
        <el-form-item prop="userNo">
          <el-input
            v-model="form.userNo"
            placeholder="请输入工号"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            <span>登录</span>
            <el-icon class="login-arrow"><ArrowRight /></el-icon>
          </el-button>
        </el-form-item>

        <!-- 密码要求提示 -->
        <p class="password-hint">密码需至少8位，包含字母和数字</p>
      </el-form>
    </div>

    <!-- 并发登录冲突弹窗 -->
    <el-dialog
      v-model="showConflictDialog"
      title="账号已在其他地方登录"
      width="400px"
      :close-on-click-modal="false"
    >
      <p>检测到您的账号已在其他设备登录：</p>
      <p class="conflict-info" v-if="conflictInfo">
        登录时间：{{ conflictInfo.loginTime }}<br />
        登录IP：{{ conflictInfo.loginIp }}
      </p>
      <p>是否强制登录？强制登录将使其他设备下线。</p>
      <template #footer>
        <el-button @click="showConflictDialog = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleForceLogin">
          确认登录
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowRight } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { login, type LoginResponse } from '@/api/auth'

interface ConflictInfo {
  loginTime: string
  loginIp: string
}

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const showPassword = ref(false)
const showConflictDialog = ref(false)
const conflictInfo = ref<ConflictInfo | null>(null)

const form = reactive({
  userNo: '',
  password: ''
})

const rules: FormRules = {
  userNo: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

function handleLoginSuccess(response: LoginResponse) {
  localStorage.setItem('token', response.token)
  localStorage.setItem('userInfo', JSON.stringify({
    id: response.userId,
    userNo: response.userNo,
    name: response.name,
    permissions: response.permissions || [],
    roles: response.roles || []
  }))
  localStorage.setItem('needChangePassword', String(response.needChangePassword))

  ElMessage.success('登录成功')

  if (response.needChangePassword) {
    router.push('/change-password')
  } else {
    router.push('/dashboard')
  }
}

async function handleLogin() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const response: LoginResponse = await login({
      userNo: form.userNo,
      password: form.password
    })
    handleLoginSuccess(response)
  } catch (error: unknown) {
    const err = error as { status?: number; data?: { loginTime?: string; loginIp?: string }; message?: string }

    // 409 并发登录冲突
    if (err.status === 409 || (err as { code?: number }).code === 409) {
      conflictInfo.value = {
        loginTime: err.data?.loginTime || '未知',
        loginIp: err.data?.loginIp || '未知'
      }
      showConflictDialog.value = true
    } else {
      ElMessage.error(err.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

async function handleForceLogin() {
  loading.value = true
  try {
    const response: LoginResponse = await login({
      userNo: form.userNo,
      password: form.password,
      forceLogin: true
    })
    showConflictDialog.value = false
    handleLoginSuccess(response)
  } catch (error: unknown) {
    const err = error as { message?: string }
    ElMessage.error(err.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: var(--surface-l0);
}

/* 背景效果 */
.login-background {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.grid-pattern {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(var(--surface-l3) 1px, transparent 1px),
    linear-gradient(90deg, var(--surface-l3) 1px, transparent 1px);
  background-size: 50px 50px;
  opacity: 0.3;
}

.glow-orb {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}

/* 登录卡片 */
.login-card {
  position: relative;
  z-index: 1;
  width: 400px;
  padding: var(--spacing-2xl);
  background-color: var(--surface-l1);
  border-radius: var(--radius-xl);
  border: 1px solid var(--surface-l3);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.4);
}

.login-title {
  margin: 0 0 var(--spacing-xl);
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-color);
  text-align: center;
  font-family: var(--font-brand);
}

.login-form {
  width: 100%;
}

.login-form :deep(.el-input__wrapper) {
  background-color: var(--surface-l2);
  box-shadow: none;
  border: 1px solid var(--surface-l3);
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.2);
}

.login-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.login-arrow {
  margin-left: var(--spacing-xs);
}

.password-hint {
  margin: var(--spacing-md) 0 0;
  font-size: 12px;
  color: var(--content-c4);
  text-align: center;
}

/* 冲突弹窗 */
.conflict-info {
  margin: var(--spacing-md) 0;
  padding: var(--spacing-sm);
  background-color: var(--surface-l2);
  border-radius: var(--radius-sm);
  font-family: var(--font-data);
  color: var(--content-c2);
}
</style>
