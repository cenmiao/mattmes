---
status: needs-triage
---

# 登录日志记录与查询

## Parent

参考 `.scratch/login-module/PRD.md`

## What to build

实现登录日志自动记录和查询导出功能，满足制造业审计要求。

核心功能：
- 登录时自动记录（时间、IP、设备信息、结果、失败原因）
- 登出时更新记录（登出时间、登出类型）
- 查询日志列表（搜索、筛选）
- 按日期范围筛选
- 导出Excel

## Acceptance criteria

- [ ] 创建 `SysLoginLog.java` 实体类和 Mapper
  - 字段：id, user_no, login_time, login_ip, device_info, login_result(1成功/0失败), fail_reason, logout_time, logout_type(手动/强制/过期), 审计字段
- [ ] 创建 `LoginLogService.java` 实现日志记录和查询逻辑
- [ ] 创建 `LoginLogController.java` 提供REST接口：
  - GET /api/login-logs - 查询日志列表
  - GET /api/login-logs/export - 导出Excel
- [ ] 登录成功时（#4 LoginService）自动调用LoginLogService记录：
  - login_time=当前时间
  - login_ip=请求IP
  - device_info=User-Agent等
  - login_result=1
- [ ] 登录失败时记录：
  - login_result=0
  - fail_reason=具体原因（密码错误、锁定、账号禁用等）
- [ ] 登出时更新日志记录：
  - logout_time=当前时间
  - logout_type=MANUAL(手动点击退出) / FORCE(被强制登出) / EXPIRED(Token过期)
- [ ] 被强制登出时更新日志logout_type=FORCE
- [ ] 查询列表支持参数：userNo(搜索)、name(搜索)、loginResult(筛选)、dateRange(筛选)
- [ ] 导出Excel包含所有字段，文件名格式：login_logs_YYYYMMDD_HHmmss.xlsx
- [ ] 编写 `LoginLogServiceTest.java` 单元测试覆盖：
  - 记录登录成功
  - 记录登录失败（各种原因）
  - 更新登出记录（各种类型）
  - 查询列表各种筛选条件
  - 导出Excel生成正确

## Blocked by

- #01-database-schema（需要表结构）
- #02-backend-skeleton（需要项目骨架）
- #04-login-auth-core（依赖登录流程调用）

## Comments

_在此追加对话历史和讨论记录_