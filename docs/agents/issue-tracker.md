# Issue tracker: Local Markdown

本仓库的需求与问题以 Markdown 文件形式存放在 `.scratch/` 下，随代码一起版本化。不使用 GitHub Issues。

## 约定

- 每个功能一个目录：`.scratch/<feature-slug>/`
- 规格文件：`.scratch/<feature-slug>/spec.md`
- 实现问题一票一文件：`.scratch/<feature-slug>/issues/<NN>-<slug>.md`，从 `01` 编号，**不要**合并成单一 tickets 文件
- 分流状态记录在每个问题文件顶部的 `Status:` 行（角色字符串见 `triage-labels.md`）
- 评论与对话历史追加到文件底部的 `## Comments` 标题下

> 历史目录中规格文件曾命名为 `PRD.md` / `prd.md`，读取时按实际存在的文件名处理；新建时统一用 `spec.md`。

## 当技能说"发布到问题追踪器"

在 `.scratch/<feature-slug>/` 下创建新文件（必要时创建目录）。

## 当技能说"获取相关 ticket"

读取引用路径的文件。用户通常会直接给出路径或问题编号。

## Wayfinding operations

供 `/wayfinder` 使用。**map** 是一个文件，每个 ticket 是一个子文件。

- **Map**：`.scratch/<effort>/map.md`，内含 Notes / Decisions-so-far / Fog 正文。
- **子 ticket**：`.scratch/<effort>/issues/NN-<slug>.md`，从 `01` 编号，正文写明该问题。`Type:` 行记录类型（`research`/`prototype`/`grilling`/`task`）；`Status:` 行记录 `claimed`/`resolved`。
- **阻塞**：文件顶部 `Blocked by: NN, NN` 行。当它列出的每个文件都是 `resolved` 时，该 ticket 解除阻塞。
- **Frontier**：扫描 `.scratch/<effort>/issues/`，找出开放、未阻塞、未认领的文件；编号最小者优先。
- **认领**：执行任何工作前，先写入 `Status: claimed` 并保存。
- **解决**：在 `## Answer` 标题下追加答案，置 `Status: resolved`，然后把一条上下文指针（要点 + 链接）追加到 `map.md` 的 Decisions-so-far。
