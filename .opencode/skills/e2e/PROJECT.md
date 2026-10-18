# mattmes 项目适配

本文件补充 e2e 技能在本仓库的具体用法，权威内容仍是 `SKILL.md` 与
`mattmes-ui/node_modules/e2e/docs/`。

## e2e 项目根目录

e2e 的 project root 是 **`mattmes-ui/`**，不是仓库根目录：

- 配置文件：`mattmes-ui/e2e.config.ts`
- 用例目录：`mattmes-ui/tests/**/*.e2e.ts`
- 输出目录：`mattmes-ui/.e2e/`（已 gitignore）
- 依赖：`mattmes-ui/node_modules`（`e2e`、`@e2e-dev/web`）

因为没有根级 `package.json`，所有 e2e 命令都要在 `mattmes-ui` 下执行：

```bash
cd mattmes-ui
npx e2e list
npx e2e run tests/example.e2e.ts
npx e2e run --headed
npm run test:e2e
```

## 被测环境

- 前端：`http://localhost:3000`（Vite，`npm run dev`）
- 后端：`http://localhost:8080`（Spring Boot）
- 前端 `/api` 代理到后端，测试业务功能时两者都要先起
- 配置里用 `APP_URL` 覆盖地址，默认 `http://localhost:3000`
- 配置没有声明 `app.command`，所以运行测试或开 MCP session 之前，**先手动启动前端**；
  否则报 `APP_UNREACHABLE`

## 登录

`e2e.config.ts` 声明了 `admin` 凭据（用户名默认 `admin`，密码从环境变量读）：

```bash
set E2E_ADMIN_PASSWORD=Admin@123    # PowerShell: $env:E2E_ADMIN_PASSWORD='Admin@123'
```

用例中这样用，密码不会进入模型输入：

```ts
import { credentials } from 'e2e';
const admin = credentials.user('admin');
await screen.getByLabel('用户名').fill(admin.username);
await screen.getByLabel('密码').fill(admin.password);
```

MCP session 里用 `type_secret` 填同名凭据，不要把明文贴给模型。

## 与 playwright-cli 的关系

`playwright-cli` 技能仍是临场手工验证的主要方式；e2e 用于**可回归的端到端用例**
和 `e2e mcp` 实时驱动的页面检查。两者不要同时抢同一个浏览器上下文。

## 需要的浏览器

首次运行会下载 Chromium：

```bash
cd mattmes-ui
npx e2e run tests/example.e2e.ts
```

离线环境可以用 `--headed` 复用本机 Chrome 前，先确认 `@e2e-dev/web` 已装好浏览器。

## 更新技能副本

`SKILL.md`/`references/` 是从 `mattmes-ui/node_modules/e2e/skills/e2e` 复制过来的。
升级 `e2e` 依赖后同步一次：

```powershell
Copy-Item -Recurse -Force `
  "D:\Projects\mattmes\mattmes-ui\node_modules\e2e\skills\e2e\*" `
  "D:\Projects\mattmes\.opencode\skills\e2e"
```

不确定时优先用 `npx e2e guide <topic>`，它打印的是当前安装版本的文档。
