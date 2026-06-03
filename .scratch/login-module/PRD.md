---
name: login-module-prd
type: prd
---

# MES系统登录模块PRD

## 问题陈述

MES制造执行系统需要实现用户登录和权限管理功能,作为系统的基础安全模块。当前系统没有任何认证机制,无法识别操作人员身份,也无法控制不同人员的操作权限。

系统需要解决以下核心问题:
- **身份认证**: 验证用户身份,确保只有授权人员可以访问系统
- **权限控制**: 控制不同角色用户可访问的功能范围,精确到按钮级别
- **操作追溯**: 记录所有用户的登录行为和操作轨迹,满足制造业审计要求
- **安全管理**: 防止未授权访问,保护敏感数据和生产工艺信息

## 解决方案

实现一个完整的登录模块,包含:

**后端部分**:
- 用户管理(创建、编辑、禁用、删除)
- 角色管理(动态创建、权限分配)
- 权限管理(扁平化权限定义)
- 登录认证(工号+密码验证、Token生成)
- 登录日志(记录、查询、导出)
- 永久Token管理(手动生成、用途说明)

**前端部分**:
- 登录页面(工号密码输入、错误处理、并发登录处理)
- 修改密码页面(密码强度检查、首次登录强制修改)
- 用户管理页面(CRUD、角色分配、状态切换)
- 角色管理页面(CRUD、权限分配)
- 权限管理页面(树形管理)
- 登录日志页面(查询、导出)
- 主布局(侧边栏菜单、顶部导航、用户下拉菜单)

**核心特性**:
- 动态角色创建(无需固定角色)
- 多角色叠加(权限取并集)
- 按钮级权限控制
- 单终端登录限制
- 强制密码修改(首次登录、过期)
- 登录错误锁定(连续5次错误锁定10分钟)
- 30天未登录自动禁用
- 账号在其他设备登录提醒和强制登出

## 用户故事

### 1. 登录认证

1. As a 系统使用者, I want 通过工号和密码登录系统, so that 我可以开始使用系统进行生产操作
2. As a 首次登录用户, I want 系统强制我修改初始密码, so that 确保账号安全
3. As a 30天未修改密码的用户, I want 系统强制我修改密码, so that 符合安全策略
4. As a 输入错误密码的用户, I want 系统提示我密码错误并显示剩余尝试次数, so that 我知道距离锁定还有多少机会
5. As a 连续5次输入错误密码的用户, I want 系统锁定我的账号10分钟并显示剩余锁定时间, so that 防止暴力破解
6. As a 在其他设备登录的用户, I want 系统提醒我账号已在其他地方登录, so that 我知道可能存在安全风险
7. As a 想要强制登出其他设备的用户, I want 系统允许我确认强制登录, so that 我可以接管账号控制权
8. As a 被强制登出的用户, I want 系统立即提示我账号已在其他设备登录, so that 我知道需要重新认证
9. As a 被管理员禁用的用户, I want 系统立即拒绝我的请求并提示账号已禁用, so that 我知道无法继续使用系统
10. As a 修改密码的用户, I want 系统强制我使用旧设备重新登录, so that 确保密码修改生效

### 2. 用户管理

11. As a 系统管理员, I want 创建新用户账号并指定工号、姓名、密码, so that 为新员工开通系统访问权限
12. As a 系统管理员, I want 为用户分配多个角色, so that 用户可以拥有组合权限
13. As a 系统管理员, I want 编辑用户的姓名、手机号、邮箱信息, so that 维护用户档案
14. As a 系统管理员, I want 重置用户密码为默认密码, so that 帮助忘记密码的用户恢复访问
15. As a 系统管理员, I want 禁用用户账号并记录禁用原因, so that 阻止离职或违规人员访问系统
16. As a 系统管理员, I want 启用被自动禁用的用户账号, so that 恢复长期未登录用户的访问权限
17. As a 系统管理员, I want 删除用户账号, so that 清理不再需要的账号数据
18. As a 系统管理员, I want 查看用户的最后登录时间和当前状态, so that 监控账号活跃度
19. As a 系统管理员, I want 通过工号或姓名搜索用户, so that 快速定位特定用户
20. As a 系统管理员, I want 按状态筛选用户列表, so that 分别管理启用和禁用的账号
21. As a 系统管理员, I want 按角色筛选用户列表, so that 查看特定角色的所有用户
22. As a 系统管理员, I want 不可删除或禁用超级管理员账号, so that 防止系统失去最高权限控制
23. As a 普通用户, I want 自行修改姓名、手机号、邮箱, so that 维护个人档案信息
24. As a 普通用户, I want 不可修改自己的工号, so that 保持身份标识稳定

