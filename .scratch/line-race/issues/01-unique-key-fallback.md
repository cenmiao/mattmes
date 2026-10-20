# 01: 线体新增的唯一键冲突兜底

**What to build:** 在 `LineServiceImpl.add` 的插入路径上捕获数据库唯一键冲突（`DuplicateKeyException`），翻译成 `BusinessException(400, "线体编码已存在")`，与前置 `countByCodeIncludeDeleted` 查重失败使用同一错误码与文案。前置查重逻辑保留不动——它覆盖常见路径的友好提示，本次新增的兜底覆盖"两请求同时通过查重"的并发窗口。

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] `LineServiceImpl.add` 在 `lineMapper.insert` 处捕获 `DuplicateKeyException`
- [ ] 捕获后抛出 `BusinessException(400, "线体编码已存在")`（与前置查重**文案与错误码完全一致**）
- [ ] 前置 `countByCodeIncludeDeleted` 查重逻辑保持不变
- [ ] 其他异常类型不被吞掉（只捕获 `DuplicateKeyException`，不写 `catch (Exception)`）
- [ ] 新增测试覆盖「绕过前置查重直接撞唯一键」的路径，断言返回 400 而非 500
- [ ] 既有 `LineControllerTest` 全部通过（`mvn test -pl mattmes-web -am -Dtest=LineControllerTest`）
- [ ] 不改表结构、不改 Controller 签名、不改任何前端文件

## 验证方式

- 后端单测：`LineControllerTest` 新增用例（模拟 `insert` 抛 `DuplicateKeyException`）
- 手动接口验证：用写死的重复编码连续两次 POST `/api/line/add`，确认第二次为 400

## 说明

本票刻意限定为**纯后端改动**：不触碰 `mattmes-ui/` 下任何文件。验证目的之一是确认编排者的 `e2e.scope=changed` 能只凭后端路径把 `line` 模块认出来。
