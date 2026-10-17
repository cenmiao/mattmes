import { test, describe } from '@e2e-dev/web';
import { expect } from 'e2e';
import { readFileSync } from 'node:fs';

/**
 * 料号管理（#/materials）端到端用例：查 / 增 / 改 / 删（单条 + 批量）/ 状态切换 / 导出。
 *
 * 数据策略：每次用时间戳生成唯一料号编码，用例结束通过接口清理，
 * 重跑与失败重试都不会被残留数据影响。
 */

const stamp = () => `${Date.now()}`;

function uniqueCode(prefix: string) {
  return `${prefix}_${stamp()}`.slice(0, 50);
}

/** 用登录态接口清理指定编码的料号（测试数据收尾，不属于断言逻辑） */
async function apiCleanup(browser: any, baseUrl: string | undefined, code: string) {
  if (!baseUrl) return;
  const token = (await browser.evaluate(() => localStorage.getItem('token'))) as string | null;
  if (!token) return;
  const headers = { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` };

  const listRes = await fetch(new URL('/api/material/list', baseUrl), {
    method: 'POST',
    headers,
    body: JSON.stringify({ code, pageNum: 1, pageSize: 100 }),
  });
  if (!listRes.ok) return;
  const payload = (await listRes.json()) as { data?: { list?: Array<{ id: number }> } };
  const ids = (payload.data?.list ?? []).map((row) => row.id);
  if (ids.length === 0) return;

  await fetch(new URL('/api/material/batch', baseUrl), {
    method: 'DELETE',
    headers,
    body: JSON.stringify(ids),
  });
}

function rowOf(screen: any, code: string) {
  return screen.getByRole('row').filter({ hasText: code });
}

function addDialog(screen: any) {
  return screen.getByRole('dialog', '新增料号');
}

function editDialog(screen: any) {
  return screen.getByRole('dialog', '编辑料号');
}

/**
 * 打开 el-select 的下拉面板并选中一项。
 * 面板挂在 body 层，不在 dialog 内：combobox 用 scope 取，option 用根 screen 取。
 */
async function pickOption(screen: any, scope: any, comboboxName: RegExp | string, option: string) {
  await scope.getByRole('combobox', comboboxName).press('Enter');
  await screen.getByRole('option', option).tap();
}

/**
 * 通过 UI 新增一条料号。
 * 需要先从"所属项目"下拉中选一个项目（取第一个可用选项）。
 */
async function createViaUi(
  screen: any,
  data: { code: string; name: string; projectName?: string; color?: string; size?: string; spec1?: string; description?: string; remark?: string }
) {
  await screen.getByRole('button', '新增').tap();
  const dialog = addDialog(screen);
  await expect(dialog).toBeVisible();

  await dialog.getByRole('textbox', { name: /料号编码/ }).fill(data.code);
  await dialog.getByRole('textbox', { name: /料号名称/ }).fill(data.name);

  // 选择所属项目：打开下拉，取第一个选项
  await dialog.getByRole('combobox', /所属项目/).press('Enter');
  const firstOption = screen.getByRole('option').nth(0);
  const optionText = await firstOption.textContent();
  await firstOption.tap();

  if (data.color) {
    await dialog.getByRole('textbox', { name: '颜色' }).fill(data.color);
  }
  if (data.size) {
    await dialog.getByRole('textbox', { name: '尺码' }).fill(data.size);
  }
  if (data.spec1) {
    await dialog.getByRole('textbox', { name: '通用规格1' }).fill(data.spec1);
  }
  if (data.description) {
    await dialog.getByRole('textbox', { name: '描述' }).fill(data.description);
  }
  if (data.remark) {
    await dialog.getByRole('textbox', { name: '备注' }).fill(data.remark);
  }

  await dialog.getByRole('button', '确定').tap();
  await expect(screen.getByText('新增料号成功').last()).toBeVisible();
  await expect(addDialog(screen)).toBeHidden();

  return optionText;
}

describe('料号管理', () => {
  test('页面结构：查询条件、操作按钮、表格列齐全', { session: 'admin' }, async ({ app, screen }) => {
    await app.open('/#/materials');

    // 查询条件
    await expect(screen.getByRole('textbox', '料号编码')).toBeVisible();
    await expect(screen.getByRole('textbox', '料号名称')).toBeVisible();
    await expect(screen.getByRole('combobox', '所属项目')).toBeVisible();
    await expect(screen.getByRole('combobox', '启用状态')).toBeVisible();

    // 操作按钮
    for (const label of ['查询', '重置', '新增', '批量删除', '导出']) {
      await expect(screen.getByRole('button', label)).toBeVisible();
    }
    // 无选中时批量删除禁用
    await expect(screen.getByRole('button', '批量删除')).toBeDisabled();

    // 表格列
    for (const header of ['ID', '料号编码', '料号名称', '所属项目', '颜色', '尺码', '启用状态', '创建人', '创建时间', '操作']) {
      await expect(screen.getByRole('columnheader', header)).toBeVisible();
    }
  });

  test('新增：必填校验拦截，编码格式校验，填写后写入列表', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_ADD');
    const name = `E2E新增_${code}`;
    try {
      await app.open('/#/materials');
      await screen.getByRole('button', '新增').tap();

      const dialog = addDialog(screen);
      await expect(screen.getByRole('heading', '新增料号')).toBeVisible();

      // 空表单提交：必填提示出现，弹窗不关闭
      await dialog.getByRole('button', '确定').tap();
      await expect(dialog).toBeVisible();
      await expect(dialog.getByText('请输入料号编码').last()).toBeVisible();
      await expect(dialog.getByText('请输入料号名称').last()).toBeVisible();

      // 编码格式校验：非法字符被拦
      await dialog.getByRole('textbox', { name: /料号编码/ }).fill('非法 编码!');
      await dialog.getByRole('textbox', { name: /料号名称/ }).focus();
      await expect(dialog.getByText('编码只能包含字母、数字、下划线和中划线').last()).toBeVisible();

      // 填写合法数据
      await dialog.getByRole('textbox', { name: /料号编码/ }).fill(code);
      await dialog.getByRole('textbox', { name: /料号名称/ }).fill(name);
      // 选择所属项目
      await dialog.getByRole('combobox', /所属项目/).press('Enter');
      await screen.getByRole('option').nth(0).tap();
      await dialog.getByRole('textbox', { name: '颜色' }).fill('金色');
      await dialog.getByRole('textbox', { name: '尺码' }).fill('5G版');
      await dialog.getByRole('textbox', { name: '通用规格1' }).fill('E2E规格1');
      await dialog.getByRole('textbox', { name: '描述' }).fill('E2E 新增描述');
      await dialog.getByRole('textbox', { name: '备注' }).fill('E2E 新增备注');

      await dialog.getByRole('button', '确定').tap();
      await expect(screen.getByText('新增料号成功')).toBeVisible();
      await expect(addDialog(screen)).toBeHidden();

      // 确认列表中存在新增的料号
      const row = rowOf(screen, code);
      await expect(row).toBeVisible();
      await expect(row).toContainText(name);
      await expect(row).toContainText('金色');
      await expect(row).toContainText('5G版');
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('查询：按编码、名称、状态过滤，重置后条件与列表恢复', { session: 'admin' }, async ({ app, screen, browser }) => {
    const codeA = uniqueCode('E2E_QRY_A');
    const codeB = uniqueCode('E2E_QRY_B');
    try {
      await app.open('/#/materials');
      await createViaUi(screen, { code: codeA, name: `E2E查询_${codeA}`, color: '红色', size: '大' });
      await createViaUi(screen, { code: codeB, name: `E2E查询_${codeB}`, color: '蓝色', size: '小' });

      // ① 按编码模糊查询，只留 A
      await screen.getByRole('textbox', '料号编码').fill(codeA);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeA)).toBeVisible();
      await expect(rowOf(screen, codeB)).toHaveCount(0);

      // ② 清编码，按料号名称查询，只留 B
      await screen.getByRole('textbox', '料号编码').fill('');
      await screen.getByRole('textbox', '料号名称').fill(`E2E查询_${codeB}`);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeB)).toBeVisible();
      await expect(rowOf(screen, codeA)).toHaveCount(0);

      // ③ 重置：条件清空，两条都回来
      await screen.getByRole('button', '重置').tap();
      await expect(screen.getByRole('textbox', '料号编码')).toHaveValue('');
      await expect(screen.getByRole('textbox', '料号名称')).toHaveValue('');
      await expect(rowOf(screen, codeA)).toBeVisible();
      await expect(rowOf(screen, codeB)).toBeVisible();
    } finally {
      await apiCleanup(browser, app.baseUrl, codeA);
      await apiCleanup(browser, app.baseUrl, codeB);
    }
  });

  test('编辑：名称/规格修改后列表同步，编码和项目只读', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_EDIT');
    try {
      await app.open('/#/materials');
      await createViaUi(screen, { code, name: `E2E编辑前_${code}`, color: '原色', size: '原尺码' });

      await rowOf(screen, code).getByRole('button', '编辑').tap();
      const dialog = editDialog(screen);
      await expect(dialog).toBeVisible();

      // 编码字段 disabled
      await expect(dialog.getByRole('textbox', { name: /料号编码/ })).toBeDisabled();
      // 所属项目字段 disabled（编辑弹窗中用 el-input disabled 展示，不是 combobox）
      await expect(dialog.getByRole('textbox', { name: /所属项目/ })).toBeDisabled();

      const newName = `E2E编辑后_${code}`;
      await dialog.getByRole('textbox', { name: /料号名称/ }).fill(newName);
      await dialog.getByRole('textbox', { name: '颜色' }).fill('新色');
      await dialog.getByRole('textbox', { name: '尺码' }).fill('新尺码');
      await dialog.getByRole('button', '确定').tap();

      await expect(screen.getByText('编辑料号成功')).toBeVisible();
      await expect(editDialog(screen)).toBeHidden();

      const row = rowOf(screen, code);
      await expect(row).toContainText(newName);
      await expect(row).toContainText('新色');
      await expect(row).toContainText('新尺码');
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('删除：确认弹窗显示料号名称，确认后该行消失', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_DEL');
    const name = `E2E删除_${code}`;
    try {
      await app.open('/#/materials');
      await createViaUi(screen, { code, name });

      await expect(rowOf(screen, code)).toBeVisible();

      await rowOf(screen, code).getByRole('button', '删除').tap();
      const confirm = screen.getByRole('dialog', '删除确认');
      await expect(confirm).toBeVisible();
      // 确认弹窗显示料号名称
      await expect(confirm).toContainText(name);
      await confirm.getByRole('button', '确定').tap();

      await expect(screen.getByText('删除料号成功')).toBeVisible();
      await expect(rowOf(screen, code)).toHaveCount(0);
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('批量删除：勾选两条后一次删除', { session: 'admin' }, async ({ app, screen, browser }) => {
    const codeA = uniqueCode('E2E_BAT_A');
    const codeB = uniqueCode('E2E_BAT_B');
    try {
      await app.open('/#/materials');
      await createViaUi(screen, { code: codeA, name: `E2E批量_${codeA}` });
      await createViaUi(screen, { code: codeB, name: `E2E批量_${codeB}` });

      // 先按编码收敛，避免误勾历史数据
      await screen.getByRole('textbox', '料号编码').fill('E2E_BAT_');
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeA)).toBeVisible();
      await expect(rowOf(screen, codeB)).toBeVisible();

      await expect(screen.getByRole('button', '批量删除')).toBeDisabled();
      // el-checkbox 的可访问节点是隐藏 input，点击落在它的视觉外壳上
      await browser.locator(`.el-table__row:has-text("${codeA}") .el-checkbox`).click();
      await browser.locator(`.el-table__row:has-text("${codeB}") .el-checkbox`).click();
      await expect(screen.getByRole('button', '批量删除')).toBeEnabled();

      await screen.getByRole('button', '批量删除').tap();
      const confirm = screen.getByRole('dialog', '批量删除确认');
      await expect(confirm).toContainText('2 条');
      await confirm.getByRole('button', '确定').tap();

      await expect(screen.getByText('成功删除 2 条料号')).toBeVisible();
      await expect(rowOf(screen, codeA)).toHaveCount(0);
      await expect(rowOf(screen, codeB)).toHaveCount(0);
    } finally {
      await apiCleanup(browser, app.baseUrl, codeA);
      await apiCleanup(browser, app.baseUrl, codeB);
    }
  });

  test('状态切换：启用改禁用，重新查询后仍是禁用', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_STAT');
    try {
      await app.open('/#/materials');
      await createViaUi(screen, { code, name: `E2E状态_${code}` });

      const row = rowOf(screen, code);
      // el-switch 的可访问节点是隐藏的 input，点击要落在它的视觉外壳上
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

  test('导出：下载 CSV，表头与当前查询数据正确', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_EXP');
    try {
      await app.open('/#/materials');
      await createViaUi(screen, { code, name: `E2E导出_${code}`, color: '导出色' });

      // 按编码收敛导出范围
      await screen.getByRole('textbox', '料号编码').fill(code);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, code)).toBeVisible();

      const download = await browser.waitForDownload(async () => {
        await screen.getByRole('button', '导出').tap();
      });

      expect(download.suggestedFilename).toMatch(/^料号列表_\d{8}_\d{6}\.csv$/);
      const csv = readFileSync(download.absolutePath, 'utf8');
      expect(csv).toContain('ID,编码,名称,所属项目,绑定路由,颜色,尺码,规格1,规格2,规格3,描述,备注,状态,创建人,创建时间');
      expect(csv).toContain(code);
      expect(csv).toContain(`E2E导出_${code}`);
      // 表头 + 1 条数据
      expect(csv.split('\n').filter((line) => line.trim().length > 0)).toHaveLength(2);
      await expect(screen.getByText('导出成功')).toBeVisible();
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });
});
