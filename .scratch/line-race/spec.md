# 线体编码查重的并发兜底

Status: ready-for-agent

## Problem Statement

线体新增时，`LineServiceImpl.add` 的流程是「先 `countByCodeIncludeDeleted` 查重 → 再 `insert`」。这两步之间没有事务级锁定，也没有捕获唯一键冲突：

```java
Long count = lineMapper.countByCodeIncludeDeleted(code);
if (count > 0) { throw new BusinessException(400, "线体编码已存在"); }
// ← 两个并发请求都可能走到这里
lineMapper.insert(entity);
```

并发场景下两个请求可以同时通过查重，随后第二个 `insert` 撞上 `line_code` 的唯一索引，抛出 `DuplicateKeyException`。该异常没有任何业务语义包装，最终以 500 返回给前端——用户看到的是"服务器错误"，而不是"编码已存在"。

这不是理论风险：`mes_line.line_code` 上有唯一索引（`V4__create_line_table.sql`），只要两个管理员同时提交同一个编码就会触发。

## Solution

在 `LineServiceImpl.add` 的 `insert` 调用处捕获唯一键冲突，翻译成与其他查重失败一致的业务错误：

- 捕获 `DuplicateKeyException`（`org.springframework.dao.DuplicateKeyException`）
- 抛 `BusinessException(400, "线体编码已存在")`，与前置查重走同一个错误码与文案
- 前置 `countByCodeIncludeDeleted` 保留不动——它负责常见路径的友好提示，唯一键兜底负责并发窗口

**不改表结构、不改接口签名、不改前端。**

## 验收范围

- 并发（或串行的"绕过前置查重"路径）下重复编码返回 400 而非 500
- 已删除记录的编码仍不可复用（既有行为不得回退）
- 既有 `LineControllerTest` 全部通过

## 不在范围

- 不加分布式锁、不引入 Redis
- 不改前端任何文件
- 不改 `mes_line` 表结构或索引