### 3. 角色管理

25. As a 系统管理员, I want 动态创建新角色并命名, so that 根据业务需求灵活定义角色
26. As a 系统管理员, I want 为角色分配多个权限, so that 定义角色的能力范围
27. As a 系统管理员, I want 编辑角色名称和描述, so that 更新角色定义
28. As a 系统管理员, I want 禁用角色, so that 暂停该角色的权限生效
29. As a 系统管理员, I want 删除角色, so that 清理不再使用的角色
30. As a 系统管理员, I want 不可删除admin超级管理员角色, so that 保护系统最高权限角色
31. As a 系统管理员, I want 查看角色关联的用户数量, so that 了解角色的使用范围
32. As a 系统管理员, I want 查看角色的创建时间, so that 了解角色历史

### 4. 权限管理

33. As a 系统管理员, I want 创建权限并定义权限编码(如user:add), so that 精细控制系统功能点的访问
34. As a 系统管理员, I want 按模块组织权限(如用户管理模块、角色管理模块), so that 权限结构清晰易懂
35. As a 系统管理员, I want 为模块权限定义子权限(按钮级), so that 控制页面内的具体操作按钮
36. As a 系统管理员, I want 编辑权限名称和描述, so that 更新权限说明
37. As a 系统管理员, I want 删除权限, so that 清理不再使用的权限定义
38. As a 系统管理员, I want 查看权限树形结构, so that 直观了解权限层级关系

### 5. 登录日志

39. As a 系统管理员, I want 查看所有用户的登录记录, so that 监控系统访问情况
40. As a 系统管理员, I want 查看登录失败记录和失败原因, so that 发现异常登录尝试
41. As a 系统管理员, I want 查看用户的登录IP和设备信息, so that 识别异常访问来源
42. As a 系统管理员, I want 查看用户的登出时间和登出类型, so that 了解会话结束方式
43. As a 系统管理员, I want 按工号搜索登录日志, so that 查看特定用户的登录历史
44. As a 系统管理员, I want 按登录状态筛选日志(成功/失败), so that 分别查看正常和异常登录
45. As a 系统管理员, I want 按日期范围筛选登录日志, so that 查看特定时段的登录活动
46. As a 系统管理员, I want 导出登录日志数据, so that 用于安全审计和合规检查
47. As a 审计人员, I want 长期保留登录日志记录, so that 满足制造业合规审计要求

### 6. 永久Token管理

48. As a 系统管理员, I want 为用户生成永久Token并记录用途说明, so that 允许外部系统或脚本调用API
49. As a 系统管理员, I want 查看永久Token列表和用途说明, so that 了解哪些外部系统有API访问权限
50. As a 系统管理员, I want 删除永久Token, so that 取消外部系统的API访问权限

### 7. 超级管理员

51. As a 超级管理员, I want 自动拥有所有权限无需逐一配置, so that 减少权限管理工作量
52. As a 超级管理员, I want 系统跳过我的权限验证检查, so that 提高操作效率
53. As a 超级管理员, I want 使用默认工号admin和默认密码Admin@123首次登录, so that 快速初始化系统

### 8. 权限控制体验

54. As a 拥有特定权限的用户, I want 看到侧边栏菜单中我有权限访问的菜单项, so that 界面简洁不显示无权限功能
55. As a 拥有特定权限的用户, I want 隐藏我没有权限的操作按钮, so that 不会误操作触发无权限提示
56. As a 缺少特定权限的用户, I want 尝试访问无权限路由时被重定向到403页面, so that 知道该功能不可访问
57. As a 未登录用户, I want 尝试访问认证路由时被重定向到登录页, so that 提示我需要先登录

## 实现决策

### 架构决策

**后端架构**: Maven多模块项目
- `mattmes-common`: 通用组件(工具类、配置、异常处理、拦截器)
- `mattmes-system`: 系统管理(用户、角色、权限、登录、日志)
- `mattmes-business`: 业务模块(预留,未来扩展SN、过站等)
- `mattmes-web`: Web层(Controller、启动类,整合所有模块)

