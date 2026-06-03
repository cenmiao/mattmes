import type { Directive, DirectiveBinding } from 'vue'

export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const value = binding.value
    const userInfoStr = localStorage.getItem('userInfo')

    if (!userInfoStr) {
      el.parentNode?.removeChild(el)
      return
    }

    const userInfo = JSON.parse(userInfoStr)
    const permissions = userInfo.permissions || []
    const roles = userInfo.roles || []
    const isSuperAdmin = roles.some((r: { roleCode: string }) => r.roleCode === 'SUPER_ADMIN')

    // 超级管理员拥有所有权限
    if (isSuperAdmin) return

    // 支持数组形式（OR 逻辑）
    if (Array.isArray(value)) {
      if (!value.some(p => permissions.includes(p))) {
        el.parentNode?.removeChild(el)
      }
    } else {
      if (!permissions.includes(value)) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}

export default permission