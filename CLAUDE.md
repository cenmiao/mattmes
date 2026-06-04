# CLAUDE.md


## 项目概述

Matt MES管理系统 - 离散制造执行系统,采用前后端分离架构:

- **后端**: Spring Boot 3.3 + MyBatis-Plus + MySQL,位于 `.mattmes/` 目录
- **前端**: Vue 3 + TypeScript + Vite + Element Plus,位于 `.mattmes-ui/` 目录

## 项目常用

### 开发
- 当用户使用tdd技能开始实施时，需要先进行tdd开发流程的评估，给用户一个评估总结，包括哪些可以tdd开发，哪些不可以tdd开发，获得用户的许可后才能开始。
- 开始tdd开发后，必须严格执行RED-GREEN-REFACTOR的流程，按照如下流程执行：
    - 1: 编写失败测试
    - 2：运行失败测试
    - 3：重构业务代码
    - 4：运行成功测试
- 绝对不可以：
    - 1：跳过任何步骤
    - 2：直接修改测试使测试通过
- 开发完成后需要编译对应的项目，确保项目可以成功编译


### 数据库
- url: jdbc:mysql://localhost:3306/
- account: root
- password: 123456
- database: mattmes

数据库相关操作使用database-mcp-server MCP

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