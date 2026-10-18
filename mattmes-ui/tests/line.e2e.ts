import { test, describe } from '@e2e-dev/web';
import { expect } from 'e2e';
import { readFileSync } from 'node:fs';

/**
 * 线体管理（/#/lines）端到端用例：菜单入口 / 页面结构 / 新增（必填、格式、唯一性）/
 * 编辑（编码只读）/ 状态切换 / 单条与批量删除 / 查询与重置 / 导出 CSV（BOM、转义）。
 *
 * 数据策略：每次用时间戳生成唯一线体编码，用例结束通过接口清理（内含已删除记录），
 * 重跑与失败重试都不会被残留数据影响。
 */

const stamp = () => `${Date.now()}`;

function uniqueCode(prefix: string) {
  return `${prefix}_${stamp()}`.slice(0, 50);
}

/** 用登录态接口查出该编码的所有线体（含已逻辑删除的），返回 id 列表 */
async function apiFindIds(browser: any, baseUrl: string | undefined, code: string): Promise<number[]> {
  if (!baseUrl) return [];
  const token = (await browser.evaluate(() => localStorage.getItem('token'))) as string | null;
  if (!token) return [];
  const res = await fetch(new URL('/api/line/list', baseUrl), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify({ code, pageNum: 1, pageSize: 100 }),
  });
  if (!res.ok) return [];
  const payload = (await res.json()) as { data?: { list?: Array<{ id: number }> } };
  return (payload.data?.list ?? []).map((row) => row.id);
}

/** 用登录态接口逻辑删除指定编码的线体（测试数据收尾，不属于断言逻辑） */
async function apiCleanup(browser: any, baseUrl: string | undefined, code: string) {
  const ids = await apiFindIds(browser, baseUrl, code);
  if (ids.length === 0 || !baseUrl) return;
  const token = (await browser.evaluate(() => localStorage.getItem('token'))) as string | null;
  if (!token) return;
  await fetch(new URL('/api/line/batchDelete', baseUrl), {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(ids),
  });
}

/**
 * 预设勾选状态，供后续点击使用。
 * 建站即勾选会触发列表刷新，把当前行冲掉，所以不直接用 locator 点，而是先在页面里把
 * 该行的选择框标记出来，再由后续步骤点击这个标记（标记随渲染节点一起存续）。
 */
