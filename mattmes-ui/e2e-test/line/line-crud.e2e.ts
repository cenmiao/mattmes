import { test, describe } from '@e2e-dev/web';
import { expect } from 'e2e';

/**
 * 线体管理列表页（#/lines）端到端用例：菜单入口 / 页面结构 / 新增（必填、格式、默认启用、唯一性）/
 * 编辑（编码只读）/ 状态切换 / 单条与批量删除 / 查询与重置。
 *
 * 数据策略：每条用例用时间戳生成唯一线体编码，用例结束（含失败）通过登录态接口逻辑删除，
 * 重跑与失败重试都不受残留数据影响。
 */

const stamp = () => `${Date.now()}`;

function uniqueCode(prefix: string) {
  return `${prefix}_${stamp()}`.slice(0, 50);
}

/** 取登录态 token（登录由 auth.setup.e2e.ts 写入 localStorage） */
async function authToken(browser: any): Promise<string | null> {
  const token = (await browser.evaluate(() => localStorage.getItem('token'))) as string | null;
  return token || null;
}

function apiHeaders(token: string) {
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` };
}

/** 用登录态接口查出该编码的所有线体，返回 id 列表（测试数据收尾用） */
async function apiFindIds(browser: any, baseUrl: string | undefined, code: string): Promise<number[]> {
  if (!baseUrl) return [];
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
  const ids = await apiFindIds(browser, baseUrl, code);
  if (ids.length === 0 || !baseUrl) return;
  const token = await authToken(browser);
  if (!token) return;
  await fetch(new URL('/api/line/batchDelete', baseUrl), {
    method: 'DELETE',
    headers: apiHeaders(token),
    body: JSON.stringify(ids),
  });
}

function rowOf(screen: any, code: string) {
  return screen.getByRole('row').filter({ hasText: code });
}

function addDialog(screen: any) {
  return screen.getByRole('dialog', '新增线体');
}

function editDialog(screen: any) {
  return screen.getByRole('dialog', '编辑线体');
}

/** 打开 el-select 下拉面板并选中一项；面板挂在 body 层，option 从根 screen 取 */
async function pickOption(screen: any, scope: any, comboboxName: RegExp | string, option: string) {
  await scope.getByRole('combobox', comboboxName).press('Enter');
  await screen.getByRole('option', option).tap();
}

/**
 * 选中 el-radio。可访问节点是隐藏 input，`.el-radio__inner` 会拦截指针事件，
 * 因此点击落在 radio 的视觉外壳上。
 */
async function pickRadio(browser: any, dialogTitle: string, label: string) {
  await browser.locator(`.el-dialog:has-text("${dialogTitle}") .el-radio:has-text("${label}")`).click();
}

/** 通过 UI 新增一条线体，并等待成功提示 */
async function createViaUi(
  screen: any,
  browser: any,
  data: { code: string; name: string; description?: string; disabled?: boolean }
) {
  await screen.getByRole('button', '新增').tap();
  const dialog = addDialog(screen);
  await expect(dialog).toBeVisible();

  await dialog.getByRole('textbox', { name: /线体编码/ }).fill(data.code);
  await dialog.getByRole('textbox', { name: /线体名称/ }).fill(data.name);
  if (data.disabled) {
    await pickRadio(browser, '新增线体', '禁用');
  }
  if (data.description) {
    await dialog.getByRole('textbox', '描述').fill(data.description);
  }

  await dialog.getByRole('button', '确定').tap();
  await expect(screen.getByText('新增线体成功').last()).toBeVisible();
  await expect(addDialog(screen)).toBeHidden();
}

/** 按编码收敛列表 */
async function filterByCode(screen: any, code: string) {
  await screen.getByRole('textbox', '线体编码').fill(code);
  await screen.getByRole('button', '查询').tap();
}

describe('线体管理', () => {
  test('菜单入口：侧边栏「线体管理」可进入 /#/lines', { session: 'admin' }, async ({ app, screen, browser }) => {
    await app.open('/#/dashboard');
    await screen.getByRole('menuitem', '线体管理').tap();

    await expect(browser).toHaveURL(/#\/lines$/);
    await expect(screen.getByRole('textbox', '线体编码')).toBeVisible();
  });

  test('页面结构：查询条件、操作按钮、表格列齐全', { session: 'admin' }, async ({ app, screen }) => {
    await app.open('/#/lines');

    await expect(screen.getByRole('textbox', '线体编码')).toBeVisible();
    await expect(screen.getByRole('textbox', '线体名称')).toBeVisible();
    await expect(screen.getByRole('combobox', '启用状态')).toBeVisible();

    for (const label of ['查询', '重置', '新增', '批量删除', '导出']) {
      await expect(screen.getByRole('button', label)).toBeVisible();
    }
    // 未勾选任何线体时批量删除禁用
    await expect(screen.getByRole('button', '批量删除')).toBeDisabled();

    for (const header of ['ID', '线体编码', '线体名称', '描述', '启用状态', '创建人', '创建时间', '操作']) {
      await expect(screen.getByRole('columnheader', header)).toBeVisible();
    }
    // 线体本期不建备注列
    await expect(screen.getByRole('columnheader', '备注')).toHaveCount(0);
  });

  test('新增：必填与编码格式被拦截，填写后写入列表且默认启用', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_ADD');
    const name = `E2E新增_${code}`;
    try {
      await app.open('/#/lines');
      await screen.getByRole('button', '新增').tap();

      const dialog = addDialog(screen);
      await expect(screen.getByRole('heading', '新增线体')).toBeVisible();

      // 空表单提交：必填提示出现，弹窗不关闭
      await dialog.getByRole('button', '确定').tap();
      await expect(dialog).toBeVisible();
      await expect(dialog.getByText('请输入线体编码').last()).toBeVisible();
      await expect(dialog.getByText('请输入线体名称').last()).toBeVisible();

      // 编码格式校验：非法字符被拦
      await dialog.getByRole('textbox', { name: /线体编码/ }).fill('非法 编码!');
      await dialog.getByRole('textbox', { name: /线体名称/ }).focus();
      await expect(dialog.getByText('线体编码只能包含字母、数字、下划线和中划线').last()).toBeVisible();

      // 合规编码写入后进入列表
      await dialog.getByRole('textbox', { name: /线体编码/ }).fill(code);
      await dialog.getByRole('textbox', { name: /线体名称/ }).fill(name);
      await dialog.getByRole('textbox', '描述').fill('E2E 新增描述');
      await dialog.getByRole('button', '确定').tap();

      await expect(screen.getByText('新增线体成功')).toBeVisible();
      await expect(addDialog(screen)).toBeHidden();

      await filterByCode(screen, code);
      const row = rowOf(screen, code);
      await expect(row).toBeVisible();
      await expect(row).toContainText(name);
      await expect(row).toContainText('E2E 新增描述');
      // 不填状态时默认启用
      await expect(row.getByRole('switch')).toBeChecked();
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('唯一性：编码重复被拦截，删除后同编码重建仍提示已存在', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_DUP');
    const name = `E2E重复_${code}`;
    try {
      await app.open('/#/lines');
      await createViaUi(screen, browser, { code, name });

      // 用同编码再建一次：弹窗保持打开，提示编码已存在
      await screen.getByRole('button', '新增').tap();
      const dialog = addDialog(screen);
      await dialog.getByRole('textbox', { name: /线体编码/ }).fill(code);
      await dialog.getByRole('textbox', { name: /线体名称/ }).fill(`${name}_dup`);
      await dialog.getByRole('button', '确定').tap();
      await expect(screen.getByText('线体编码已存在').last()).toBeVisible();
      await expect(dialog).toBeVisible();
      await dialog.getByRole('button', '取消').tap();
      await expect(addDialog(screen)).toBeHidden();

      await filterByCode(screen, code);
      await expect(rowOf(screen, code)).toHaveCount(1);

      // 删除该线体后，用同编码重建仍应被提示"已存在"（唯一性连同已删除记录一起查重）
      await rowOf(screen, code).getByRole('button', '删除').tap();
      const confirm = screen.getByRole('dialog', '删除确认');
      await confirm.getByRole('button', '确定').tap();
      await expect(screen.getByText('删除线体成功')).toBeVisible();
      await expect(rowOf(screen, code)).toHaveCount(0);

      await screen.getByRole('button', '新增').tap();
      const dialog2 = addDialog(screen);
      await dialog2.getByRole('textbox', { name: /线体编码/ }).fill(code);
      await dialog2.getByRole('textbox', { name: /线体名称/ }).fill(`${name}_re`);
      await dialog2.getByRole('button', '确定').tap();
      await expect(screen.getByText('线体编码已存在').last()).toBeVisible();
      await expect(dialog2).toBeVisible();
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('编辑：名称/描述/状态可改，编码只读', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_EDIT');
    try {
      await app.open('/#/lines');
      await createViaUi(screen, browser, { code, name: `E2E编辑前_${code}`, description: '编辑前描述' });
      await filterByCode(screen, code);

      await rowOf(screen, code).getByRole('button', '编辑').tap();
      const dialog = editDialog(screen);
      await expect(dialog).toBeVisible();
      // 编码是业务唯一标识，编辑时只读
      const codeInput = dialog.getByRole('textbox', { name: /线体编码/ });
      await expect(codeInput).toBeDisabled();
      await expect(codeInput).toHaveValue(code);

      const newName = `E2E编辑后_${code}`;
      await dialog.getByRole('textbox', { name: /线体名称/ }).fill(newName);
      await dialog.getByRole('textbox', '描述').fill('编辑后描述');
      await pickRadio(browser, '编辑线体', '禁用');
      await dialog.getByRole('button', '确定').tap();

      await expect(screen.getByText('编辑线体成功')).toBeVisible();
      await expect(editDialog(screen)).toBeHidden();

      await filterByCode(screen, code);
      const row = rowOf(screen, code);
      await expect(row).toContainText(newName);
      await expect(row).toContainText('编辑后描述');
      // 编码未被改动
      await expect(row).toContainText(code);
      await expect(row.getByRole('switch')).not.toBeChecked();
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('状态切换：启用改禁用，重新查询后仍是禁用', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_STAT');
    try {
      await app.open('/#/lines');
      await createViaUi(screen, browser, { code, name: `E2E状态_${code}` });
      await filterByCode(screen, code);

      const row = rowOf(screen, code);
      // el-switch 的可访问节点是隐藏 input，点击落在它的视觉外壳上
      const switchShell = browser.locator(`.el-table__row:has-text("${code}") .el-switch`);
      await expect(row.getByRole('switch')).toBeChecked();

      await switchShell.click();
      await expect(screen.getByText('状态更新成功')).toBeVisible();
      await expect(row.getByRole('switch')).not.toBeChecked();

      // 重新查询，确认状态已落库
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, code).getByRole('switch')).not.toBeChecked();
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('删除：确认弹窗显示线体名称，确认后该行消失', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_DEL');
    const name = `E2E删除_${code}`;
    try {
      await app.open('/#/lines');
      await createViaUi(screen, browser, { code, name });
      await filterByCode(screen, code);
      await expect(rowOf(screen, code)).toBeVisible();

      await rowOf(screen, code).getByRole('button', '删除').tap();
      const confirm = screen.getByRole('dialog', '删除确认');
      await expect(confirm).toBeVisible();
      await expect(confirm).toContainText(name);
      await confirm.getByRole('button', '确定').tap();

      await expect(screen.getByText('删除线体成功')).toBeVisible();
      await expect(rowOf(screen, code)).toHaveCount(0);
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('批量删除：未勾选时禁用，勾选两条后确认框显示条数并一次删除', { session: 'admin' }, async ({ app, screen, browser }) => {
    const codeA = uniqueCode('E2E_BAT_A');
    const codeB = uniqueCode('E2E_BAT_B');
    try {
      await app.open('/#/lines');
      await createViaUi(screen, browser, { code: codeA, name: `E2E批量_${codeA}` });
      await createViaUi(screen, browser, { code: codeB, name: `E2E批量_${codeB}` });

      // 用共用时间戳段收敛列表，避免误勾历史数据
      const stampPart = codeA.slice(codeA.lastIndexOf('_') + 1);
      await filterByCode(screen, stampPart);
      await expect(rowOf(screen, codeA)).toHaveCount(1);
      await expect(rowOf(screen, codeB)).toHaveCount(1);
      await expect(screen.getByRole('button', '批量删除')).toBeDisabled();

      // el-checkbox 的可访问节点是隐藏 input，点击落在它的视觉外壳上
      await browser.locator(`.el-table__row:has-text("${codeA}") .el-checkbox`).click();
      await expect(screen.getByRole('button', '批量删除')).toBeEnabled();
      await browser.locator(`.el-table__row:has-text("${codeB}") .el-checkbox`).click();
      await expect(screen.getByRole('button', '批量删除')).toBeEnabled();

      await screen.getByRole('button', '批量删除').tap();
      const confirm = screen.getByRole('dialog', '批量删除确认');
      await expect(confirm).toContainText('2 条');
      await confirm.getByRole('button', '确定').tap();

      await expect(screen.getByText('成功删除 2 条线体')).toBeVisible();
      await expect(rowOf(screen, codeA)).toHaveCount(0);
      await expect(rowOf(screen, codeB)).toHaveCount(0);
    } finally {
      await apiCleanup(browser, app.baseUrl, codeA);
      await apiCleanup(browser, app.baseUrl, codeB);
    }
  });

  test('查询：按编码、名称、状态过滤并组合，重置后条件清空', { session: 'admin' }, async ({ app, screen, browser }) => {
    const codeA = uniqueCode('E2E_QRY_A');
    const codeB = uniqueCode('E2E_QRY_B');
    const nameA = `E2E查询甲_${codeA}`;
    const nameB = `E2E查询乙_${codeB}`;
    try {
      await app.open('/#/lines');
      await createViaUi(screen, browser, { code: codeA, name: nameA });
      await createViaUi(screen, browser, { code: codeB, name: nameB, disabled: true });

      // ① 按编码模糊查询，只留 A
      await filterByCode(screen, codeA);
      await expect(rowOf(screen, codeA)).toBeVisible();
      await expect(rowOf(screen, codeB)).toHaveCount(0);

      // ② 清编码，按名称模糊查询，只留 B
      await screen.getByRole('textbox', '线体编码').fill('');
      await screen.getByRole('textbox', '线体名称').fill(nameB);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeB)).toBeVisible();
      await expect(rowOf(screen, codeA)).toHaveCount(0);

      // ③ 按状态筛选：禁用只剩 B
      await screen.getByRole('textbox', '线体名称').fill('');
      await pickOption(screen, screen, '启用状态', '禁用');
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeB)).toBeVisible();
      await expect(rowOf(screen, codeA)).toHaveCount(0);

      // ④ 启用只剩 A
      await pickOption(screen, screen, '启用状态', '启用');
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeA)).toBeVisible();
      await expect(rowOf(screen, codeB)).toHaveCount(0);

      // ⑤ 组合条件：编码命中 A + 名称命中 B → 无结果
      await screen.getByRole('textbox', '线体编码').fill(codeA);
      await screen.getByRole('textbox', '线体名称').fill(nameB);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeA)).toHaveCount(0);
      await expect(rowOf(screen, codeB)).toHaveCount(0);

      // ⑥ 重置：三个条件清空，两条都回来
      await screen.getByRole('button', '重置').tap();
      await expect(screen.getByRole('textbox', '线体编码')).toHaveValue('');
      await expect(screen.getByRole('textbox', '线体名称')).toHaveValue('');
      await expect(rowOf(screen, codeA)).toBeVisible();
      await expect(rowOf(screen, codeB)).toBeVisible();
    } finally {
      await apiCleanup(browser, app.baseUrl, codeA);
      await apiCleanup(browser, app.baseUrl, codeB);
    }
  });
});
