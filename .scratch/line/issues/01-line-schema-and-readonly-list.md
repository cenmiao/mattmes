# 01: 线体建表与只读列表

**What to build:** 打通从数据库到浏览器的最窄完整路径。管理员登录后能在侧边栏看到"线体管理"并点进列表页，页面分页展示线体（此时为空表），支持按线体编码、线体名称模糊查询与按启用状态筛选。这一票同时建立 `line` 模块权限与 `line:read`、`line:add`、`line:edit`、`line:delete`、`line:export` 五个权限点，后续票只消费不新建。路由的 `meta.permission` 与菜单项的 `permission` 两处都要配 `line:read`，不复制工序在这两处的遗漏。

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] `mes_line` 表可重复执行建表而无需报错，含 `id`、`line_code`（唯一）、`line_name`、`description`、`enable`、四个审计字段、`deleted`
- [ ] 唯一索引建在 `line_code` 上，`enable` 与 `deleted` 各有普通索引
- [ ] `line` 模块权限与五个按钮权限的种子脚本可重复执行，重复运行不产生重复记录
- [ ] 分页查询接口接受编码、名称、状态与分页参数，返回 `{ list, total, pageNum, pageSize }`
- [ ] 编码与名称支持模糊匹配，状态为精确匹配，三个条件可组合使用
- [ ] 列表按 ID 倒序返回
- [ ] 前端页面可挂载，查询条件区含编码、名称、状态下拉与查询、重置按钮
- [ ] 表格含多选列、ID、线体编码、线体名称、描述、启用状态、创建人、创建时间、操作列
- [ ] 描述列过长时省略显示并可悬浮查看全文
- [ ] 路由路径为复数形式，与料号一致
- [ ] 未登录访问被重定向到登录页；已登录但无 `line:read` 被重定向到 403
- [ ] 菜单项仅在拥有 `line:read` 时可见，图标使用 Connection
- [ ] 重置按钮清空三个查询条件并回到第一页