模块依赖关系:
```
mattmes-web → mattmes-business → mattmes-system → mattmes-common
```

**前端架构**: Vue3单页应用
- Hash模式路由(`#/path`)
- Pinia状态管理
- Element Plus UI组件
- Vite构建工具
- TypeScript

### 数据库设计

**表结构**:

`sys_user` 用户表:
- id (主键)
- user_no (工号,唯一索引,等同于用户名)
- password (BCrypt加密存储)
- name (姓名)
- phone (手机号,可选)
- email (邮箱,可选)
- status (状态: 1=启用, 0=禁用)
- disable_reason (禁用原因: ADMIN_MANUAL=管理员手动禁用, AUTO_INACTIVE=30天未登录自动禁用)
- password_update_time (密码最后修改时间)
- login_error_count (连续登录错误次数)
- lock_time (锁定截止时间)
- last_login_time (最后登录时间)
- current_token (当前登录Token,用于单终端登录控制)
- token_expire_time (Token过期时间)
- created_by, create_time, updated_by, update_time (审计字段)

`sys_role` 角色表:
- id (主键)
- role_name (角色名称)
- role_code (角色编码,唯一,如SUPER_ADMIN)
- status (状态: 1=启用, 0=禁用)
- 审计字段

`sys_permission` 权限表:
- id (主键)
- permission_name (权限名称)
- permission_code (权限编码,如user:add)
- 审计字段

`sys_user_role` 用户角色关联表:
- id (主键)
- user_id
- role_id
- 审计字段

`sys_role_permission` 角色权限关联表:
- id (主键)
- role_id
- permission_id
- 审计字段

`sys_permanent_token` 永久Token表:
- id (主键)
- user_id (所属用户)
- token (UUID格式,唯一索引)
- description (用途说明)
- 审计字段

`sys_login_log` 登录日志表:
- id (主键)
- user_no (工号)
- login_time (登录时间)
- login_ip (登录IP)
- device_info (设备信息)
- login_result (登录结果: 1=成功, 0=失败)
- fail_reason (失败原因)
- logout_time (退出时间)
- 审计字段

**审计字段规范**: 所有表必须包含created_by, create_time, updated_by, update_time四个字段。

### Token存储方案

**临时方案**: 数据库存储(开发阶段)
- 在sys_user表添加current_token和token_expire_time字段
- 优点: 无需额外组件,便于开发调试
- 缺点: 增加数据库查询压力
- 迁移路径: 定义TokenStorage接口,未来实现RedisTokenStorage替换DatabaseTokenStorage

**Token格式**:
- 临时Token: JWT格式,Payload包含{userId, userNo, exp, iat},有效期6小时
- 永久Token: UUID格式,存储在sys_permanent_token表,长期有效

### 安全方案

**密码加密**: BCrypt算法(Spring Security BCryptPasswordEncoder)
- 自动加盐,每次加密生成不同哈希值
- 默认强度10,防止彩虹表攻击

**密码强度要求**:
- 至少8位
- 必须包含字母+数字

**密码安全策略**:
- 首次登录强制修改密码
- 每30天必须修改密码
- 连续错误5次锁定10分钟
- 30天未登录自动禁用账号

**并发登录控制**:
- 单终端登录限制(同一用户同一时间只能在一个终端登录)
- 新登录时检测已有在线会话,提示用户选择强制登录或取消
- 使用数据库事务锁确保并发登录请求只有一个成功

**Token立即失效场景**:
1. 用户被管理员禁用 → Token立即失效,返回401
2. 用户修改密码 → 旧Token立即失效,强制重新登录
3. 用户在其他设备强制登录 → 旧设备Token立即失效

### API设计

**统一响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

**标准错误码**:
- 200: 操作成功
- 400: 业务异常(参数错误、密码强度不足、用户名不存在等)
- 401: 认证失败(Token无效、Token过期、被强制登出)
- 403: 权限不足
- 409: 业务冲突(账号已在其他设备登录,需要确认强制登录)
- 500: 系统异常(后端代码异常)

**被强制登出响应**:
```json
{
  "code": 401,
  "message": "您的账号已在其他设备登录,请重新登录",
  "data": null
}
```

**账号在其他设备登录响应**:
```json
{
  "code": 409,
  "message": "账号已在其他地方登录,是否强制登录?",
  "data": {
    "currentLoginTime": "2026-05-30 10:30:00",
    "currentLoginIp": "192.168.1.100"
  }
}
```

