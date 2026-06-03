<template>
  <div class="change-password-container">
    <!-- 背景效果 -->
    <div class="change-password-background">
      <div class="grid-pattern"></div>
      <div class="glow-orb"></div>
    </div>

    <!-- 修改密码卡片 -->
    <div class="change-password-card">
      <h1 class="change-password-title">{{ pageTitle }}</h1>
      <p class="change-password-reason">{{ reasonText }}</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="change-password-form"
        @submit.prevent="handleSubmit"
      >
        <!-- 旧密码输入框（非首次登录时显示） -->
        <el-form-item v-if="!isFirstLogin" prop="oldPassword" label="旧密码">
          <el-input
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <!-- 新密码输入框 -->
        <el-form-item prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <!-- 密码强度指示器 -->
        <div class="password-strength">
          <div class="strength-bar">
            <div
              class="strength-bar-fill"
              :style="{ width: strengthPercent + '%', backgroundColor: strengthColor }"
            ></div>
          </div>
          <span class="strength-label" :style="{ color: strengthColor }">{{ strengthLabel }}</span>
        </div>

        <!-- 密码要求检查列表 -->
        <div class="password-requirements">
          <div class="requirement-item" :class="{ met: requirements.length }">
            <el-icon class="requirement-check"><Check /></el-icon>
            <span>至少8位字符</span>
          </div>
          <div class="requirement-item" :class="{ met: requirements.letter }">
            <el-icon class="requirement-check"><Check /></el-icon>
            <span>包含字母</span>
          </div>
          <div class="requirement-item" :class="{ met: requirements.number }">
            <el-icon class="requirement-check"><Check /></el-icon>
            <span>包含数字</span>
          </div>
        </div>

        <!-- 确认密码输入框 -->
        <el-form-item prop="confirmPassword" label="确认密码">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="!canSubmit"
            class="submit-button"
            @click="handleSubmit"
          >
            <span>确认修改</span>
            <el-icon class="submit-arrow"><ArrowRight /></el-icon>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, ArrowRight, Check } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { changePassword } from '@/api/auth'

type Scenario = 'firstLogin' | 'expired' | 'normal'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const scenario = ref<Scenario>('normal')

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 判断场景
onMounted(() => {
  const needChangePassword = localStorage.getItem('needChangePassword')
  if (needChangePassword === 'true') {
    scenario.value = 'firstLogin'
  }
  // TODO: 从后端获取密码过期状态，暂时通过路由参数判断
  const urlParams = new URLSearchParams(window.location.hash.split('?')[1] || '')
  if (urlParams.get('expired') === 'true') {
    scenario.value = 'expired'
  }
})

const isFirstLogin = computed(() => scenario.value === 'firstLogin')

const pageTitle = computed(() => {
  switch (scenario.value) {
    case 'firstLogin':
      return '设置密码'
    case 'expired':
      return '更新密码'
    default:
      return '修改密码'
  }
})

const reasonText = computed(() => {
  switch (scenario.value) {
    case 'firstLogin':
      return '首次登录，请设置您的密码'
    case 'expired':
      return '您的密码已超过30天未修改，请更新密码'
    default:
      return '修改您的密码'
  }
})

// 密码要求检查
const requirements = computed(() => ({
  length: form.newPassword.length >= 8,
  letter: /[a-zA-Z]/.test(form.newPassword),
  number: /[0-9]/.test(form.newPassword)
}))

// 密码强度计算
const strengthScore = computed(() => {
  let score = 0
  if (requirements.value.length) score++
  if (requirements.value.letter) score++
  if (requirements.value.number) score++
  return score
})

const strengthPercent = computed(() => {
  if (strengthScore.value === 0) return 0
  if (strengthScore.value === 1) return 33
  if (strengthScore.value === 2) return 66
  return 100
})

const strengthLabel = computed(() => {
  if (strengthScore.value === 0) return ''
  if (strengthScore.value <= 1) return '弱'
  if (strengthScore.value === 2) return '中等'
  return '强'
})

const strengthColor = computed(() => {
  if (strengthScore.value <= 1) return '#EF4444'
  if (strengthScore.value === 2) return '#F59E0B'
  return '#22C55E'
})

// 表单验证规则
const rules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 提交按钮是否可用
const canSubmit = computed(() => {
  const baseRequirements = requirements.value.length && requirements.value.letter && requirements.value.number
  const passwordMatch = form.newPassword === form.confirmPassword && form.newPassword.length > 0
  const hasOldPassword = isFirstLogin.value || form.oldPassword.length > 0
  return baseRequirements && passwordMatch && hasOldPassword
})

async function handleSubmit() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await changePassword({
      oldPassword: isFirstLogin.value ? undefined : form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
      isFirstLogin: isFirstLogin.value
    })

    // 清空 token
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('needChangePassword')

    ElMessage.success('密码修改成功，请重新登录')
    router.push('/login')
  } catch (error: unknown) {
    const err = error as { message?: string }
    ElMessage.error(err.message || '密码修改失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.change-password-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: var(--surface-l0);
}

/* 背景效果 */
.change-password-background {
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

/* 修改密码卡片 */
.change-password-card {
  position: relative;
  z-index: 1;
  width: 400px;
  padding: var(--spacing-2xl);
  background-color: var(--surface-l1);
  border-radius: var(--radius-xl);
  border: 1px solid var(--surface-l3);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.4);
}

.change-password-title {
  margin: 0 0 var(--spacing-xs);
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-color);
  text-align: center;
  font-family: var(--font-brand);
}

.change-password-reason {
  margin: 0 0 var(--spacing-xl);
  font-size: 14px;
  color: var(--content-c3);
  text-align: center;
}

.change-password-form {
  width: 100%;
}

.change-password-form :deep(.el-input__wrapper) {
  background-color: var(--surface-l2);
  box-shadow: none;
  border: 1px solid var(--surface-l3);
}

.change-password-form :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color);
}

.change-password-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.2);
}

/* 密码强度指示器 */
.password-strength {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.strength-bar {
  flex: 1;
  height: 4px;
  background-color: var(--surface-l3);
  border-radius: 2px;
  overflow: hidden;
}

.strength-bar-fill {
  height: 100%;
  transition: width 0.3s, background-color 0.3s;
}

.strength-label {
  font-size: 12px;
  font-weight: 500;
  min-width: 36px;
  text-align: right;
}

/* 密码要求检查列表 */
.password-requirements {
  margin-bottom: var(--spacing-lg);
}

.requirement-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 12px;
  color: var(--content-c4);
  margin-bottom: var(--spacing-xs);
}

.requirement-item.met {
  color: #22C55E;
}

.requirement-check {
  font-size: 14px;
  opacity: 0.3;
}

.requirement-item.met .requirement-check {
  opacity: 1;
}

/* 提交按钮 */
.submit-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.submit-arrow {
  margin-left: var(--spacing-xs);
}
</style>
