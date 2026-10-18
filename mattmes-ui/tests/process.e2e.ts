import { test, describe } from '@e2e-dev/web';
import { expect } from 'e2e';
import { readFileSync } from 'node:fs';

/**
 * 工序管理（#/process）端到端用例：查 / 增 / 改 / 删（单条 + 批量）/ 状态切换 / 导出。
 *
 * 数据策略：每次用时间戳生成唯一工序编码，用例结束通过接口清理，
 * 重跑与失败重试都不会被残留数据影响。
 */

const stamp = () => `${Date.now()}`;

function uniqueCode(prefix: string) {
  return `${prefix}_${stamp()}`.slice(0, 50);
}

/** 用登录态接口清理指定编码的工序（测试数据收尾，不属于断言逻辑） */
async function apiCleanup(browser: any, baseUrl: string | undefined, code: string) {
  if (!baseUrl) return;
  const token = (await browser.evaluate(() => localStorage.getItem('token'))) as string | null;
  if (!token) return;
  const headers = { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` };

  const listRes = await fetch(new URL('/api/process/list', baseUrl), {
    method: 'POST',
    headers,
    body: JSON.stringify({ code, pageNum: 1, pageSize: 100 }),
  });
  if (!listRes.ok) return;
  const payload = (await listRes.json()) as { data?: { list?: Array<{ id: number }> } };
  const ids = (payload.data?.list ?? []).map((row) => row.id);
  if (ids.length === 0) return;

  await fetch(new URL('/api/process/batchDelete', baseUrl), {
    method: 'DELETE',
    headers,
    body: JSON.stringify(ids),
  });
}

function rowOf(screen: any, code: string) {
  return screen.getByRole('row').filter({ hasText: code });
}

function addDialog(screen: any) {
  return screen.getByRole('dialog', '新增工序');
}

function editDialog(screen: any) {
  return screen.getByRole('dialog', '编辑工序');
}

/**
 * 打开 el-select 的下拉面板并选中一项。
 * 面板挂在 body 层，不在 dialog 内：combobox 用 scope 取，option 用根 screen 取。
 */
async function pickOption(screen: any, scope: any, comboboxName: RegExp | string, option: string) {
  await scope.getByRole('combobox', comboboxName).press('Enter');
  await screen.getByRole('option', option).tap();
}

/** 通过 UI 新增一条工序 */
async function createViaUi(
  screen: any,
  data: { code: string; name: string; type: string; description?: string; remark?: string }
) {
  await screen.getByRole('button', '新增').tap();
  const dialog = addDialog(screen);
  await expect(dialog).toBeVisible();

  await dialog.getByRole('textbox', { name: /工序编码/ }).fill(data.code);
  await dialog.getByRole('textbox', { name: /工序名称/ }).fill(data.name);
  await pickOption(screen, dialog, /工序类型/, data.type);
  if (data.description) {
    await dialog.getByRole('textbox', { name: '工序描述' }).fill(data.description);
  }
  if (data.remark) {
    await dialog.getByRole('textbox', { name: '备注' }).fill(data.remark);
  }

  await dialog.getByRole('button', '确定').tap();
  // ElMessage 会叠加多条，取最后一条判定提示出现
  await expect(screen.getByText('新增工序成功').last()).toBeVisible();
  await expect(addDialog(screen)).toBeHidden();
}

describe('工序管理', () => {
  test('页面结构：查询条件、操作按钮、表格列齐全', { session: 'admin' }, async ({ app, screen }) => {
    await app.open('/#/process');

    await expect(screen.getByRole('textbox', '工序编码')).toBeVisible();
    await expect(screen.getByRole('textbox', '工序名称')).toBeVisible();
    await expect(screen.getByRole('combobox', '工序类型')).toBeVisible();
    await expect(screen.getByRole('combobox', '启用状态')).toBeVisible();

    for (const label of ['查询', '重置', '新增', '批量删除', '导出']) {
      await expect(screen.getByRole('button', label)).toBeVisible();
    }

    for (const header of ['ID', '工序编码', '工序名称', '工序类型', '工序描述', '启用状态', '创建时间', '备注', '操作']) {
      await expect(screen.getByRole('columnheader', header)).toBeVisible();
    }
  });

  test('新增：必填校验拦截，填写后写入列表', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_ADD');
    const name = `E2E新增_${code}`;
    try {
      await app.open('/#/process');
      await screen.getByRole('button', '新增').tap();

      const dialog = addDialog(screen);
      await expect(screen.getByRole('heading', '新增工序')).toBeVisible();

      // 空表单提交：必填提示出现，弹窗不关闭
      await dialog.getByRole('button', '确定').tap();
      await expect(dialog).toBeVisible();
      await expect(dialog.getByText('请输入工序编码').last()).toBeVisible();
      await expect(dialog.getByText('请输入工序名称').last()).toBeVisible();
      await expect(dialog.getByRole('combobox', /工序类型/)).toBeVisible();

      // 编码格式校验：非法字符被拦
      await dialog.getByRole('textbox', { name: /工序编码/ }).fill('非法 编码!');
      await dialog.getByRole('textbox', { name: /工序名称/ }).focus();
      await expect(dialog.getByText('编码只能包含字母、数字、下划线和中划线').last()).toBeVisible();

      await dialog.getByRole('textbox', { name: /工序编码/ }).fill(code);
      await dialog.getByRole('textbox', { name: /工序名称/ }).fill(name);
      await pickOption(screen, dialog, /工序类型/, '检测');
      await dialog.getByRole('textbox', { name: '工序描述' }).fill('E2E 新增描述');
      await dialog.getByRole('textbox', { name: '备注' }).fill('E2E 新增备注');

      await dialog.getByRole('button', '确定').tap();
      await expect(screen.getByText('新增工序成功')).toBeVisible();
      await expect(addDialog(screen)).toBeHidden();

      const row = rowOf(screen, code);
      await expect(row).toBeVisible();
      await expect(row).toContainText(name);
      await expect(row).toContainText('检测');
      await expect(row).toContainText('E2E 新增描述');
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('查询：按编码、按类型过滤，重置后条件与列表恢复', { session: 'admin' }, async ({ app, screen, browser }) => {
    const codeA = uniqueCode('E2E_QRY_A');
    const codeB = uniqueCode('E2E_QRY_B');
    try {
      await app.open('/#/process');
      await createViaUi(screen, { code: codeA, name: `E2E查询_${codeA}`, type: '检测' });
      await createViaUi(screen, { code: codeB, name: `E2E查询_${codeB}`, type: '组装' });

      // ① 按编码模糊查询，只留 A
      await screen.getByRole('textbox', '工序编码').fill(codeA);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeA)).toBeVisible();
      await expect(rowOf(screen, codeB)).toHaveCount(0);

      // ② 清编码，按工序类型查询，只留 B
      await screen.getByRole('textbox', '工序编码').fill('');
      await pickOption(screen, screen, '工序类型', '组装');
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeB)).toBeVisible();
      await expect(rowOf(screen, codeA)).toHaveCount(0);

      // ③ 重置：条件清空，两条都回来
      await screen.getByRole('button', '重置').tap();
      await expect(screen.getByRole('textbox', '工序编码')).toHaveValue('');
      await expect(rowOf(screen, codeA)).toBeVisible();
      await expect(rowOf(screen, codeB)).toBeVisible();
    } finally {
      await apiCleanup(browser, app.baseUrl, codeA);
      await apiCleanup(browser, app.baseUrl, codeB);
    }
  });

  test('编辑：名称/类型/描述修改后列表同步，编码只读', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_EDIT');
    try {
      await app.open('/#/process');
      await createViaUi(screen, { code, name: `E2E编辑前_${code}`, type: '检测' });

      await rowOf(screen, code).getByRole('button', '编辑').tap();
      const dialog = editDialog(screen);
      await expect(dialog).toBeVisible();
      await expect(dialog.getByRole('textbox', { name: /工序编码/ })).toBeDisabled();

      const newName = `E2E编辑后_${code}`;
      await dialog.getByRole('textbox', { name: /工序名称/ }).fill(newName);
      await pickOption(screen, dialog, /工序类型/, '包装');
      await dialog.getByRole('textbox', { name: '工序描述' }).fill('E2E 编辑后描述');
      await dialog.getByRole('button', '确定').tap();

      await expect(screen.getByText('编辑工序成功')).toBeVisible();
      await expect(editDialog(screen)).toBeHidden();

      const row = rowOf(screen, code);
      await expect(row).toContainText(newName);
      await expect(row).toContainText('包装');
      await expect(row).toContainText('E2E 编辑后描述');
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('删除：确认弹窗后该行消失', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_DEL');
    try {
      await app.open('/#/process');
      await createViaUi(screen, { code, name: `E2E删除_${code}`, type: '其他' });
      await expect(rowOf(screen, code)).toBeVisible();

      await rowOf(screen, code).getByRole('button', '删除').tap();
      const confirm = screen.getByRole('dialog', '删除确认');
      await expect(confirm).toBeVisible();
      await expect(confirm).toContainText(`E2E删除_${code}`);
      await confirm.getByRole('button', '确定').tap();

      await expect(screen.getByText('删除工序成功')).toBeVisible();
      await expect(rowOf(screen, code)).toHaveCount(0);
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });

  test('批量删除：勾选两条后一次删除', { session: 'admin' }, async ({ app, screen, browser }) => {
    const codeA = uniqueCode('E2E_BAT_A');
    const codeB = uniqueCode('E2E_BAT_B');
    try {
      await app.open('/#/process');
      await createViaUi(screen, { code: codeA, name: `E2E批量_${codeA}`, type: '检测' });
      await createViaUi(screen, { code: codeB, name: `E2E批量_${codeB}`, type: '检测' });

      // 先按编码收敛，避免误勾历史数据
      await screen.getByRole('textbox', '工序编码').fill('E2E_BAT_');
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

      await expect(screen.getByText('成功删除 2 条工序')).toBeVisible();
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
      await app.open('/#/process');
      await createViaUi(screen, { code, name: `E2E状态_${code}`, type: '检测' });

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
      await app.open('/#/process');
      await createViaUi(screen, { code, name: `E2E导出_${code}`, type: '组装' });

      // 按编码收敛导出范围
      await screen.getByRole('textbox', '工序编码').fill(code);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, code)).toBeVisible();

      const download = await browser.waitForDownload(async () => {
        await screen.getByRole('button', '导出').tap();
      });

      expect(download.suggestedFilename).toMatch(/^工序数据_\d{8}_\d{6}\.csv$/);
      const csv = readFileSync(download.absolutePath, 'utf8');
      expect(csv).toContain('ID,工序编码,工序名称,工序类型,工序描述,启用状态,备注,创建人,创建时间');
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