### 前端路由与权限控制

**路由分类**:
- 公共路由: /login, /403, /404 (无需认证)
- 认证路由: /dashboard, /change-password (需登录)
- 权限路由: /users, /roles, /permissions, /login-logs (需登录+特定权限)

**路由守卫逻辑** (来自ADR 0003):
```javascript
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = localStorage.getItem('userInfo')
  const needChangePassword = localStorage.getItem('needChangePassword')

  // 1. 登录页检查
  if (to.path === '/login') {
    if (token) next('/dashboard')
    else next()
    return
  }

  // 2. 公共页面
  if (to.meta.public) {
    next()
    return
  }

  // 3. 认证检查
  if (!token) {
    next('/login')
    return
  }

  // 4. 密码修改检查
  if (needChangePassword === 'true' && to.path !== '/change-password') {
    next('/change-password')
    return
  }

  // 5. 权限检查
  if (to.meta.permission) {
    const permissions = JSON.parse(userInfo).permissions || []
    if (!permissions.includes(to.meta.permission)) {
      next('/403')
      return
    }
  }

  next()
})
```

**权限指令** (来自ADR 0003):
```javascript
app.directive('permission', {
  mounted(el, binding) {
    const permission = binding.value
    const permissions = JSON.parse(localStorage.getItem('userInfo')).permissions
    if (!permissions.includes(permission)) {
      el.parentNode?.removeChild(el)
    }
  }
})
```

**菜单管理**: 前端代码写死菜单配置(未来可考虑数据库存储)

### 系统初始化

**默认超级管理员**:
- 工号: admin
- 默认密码: Admin@123
- 默认角色: SUPER_ADMIN (超级管理员)
- 首次登录强制修改密码

**超级管理员特权**:
- 自动拥有所有权限,无需逐一配置
- 后端权限验证拦截器检测到该角色时跳过验证
- 前端禁止删除或禁用超级管理员账号

### 前端设计规范

**设计风格**: 工业精密风格(深色基调)

**核心色板**:
- 主色: #0EA5E9 (Sky-500 精密蓝)
- 主色浅: #38BDF8 (Sky-400)
- 主色深: #0284C7 (Sky-600)
- 成功: #10B981 (Emerald-500)
- 警告: #F59E0B (Amber-500)
- 错误: #F43F5E (Rose-500)
- 数据高亮: #22D3EE (Cyan-400)

**表面色(背景层级)**:
- L0: #0F172A (最深背景)
- L1: #1E293B (主背景、侧边栏)
- L2: #334155 (卡片、表格背景)
- L3: #475569 (边框、分隔线)

**内容色**:
- C1: #F8FAFC (主文字)
- C2: #CBD5E1 (次要文字)
- C3: #94A3B8 (辅助文字)
- C4: #64748B (禁用文字)

**字体规范**:
- 标题/品牌: JetBrains Mono, SF Mono
- 正文: Inter, 系统字体
- 数据展示: IBM Plex Mono (等宽,用于工号、IP、时间)

**间距规范**: xs(4px), sm(8px), md(16px), lg(24px), xl(32px), 2xl(48px)

**圆角规范**: sm(4px), md(8px), lg(12px), xl(16px)

### 前端页面设计

**登录页** (/login):
- 居中卡片式布局
- 网格背景图案(50px间距) + 主色辉光球视觉效果
- 工号输入框(带User图标) + 密码输入框(带Lock图标,可显示密码)
- 登录按钮(带箭头图标)
- 密码要求提示文字
- 账号冲突弹窗(显示上次登录时间、IP,提供取消和确认登录按钮)
- 错误提示弹窗(显示错误原因,密码锁定时显示剩余时间)

**修改密码页** (/change-password):
- 居中卡片式布局,与登录页风格一致
- 原因提示(首次登录/密码过期/普通修改)
- 旧密码输入框(非首次登录时显示)
- 新密码输入框
- 密码强度指示器(进度条+文字: 弱/中等/强)
- 密码要求检查列表(三项: 8位、字母、数字)
- 确认密码输入框
- 提交按钮

**用户管理页** (/users):
- 搜索栏: 工号/姓名输入框,状态下拉选择,角色下拉选择,搜索/重置按钮
- 数据表格: 工号(等宽字体+主色), 姓名, 部门(部门徽章), 角色(角色标签组), 状态(Switch开关), 最后登录(等宽字体), 操作(图标按钮组)
- 操作按钮: 编辑、重置密码、删除(图标按钮,悬停变色)
- 弹窗: 新增/编辑用户弹窗, 重置密码确认弹窗

