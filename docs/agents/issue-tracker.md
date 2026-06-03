# Issue tracker: Local Markdown

问题以本地 Markdown 文件形式存放在 `.scratch/` 目录下,适合个人项目或无远程仓库的场景。

## 约定

- 每个功能一个目录: `.scratch/<feature-slug>/`
- PRD 文件: `.scratch/<feature-slug>/PRD.md`
- 实现问题: `.scratch/<feature-slug>/issues/<NN>-<slug>.md`,从 `01` 开始编号
- 分流状态记录在每个问题文件顶部的 `Status:` 行(标签字符串见 `triage-labels.md`)
- 评论和对话历史追加到文件底部的 `## Comments` 段落下

## 当技能说"发布到问题追踪器"

在 `.scratch/<feature-slug>/` 下创建新文件(如需要则创建目录)。

## 当技能说"获取相关 ticket"

读取引用路径的文件。用户通常会直接传递路径或问题编号。