async function markCheckbox(browser: any, code: string) {
  const ok = await browser.evaluate(
    (rowCode: string) => {
      const rows = Array.from(document.querySelectorAll('.el-table__body .el-table__row'));
      const row = rows.find((r) => (r.textContent ?? '').includes(rowCode));
      const box = row?.querySelector('.el-checkbox') as HTMLElement | null;
      if (!box) return false;
      box.setAttribute('data-e2e-target', rowCode);
      return true;
    },
    code
  );
  if (!ok) throw new Error(`列表中没有找到编码为 ${code} 的行`);
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

/**
 * 打开 el-select 的下拉面板并选中一项。
 * 面板挂在 body 层，不在 dialog 内：combobox 用 scope 取，option 用根 screen 取。
 */
async function pickOption(screen: any, scope: any, comboboxName: RegExp | string, option: string) {
  await scope.getByRole('combobox', comboboxName).press('Enter');
  await screen.getByRole('option', option).tap();
}

/**
 * 选中 el-radio。
 * el-radio 的可访问节点是隐藏的 input，`.el-radio__inner` 圆形控件会拦截指针事件，
 * 因此点击落在它所在标签的视觉外壳上（与既有 process/material 用例处理 el-checkbox 同一思路）。
 */
async function pickRadio(browser: any, dialogTitle: string, label: string) {
  await browser
    .locator(`.el-dialog:has-text("${dialogTitle}") .el-radio:has-text("${label}")`)
    .click();
}

/** 通过 UI 新增一条线体 */
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

/** 一个最小可用的 CSV 记录切分（RFC4180：引号内允许逗号与换行） */
function splitCsvRecords(text: string): string[] {
  const records: string[] = [];
  let field = '';
  let record = '';
  let inQuotes = false;
  for (let i = 0; i < text.length; i += 1) {
    const ch = text[i];
    if (inQuotes) {
      if (ch === '"') {
        if (text[i + 1] === '"') {
          field += '"';
          record += '""';
          i += 1;
        } else {
          inQuotes = false;
          record += ch;
        }
      } else {
        field += ch;
        record += ch;
      }
      continue;
    }
    if (ch === '"') {
      inQuotes = true;
      record += ch;
    } else if (ch === ',') {
      field = '';
      record += ch;
    } else if (ch === '\r') {
      // 跳过 CRLF 的 CR
    } else if (ch === '\n') {
      records.push(record);
      record = '';
      field = '';
    } else {
      field += ch;
      record += ch;
    }
  }
  if (record.length > 0) records.push(record);
  return records.filter((line) => line.trim().length > 0);
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
    // 线体本期没有备注列
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

  test('唯一性：编码重复被后端拦截，删除后同编码重建仍提示已存在', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_DUP');
    const name = `E2E重复_${code}`;
    try {
      await app.open('/#/lines');
      await createViaUi(screen, browser, { code, name });

      // ① 同编码再建一次：弹窗保持打开，提示编码已存在，列表仍只有一条
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

      // ② 删除该线体后，用同编码重建仍应被提示"已存在"（唯一性连同已删除记录一起查重）
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

  test('批量删除：未勾选时禁用，勾选两条后确认框显示条数并一次删完', { session: 'admin' }, async ({ app, screen, browser }) => {
    const codeA = uniqueCode('E2E_BAT_A');
    const codeB = uniqueCode('E2E_BAT_B');
    try {
      await app.open('/#/lines');
      await createViaUi(screen, browser, { code: codeA, name: `E2E批量_${codeA}` });
      await createViaUi(screen, browser, { code: codeB, name: `E2E批量_${codeB}` });

      // 只按共同的时间戳段收敛：A/B 的前缀不同，用公共前缀查不到两条
      const stampPart = codeA.slice(codeA.lastIndexOf('_') + 1);
      await filterByCode(screen, stampPart);
      await expect(rowOf(screen, codeA)).toHaveCount(1);
      await expect(rowOf(screen, codeB)).toHaveCount(1);
      await expect(screen.getByRole('button', '批量删除')).toBeDisabled();
      // el-checkbox 的可访问节点是隐藏 input，点击要落在它的视觉外壳上
      await markCheckbox(browser, codeA);
      await browser.locator('[data-e2e-target="' + codeA + '"]').click();
      await expect(screen.getByRole('button', '批量删除')).toBeEnabled();
      await markCheckbox(browser, codeB);
      await browser.locator('[data-e2e-target="' + codeB + '"]').click();
      // 两条都勾上：批量删除按钮保持可用
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
      await screen.getByRole('textbox', '线体名称').fill(`E2E查询乙_${codeB}`);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeB)).toBeVisible();
      await expect(rowOf(screen, codeA)).toHaveCount(0);

      // ③ 按状态筛选：禁用只剩 B；启用只剩 A
      await screen.getByRole('textbox', '线体名称').fill('');
      await pickOption(screen, screen, '启用状态', '禁用');
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeB)).toBeVisible();
      await expect(rowOf(screen, codeA)).toHaveCount(0);

      await pickOption(screen, screen, '启用状态', '启用');
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeA)).toBeVisible();
      await expect(rowOf(screen, codeB)).toHaveCount(0);

      // ④ 组合条件：编码命中 A + 名称命中 B → 无结果
      await screen.getByRole('textbox', '线体编码').fill(codeA);
      await screen.getByRole('textbox', '线体名称').fill(`E2E查询乙_${codeB}`);
      await screen.getByRole('button', '查询').tap();
      await expect(rowOf(screen, codeA)).toHaveCount(0);
      await expect(rowOf(screen, codeB)).toHaveCount(0);

      // ⑤ 重置：三个条件清空，两条都回来
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

  test('导出：CSV 带 BOM、7 列表头、文件名带时间戳，特殊字符描述正确转义', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_EXP');
    // 描述里同时含逗号、双引号、换行
    const description = '含逗号,含"引号",含换行\n第二行';
    try {
      await app.open('/#/lines');
      await createViaUi(screen, browser, { code, name: `E2E导出_${code}`, description });
      await filterByCode(screen, code);
      await expect(rowOf(screen, code)).toBeVisible();

      const download = await browser.waitForDownload(async () => {
        await screen.getByRole('button', '导出').tap();
      });

      // 文件名带 yyyyMMdd_HHmmss 时间戳
      expect(download.suggestedFilename).toMatch(/^线体数据_\d{8}_\d{6}\.csv$/);

      // 按字节读，先验证 UTF-8 BOM（EF BB BF）
      const raw = readFileSync(download.absolutePath);
      expect(Array.from(raw.subarray(0, 3))).toEqual([0xef, 0xbb, 0xbf]);

      const csv = raw.toString('utf8').replace(/^\uFEFF/, '');
      const records = splitCsvRecords(csv);

      // 表头 7 列，顺序固定
      expect(records[0]).toBe('ID,线体编码,线体名称,描述,启用状态,创建人,创建时间');

      // 只按本条编码收敛，应当只有表头 + 1 条数据
      expect(records).toHaveLength(2);
      const row = records[1];
      expect(row).toContain(code);
      expect(row).toContain(`E2E导出_${code}`);
      // 描述整体被引号包裹，内部双引号成对转义，逗号与换行不拆列
      expect(row).toContain(`"含逗号,含""引号"",含换行\n第二行"`);
      // 状态列导出原始值 1，不转中文
      expect(row).toMatch(/,1,/);
      expect(row).not.toContain('启用');

      await expect(screen.getByText('导出成功')).toBeVisible();
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });
});
