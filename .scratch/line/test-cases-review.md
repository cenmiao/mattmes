# 测试案例审查 — line

- 审查对象：test-cases.md（52 条案例）
- 基准：spec.md、issues/（76 条 AC）

## Coverage

- [hard] `01@12` 的「图标使用 Connection」没有任何案例断言：TC-010 只验证菜单项出现并能进入列表页，TC-011 只验证无权限时菜单项不渲染；把菜单图标换成任意图标，两条案例仍绿，该子句也未进入「未覆盖」 — back_to: to-test-cases
- [hard] `02@10` 的「长度」前端校验分支没有案例：TC-021 的 `当` 只提交空表单与非法字符编码，`则` 也只断言必填与格式提示，未提交超长值；`02@4` / `03@3` 进「未覆盖」不能替代 `02@10` 明确要求的前端长度校验 — back_to: to-test-cases
- [soft] `01@9` 的「省略显示」没有被显式断言：TC-006 只断言悬浮后出现全文浮层（`show-overflow-tooltip` 下可视为间接证明），未断言单元格文本确实被截断；建议补一条省略/截断的可观察断言 — back_to: to-test-cases

## Fidelity

- [hard] 整个文件（TC-001–TC-052）都是 Playwright/e2e 浏览器场景（登录重定向、菜单、下载文件、悬浮浮层），而 spec 的 `Out of Scope` 明文写着「E2E 测试：不新增 Playwright/e2e 用例，待真实联调时再补」；交付物与需求范围直接冲突 — back_to: to-spec
- [hard] 案例不落在 `Testing Decisions` 声明的两个缝隙上：缝隙 1 是 `MockMvc` 打 `/api/line/**`，缝隙 2 是 `mount(LineList)` + `vi.mock('@/api/line')`，且声明「均复用既有，不新建」；这批案例走的是浏览器全链路。典型如 TC-045 / TC-047 / TC-049 断言 HTTP 响应体的 BOM、CSV 表头与转义，本属缝隙 1 的内容，却通过下载文件验证 — back_to: to-spec
- [soft] `Testing Decisions` 缝隙 2 点名「页面挂载后触发列表查询」，案例里没有对应断言：TC-004 只验证查询控件与按钮渲染，未验证挂载即发起列表请求 — back_to: to-test-cases

## Quality

- [soft] 同形态数据重复声明（E13）：`line-delete-scope`、`line-batch-delete-1`、`line-batch-delete-2` 都是「同一前缀的 2 条启用线体」，`line-status-pool` 是「3 条启用线体」；`line-query-a` / `line-edit-target` / `line-delete-query` / `line-delete-confirm` 也都是单条启用线体的同形声明，可合并为带序号的数据池 — back_to: to-test-cases
- [soft] TC-031 混合了两个行为族：既验证开关显示映射（AC `04@5`），又验证启用/禁用筛选，标题需用「与」才能概括；后半段与 TC-002 的筛选断言重复 — back_to: to-test-cases
- [soft] TC-013 的「ID 列有值且唯一」与 TC-020 的「全程未手动触发查询」不是可从页面状态断言的可观测业务值：单条新行无法验证 ID 唯一性，后者是测试过程描述；删掉这两句案例仍会通过 — back_to: to-test-cases
- [soft] 多条案例把 spec/tickets 未规定的字面值写死：TC-014 / TC-015 / TC-016 的具体校验提示、TC-030 的「状态更新成功」、TC-034 的「线体不存在」（`04@8` 只要求显示服务端错误信息，未规定文案）、TC-046 / TC-051 的文件名主名「线体数据」、TC-051 的「导出成功」。AC 只要求行为与字段，实现换文案或文件名即误报 — back_to: to-test-cases
- [soft] 多条案例的 `则` 里混入了 `当` 未包含的操作：TC-010「进入后到达线体列表页」、TC-029「在重新查询后保持」、TC-040「取消后该行仍在列表中」；步骤与预期不闭合，应把额外操作补进 `当` 或删去 — back_to: to-test-cases

---
summary: 4 hard / 7 soft
