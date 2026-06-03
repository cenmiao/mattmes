---
name: complete-issue
description: 将 issue 文件标记为完成状态，更新所有验收标准为已完成。Use when user wants to mark an issue as done, complete an issue, or update issue status to finished.
---

# Complete Issue

将 issue 文件从 `ready-for-agent` 状态更新为 `done` 状态。

## Quick start

```
/complete-issue <issue-file-path>
```

## Workflows

### 步骤清单

1. **读取 issue 文件**
   - 读取指定的 issue 文件内容

2. **更新 YAML 头部状态**
   ```yaml
   # 从
   ---
   status: ready-for-agent
   ---

   # 改为
   ---
   status: done
   completed: 2026-05-31  # 当前日期
   ---
   ```

3. **勾选所有验收标准**
   - 在 `## Acceptance criteria` 部分
   - 将所有 `- [ ]` 改为 `- [x]`

4. **添加完成记录**
   - 在 `## Comments` 部分之前添加 `## Completion Notes` 部分
   - 记录完成日期、状态变更、创建的文件列表

5. **如果存在 Agent Brief 部分**
   - 同样勾选该部分的验收标准

### 完成记录模板

```markdown
---

## Completion Notes

**2026-05-31 完成记录:**

- 状态: `ready-for-agent` → `done`
- 实现方式: [简要说明]

**创建的文件:**
1. `path/to/file1.java`
2. `path/to/file2.java`

**待后续处理:**
- [如有未完成项，列出]

---

## Comments
```

## File location

Issue 文件位于: `.scratch/<feature>/issues/<issue-file>.md`

## Example

用户请求: "完成 05-password-change 这个 issue"

执行步骤:
1. 读取 `.scratch/login-module/issues/05-password-change.md`
2. 更新 status 为 done
3. 勾选所有 `- [ ]` 为 `- [x]`
4. 添加完成记录
