import { describe, test } from '@e2e-dev/web';
import { expect } from 'e2e';

/**
 * 线体编码唯一键并发兜底（需求 .scratch/line-race）。
 *
 * 背景：`LineServiceImpl.add` 是「先 countByCodeIncludeDeleted 查重 → 再 insert」，
 * 两步之间没有锁。并发下后写入的一方会撞上 `mes_line.line_code` 唯一索引，
 * 抛 DuplicateKeyException；未兜底时表现为 500「系统错误」，兜底后应是
 * 与其他查重失败一致的 400「线体编码已存在」。
 *
 * 覆盖点：
 *  ① 真并发：两个同编码 POST 同时发出，只能有一个成功，另一个按 400 同一文案被拒；
 *  ② 串行重复：已存在编码再次提交仍返回 400 + 同一文案（前置查重与兜底共用语义）；
 *  ③ 已删除记录的编码仍不可复用（既有行为不得回退）；
 *  ④ 页面上用户看到的是业务提示「线体编码已存在」，弹窗保持打开，不是「请求失败」。
 */

const DUPLICATE_MESSAGE = '线体编码已存在';

function uniqueCode(prefix: string) {
  return `${prefix}_${Date.now()}`.slice(0, 50);
}

/** 取登录态 token（登录由 tests/auth.setup.e2e.ts 写入 localStorage） */
async function authToken(browser: any): Promise<string | null> {
  const token = (await browser.evaluate(() => localStorage.getItem('token'))) as string | null;
  return token || null;
}