**角色管理页** (/roles):
- 数据表格: 角色名称(带角色图标), 角色编码(等宽字体), 描述, 用户数(数字徽章), 创建时间(等宽字体), 操作(图标按钮组)
- 特殊逻辑: admin角色不可删除(删除按钮禁用)
- 弹窗: 新增/编辑角色弹窗, 分配权限弹窗(树形结构)

**权限管理页** (/permissions):
- 数据表格: 树形展开表格
- 列: 权限名称(模块图标/按钮图标), 权限编码(等宽字体+Cyan色), 类型(模块/按钮徽章), 描述, 创建时间(等宽字体), 操作(图标按钮组)
- 权限类型样式: 模块(蓝色徽章), 按钮(灰色徽章)

**登录日志页** (/login-logs):
- 搜索栏: 工号输入框(带User图标), 姓名输入框, 登录状态下拉选择, 日期范围选择器, 搜索/重置按钮, 导出按钮(右上角)
- 数据表格: 工号(等宽字体+主色), 姓名, 登录时间(等宽字体), 登录IP(等宽字体+Cyan色), 设备信息, 状态(成功/失败徽章), 失败原因(红色文字), 登出时间(等宽字体), 登出类型(类型徽章)
- 登出类型样式: 手动退出(灰色徽章), 强制登出(橙色徽章), Token过期(红色徽章)

**主布局** (/dashboard):
- Sidebar (侧边栏 - 240px / 收起 72px): Logo, Menu (动态路由), Version
- Header (顶部导航 - 64px): CollapseBtn (折叠按钮), Breadcrumb (面包屑), UserDropdown (用户下拉菜单)
- Content (内容区): <router-view />

## 测试决策

### 测试策略

**测试原则**: 只测试外部行为,不测试实现细节

**测试范围**:
- 所有Service层接口进行单元测试
- Controller层接口进行集成测试
- 前端关键组件进行单元测试(Vue Test Utils)
- 权限拦截器进行单元测试
- Token验证逻辑进行单元测试
- 密码强度验证进行单元测试
- 登录流程进行端到端测试

### 测试重点

**后端测试模块**:
1. UserService: 用户CRUD、状态切换、密码重置、30天未登录自动禁用
2. RoleService: 角色CRUD、权限分配
3. PermissionService: 权限CRUD、树形结构
4. LoginService: 登录验证、密码错误锁定、并发登录处理、强制登出、首次登录强制修改密码
5. LoginLogService: 登录日志记录、查询、导出
6. TokenStorage: Token存储、验证、失效
7. PermissionInterceptor: 权限验证、超级管理员跳过验证
8. PasswordEncoder: BCrypt加密验证、密码强度验证

**前端测试模块**:
1. Login组件: 表单验证、错误提示、并发登录弹窗
2. ChangePassword组件: 密码强度检查、表单验证
3. Users组件: 表格渲染、状态切换、搜索过滤
4. Roles组件: 表格渲染、权限分配
5. router beforeEach守卫: 路由跳转逻辑
6. permission指令: 按钮显示隐藏逻辑

### 测试数据

**初始化测试数据**:
- 超级管理员账号: admin / Admin@123
- 测试用户账号: testuser / Test@123
- 测试角色: TEST_ROLE
- 测试权限: user:view, user:add, user:edit, user:delete

## 范围外内容

**不在本PRD范围内**:
- MES业务模块(SN生成、过站、检测、组装、包装)
- 数据库菜单管理(当前阶段前端写死)
- Redis Token存储(当前阶段使用数据库存储)
- 单点登录(SSO)
- "记住我"功能
- 用户头像上传
- 用户部门管理
- 租户管理(多租户架构)
- 国际化(i18n)
- 移动端适配(响应式布局仅考虑侧边栏折叠)
- API文档自动生成(Swagger)
- 性能监控和统计
- 数据备份和恢复

## 进一步说明

### 实施顺序建议

**阶段1: 数据库与后端基础**
1. 创建数据库表结构(7张核心表)
2. 搭建SpringBoot多模块项目
3. 配置MyBatis-Plus(审计字段自动填充)
4. 创建实体类和Mapper

