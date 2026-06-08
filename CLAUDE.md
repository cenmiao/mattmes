# CLAUDE.md


## 项目概述

Matt MES管理系统 - 离散制造执行系统,采用前后端分离架构:

- **后端**: Spring Boot 3.3 + MyBatis-Plus + MySQL,位于 `.mattmes/` 目录
- **前端**: Vue 3 + TypeScript + Vite + Element Plus,位于 `.mattmes-ui/` 目录

## 项目常用

### 异常处理

项目中有异常索引文件，用于记录常见或难以解决的问题，遇到难以解决的问题可以参考。
- 异常索引文件：D:\Projects\MattSkillsDemoLocal\docs\issueTracker\index.md

尝试解决同一个问题的次数不能超过3次，达到3次后仍然不能解决的，需要按照如下方式处理：
- 1：尝试在D:\Projects\MattSkillsDemoLocal\docs\issueTracker\index.md 中寻找是否有解决方案
- 2：如果找不到解决方案，则立即停止所有操作，并汇总问题询问用户

当一个多次发生的问题被成功解决后，需要按照如下步骤收录问题及解决方案：
- 1：查找同类型问题收录文档：查找问题索引文件：D:\Projects\MattSkillsDemoLocal\docs\issueTracker\index.md
- 2：如果已经有同类型文件，则根据索引文件指示，将问题和解决方案收录到对应的文件中
- 3：如果没有同类型文件，则在issueTracker中新建一个issue文件收录问题和解决方案，同时在索引文件中添加索引


### 开发
- 当用户使用tdd技能开始实施时，需要先进行tdd开发流程的评估，给用户一个评估总结，包括哪些可以tdd开发，哪些不可以tdd开发，获得用户的许可后才能开始。
- 开始tdd开发后，必须严格执行RED-GREEN-REFACTOR的流程，按照如下流程执行：
    - 1: 编写失败测试，覆盖所有预期场景
    - 2：运行失败测试
    - 3：编写最小实现，使得测试通过
    - 4：重构业务代码，消除重复、优化设计、提高可读性
    - 5：运行成功测试
- 绝对不可以：
    - 1：跳过任何步骤
    - 2：直接修改测试使测试通过
- 开发完成后需要编译对应的项目，确保项目可以成功编译


### 数据库
- url: jdbc:mysql://localhost:3306/
- account: root
- password: 123456
- database: mattmes

想要查看数据库或对数据库进行相关操作使用database-mcp-server MCP

### 运行
- 后端地址：http://localhost:8080
- 项目前端登录地址：http://localhost:3000
- 账户: admin
- 密码: Admin@123

### 自动化测试
- 测试方式1：测试后端接口； 启动后端，直接调用接口进行测试，使用永久token：eyJhbGciOiJIUzM4NCJ9.eyJ1c2VyTm8iOiJhZG1pbiIsInVzZXJJZCI6MSwiaWF0IjoxNzgwNDg2OTEyLCJleHAiOjE4MTIwMjI5MTJ9.be7sM94QmpqKrkr3iYWMRkROzaKyb-LGZNF3SW93VPzSzjEEjJy06zbOCeOjsTGK
- 测试方式2：测试前端/前后端联调；使用playwright-cli技能自动登录进行测试


## 领域知识

详见 `CONTEXT.md`,核心概念:
- **SN**: 序列号,产品唯一追溯键
- **过站**: 产品经过工序站点的记录
- **权限模型**: 扁平化 `模块:操作` 格式,无继承

## Agent skills

### Issue tracker

问题以本地 Markdown 文件形式存放在 `.scratch/<feature>/` 目录下。适合个人项目或无远程仓库的场景。详见 `docs/agents/issue-tracker.md`。

### Triage labels

使用五个规范分流标签: `needs-triage`、`needs-info`、`ready-for-agent`、`ready-for-human`、`wontfix`。详见 `docs/agents/triage-labels.md`。

### Domain docs

单上下文布局: `CONTEXT.md` 和 `docs/adr/` 位于仓库根目录。详见 `docs/agents/domain.md`。