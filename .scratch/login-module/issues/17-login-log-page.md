---
status: needs-triage
---

# 登录日志页面

## Parent

参考 `.scratch/login-module/PRD.md`

## What to build

实现登录日志页面UI组件，包含日志列表、搜索筛选、导出Excel、登出类型样式区分。

核心功能：
- 搜索栏（工号、姓名、登录状态、日期范围、导出按钮）
- 数据表格（工号、姓名、登录时间、IP、设备、状态、失败原因、登出时间、登出类型）
- 导出Excel按钮

## Acceptance criteria

- [ ] 创建 `src/views/login-log/LoginLogList.vue` 登录日志页面
- [ ] 创建 `src/api/login-log.ts` 登录日志API封装：
  - `getLoginLogs(params)` - 查询列表
  - `exportLoginLogs(params)` - 导出Excel
- [ ] 搜索栏：
  - 工号输入框（带User图标）
  - 姓名输入框
  - 登录状态下拉选择（全部/成功/失败）
  - 日期范围选择器（Element Plus DatePicker range模式）
  - 搜索按钮、重置按钮
  - 导出按钮（右上角，主色背景）
- [ ] 数据表格：
  - 工号列：等宽字体(IBMMono)，主色(#0EA5E9)
  - 姓名列：普通字体
  - 登录时间列：等宽字体，YYYY-MM-DD HH:mm:ss格式
  - 登录IP列：等宽字体，Cyan色(#22D3EE)
  - 设备信息列：普通字体，过长截断hover显示完整
  - 状态列：Badge徽章 - 成功(绿色Success)、失败(红色Error)
  - 失败原因列：红色文字(#F43F5E)，成功时显示"-"
  - 登出时间列：等宽字体，未登出显示"-"
  - 登出类型列：Badge徽章 - 手动退出(灰色)、强制登出(橙色Warning)、Token过期(红色Error)
- [ ] 导出Excel：
  - 点击导出按钮调用API
  - 显示导出进度提示
  - 导出成功自动下载文件
  - 文件名格式：login_logs_YYYYMMDD_HHmmss.xlsx
- [ ] 表格分页：Element Plus Pagination组件
- [ ] 使用v-permission指令控制导出按钮：v-permission="login-log:export"
- [ ] 编写组件测试覆盖关键交互

## Blocked by

- #03-frontend-skeleton（需要前端骨架）
- #09-login-log（需要登录日志接口）
- #11-main-layout（需要路由和权限指令）

## Comments

_在此追加对话历史和讨论记录_