function apiHeaders(token: string) {
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` };
}

interface AddResult {
  httpStatus: number;
  code?: number;
  message?: string;
}

/** 直接打新增接口，同时记录 HTTP 状态码与业务响应体（不依赖页面对错误的渲染） */
async function postAdd(
  browser: any,
  baseUrl: string,
  token: string,
  body: { code: string; name: string }
): Promise<AddResult> {
  const res = await fetch(new URL('/api/line/add', baseUrl), {
    method: 'POST',
    headers: apiHeaders(token),
    body: JSON.stringify(body),
  });
  const text = await res.text();
  let payload: { code?: number; message?: string } = {};
  try {
    payload = JSON.parse(text) as { code?: number; message?: string };
  } catch {
    // 非 JSON（如白页 500），保留 httpStatus 即可
  }
  return { httpStatus: res.status, code: payload.code, message: payload.message };
}

/** 用登录态接口查出该编码的线体 id（收尾删除用） */
async function apiFindIds(browser: any, baseUrl: string, code: string): Promise<number[]> {
  const token = await authToken(browser);
  if (!token) return [];
  const res = await fetch(new URL('/api/line/list', baseUrl), {
    method: 'POST',
    headers: apiHeaders(token),
    body: JSON.stringify({ code, pageNum: 1, pageSize: 100 }),
  });
  if (!res.ok) return [];
  const payload = (await res.json()) as { data?: { list?: Array<{ id: number }> } };
  return (payload.data?.list ?? []).map((row) => row.id);
}

/** 逻辑删除指定编码的线体（测试数据收尾，不属于断言逻辑） */
async function apiCleanup(browser: any, baseUrl: string | undefined, code: string) {
  if (!baseUrl) return;
  const ids = await apiFindIds(browser, baseUrl, code);
  if (ids.length === 0) return;
  const token = await authToken(browser);
  if (!token) return;
  await fetch(new URL('/api/line/batchDelete', baseUrl), {
    method: 'DELETE',
    headers: apiHeaders(token),
    body: JSON.stringify(ids),
  });
}

function addDialog(screen: any) {
  return screen.getByRole('dialog', '新增线体');
}

function rowOf(screen: any, code: string) {
  return screen.getByRole('row').filter({ hasText: code });
}

/** 在已打开的新增弹窗里填入编码/名称并提交（结果由调用方断言） */
async function submitAddDialog(screen: any, code: string, name: string) {
  const dialog = addDialog(screen);
  await expect(dialog).toBeVisible();
  await dialog.getByRole('textbox', { name: /线体编码/ }).fill(code);
  await dialog.getByRole('textbox', { name: /线体名称/ }).fill(name);
  await dialog.getByRole('button', '确定').tap();
}

/** 点开新增弹窗并提交一条线体 */
async function addViaDialog(screen: any, code: string, name: string) {
  await screen.getByRole('button', '新增').tap();
  await submitAddDialog(screen, code, name);
}

function requireToken(token: string | null): string {
  if (!token) throw new Error('登录态缺失：tests/auth.setup.e2e.ts 未写入 token');
  return token;
}

describe('线体编码唯一键兜底', () => {
  test('并发：两个同编码请求同时提交，只成功一条，另一条返回 400 业务文案', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_UQ_CONC');
    const baseUrl = app.baseUrl!;
    try {
      await app.open('/#/lines');
      const token = requireToken(await authToken(browser));

      // 两个请求同批次发出：都在前置查重之后才 insert，后到的一方撞唯一键
      const results = await Promise.all([
        postAdd(browser, baseUrl, token, { code, name: `E2E并发甲_${code}` }),
        postAdd(browser, baseUrl, token, { code, name: `E2E并发乙_${code}` }),
      ]);

      // 断言只看「出问题的那一方」：成功方数量 ≤1 属于并发调度噪声（真正串行时后者走前置查重），
      // 唯一不可接受的是落库失败的那一方被冒泡成 500。
      const okCount = results.filter((r) => r.code === 200).length;
      const rejected = results.filter((r) => r.code !== 200);

      expect(okCount).toBeGreaterThanOrEqual(1);
      expect(rejected).toHaveLength(2 - okCount);
      for (const r of rejected) {
        expect(r.httpStatus).toBe(200);
        expect(r.code).toBe(400);
        expect(r.message).toBe(DUPLICATE_MESSAGE);
      }

      // 库里确实只有一条
      await screen.getByRole('textbox', '线体编码').fill(code);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, code)).toHaveCount(1);
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('已存在编码：再次提交返回 400 与同一文案，页面弹窗保持打开', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_UQ_DUP');
    const name = `E2E重复_${code}`;
    try {
      await app.open('/#/lines');
      await addViaDialog(screen, code, name);
      await expect(screen.getByText('新增线体成功').last()).toBeVisible();
      await expect(addDialog(screen)).toBeHidden();

      const token = requireToken(await authToken(browser));
      const dup = await postAdd(browser, app.baseUrl!, token, { code, name: `${name}_again` });
      expect(dup.code).toBe(400);
      expect(dup.message).toBe(DUPLICATE_MESSAGE);

      // 页面侧：同一编码再走一次 UI，用户看到业务提示而不是「请求失败」
      await screen.getByRole('button', '新增').tap();
      await submitAddDialog(screen, code, `${name}_ui`);
      // 提示条挂在 body 上、不在 dialog 子树里，必须从根 screen 取
      await expect(screen.getByText(DUPLICATE_MESSAGE).last()).toBeVisible();
      await expect(screen.getByText('请求失败')).toHaveCount(0);
      await expect(addDialog(screen)).toBeVisible();
      await addDialog(screen).getByRole('button', '取消').tap();
      await expect(addDialog(screen)).toBeHidden();

      await screen.getByRole('textbox', '线体编码').fill(code);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, code)).toHaveCount(1);
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('已删除编码不可复用：删除后同编码仍被 400 拦截', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_UQ_DEL');
    const name = `E2E删后重建_${code}`;
    try {
      await app.open('/#/lines');
      await addViaDialog(screen, code, name);
      await expect(screen.getByText('新增线体成功').last()).toBeVisible();
      await expect(addDialog(screen)).toBeHidden();

      await screen.getByRole('textbox', '线体编码').fill(code);
      await screen.getByRole('button', '查询').tap();
      await rowOf(screen, code).getByRole('button', '删除').tap();
      const confirm = screen.getByRole('dialog', '删除确认');
      await confirm.getByRole('button', '确定').tap();
      await expect(screen.getByText('删除线体成功')).toBeVisible();
      await expect(rowOf(screen, code)).toHaveCount(0);

      const token = requireToken(await authToken(browser));
      const reuse = await postAdd(browser, app.baseUrl!, token, { code, name: `${name}_re` });
      // 已删除记录的编码仍占用唯一键：不得成功，也不得是 500
      expect(reuse.httpStatus).toBe(200);
      expect(reuse.code).toBe(400);
      expect(reuse.message).toBe(DUPLICATE_MESSAGE);
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });
});
