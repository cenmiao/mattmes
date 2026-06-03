<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">403</h1>
      <p class="error-message">您没有权限访问此页面</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--surface-l0, #0f172a);
}

.error-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  margin: 0;
  color: var(--error-color, #f43f5e);
  font-family: var(--font-brand, 'JetBrains Mono'), monospace;
}

.error-message {
  font-size: 18px;
  color: var(--content-c2, #cbd5e1);
  margin: 16px 0 32px;
}
</style>