**阶段2: 后端核心功能**
1. 实现BCrypt密码加密
2. 实现JWT Token生成和解析
3. 实现DatabaseTokenStorage
4. 实现登录接口(包含并发登录处理)
5. 实现权限拦截器(包含超级管理员跳过验证)
6. 实现登录日志记录

**阶段3: 后端管理接口**
1. 实现用户管理接口(CRUD、状态切换、密码重置)
2. 实现角色管理接口(CRUD、权限分配)
3. 实现权限管理接口(CRUD、树形结构)
4. 实现登录日志查询接口
5. 实现登录日志导出接口
6. 实现永久Token管理接口

**阶段4: 前端基础设施**
1. 创建Vue3项目(Vite + TypeScript)
2. 配置路由和路由守卫
3. 配置Element Plus主题样式(工业精密风格)
4. 实现权限指令
5. 实现主布局组件(Sidebar、Header、Content)

**阶段5: 前端页面开发**
1. 实现登录页面(包含并发登录弹窗)
2. 实现修改密码页面(包含密码强度指示器)
3. 实现用户管理页面
4. 实现角色管理页面
5. 实现权限管理页面
6. 实现登录日志页面

**阶段6: 测试与集成**
1. 编写后端单元测试
2. 编写前端组件测试
3. 进行端到端测试
4. 系统初始化(创建超级管理员账号)
5. 首次登录验证

### 技术栈确认

**后端**:
- Java 17+
- SpringBoot 3.x
- MyBatis-Plus 3.5.x
- MySQL 8.x
- Spring Security (仅用于BCryptPasswordEncoder)
- jjwt (JWT生成和解析)

**前端**:
- Vue 3.3+
- TypeScript 5.x
- Vue Router 4.x (Hash模式)
- Pinia 2.x
- Element Plus 2.x
- Vite 4.x
- @element-plus/icons-vue

### 项目目录结构

**后端项目**: mattmes/
```
mattmes/
├── mattmes-common/
│   ├── src/main/java/com/matt/mes/common/
│   │   ├── config/ (MybatisPlusConfig, WebConfig)
│   │   ├── exception/ (BusinessException, GlobalExceptionHandler)
│   │   ├── interceptor/ (TokenInterceptor, PermissionInterceptor)
│   │   ├── utils/ (JwtUtils, PasswordUtils)
│   │   └── result/ (Result, ResultCode)
│   └── pom.xml
├── mattmes-system/
│   ├── src/main/java/com/matt/mes/system/
│   │   ├── service/
│   │   ├── mapper/
│   │   ├── entity/
│   │   └── dto/
│   └── pom.xml
├── mattmes-business/ (预留,暂无内容)
│   └ pom.xml
├── mattmes-web/
│   ├── src/main/java/com/matt/mes/
│   │   ├── controller/
│   │   └── MesApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── mapper/
│   └── pom.xml
└── pom.xml (父POM)
```

**前端项目**: mattmes-ui/
```
mattmes-ui/
├── public/
├── src/
│   ├── api/ (auth.ts, user.ts, role.ts, permission.ts, login-log.ts)
│   ├── components/ (Layout.vue)
│   ├── directives/ (permission.ts)
│   ├── router/ (index.ts, menus.ts)
│   ├── store/ (user.ts)
│   ├── styles/ (theme.css, variables.css)
│   ├── utils/ (request.ts, auth.ts)
│   ├── views/
│   │   ├── login/Login.vue
│   │   ├── change-password/ChangePassword.vue
│   │   ├── dashboard/Dashboard.vue
│   │   ├── user/UserList.vue
│   │   ├── role/RoleList.vue
│   │   ├── permission/PermissionList.vue
│   │   └── login-log/LoginLogList.vue
│   ├── App.vue
│   └── main.ts
├── package.json
├── tsconfig.json
└── vite.config.ts
```

### 关键依赖

**后端关键依赖**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

**前端关键依赖**:
```json
{
  "dependencies": {
    "vue": "^3.3.4",
    "vue-router": "^4.2.4",
    "pinia": "^2.1.6",
    "element-plus": "^2.3.12",
    "@element-plus/icons-vue": "^2.1.9",
    "axios": "^1.5.0"
  },
  "devDependencies": {
    "typescript": "^5.0.2",
    "vite": "^4.4.9",
    "@vitejs/plugin-vue": "^4.3.4",
    "vue-tsc": "^1.8.5"
  }
}
```