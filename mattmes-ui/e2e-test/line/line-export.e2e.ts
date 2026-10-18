import { test, describe } from '@e2e-dev/web';
import { expect } from 'e2e';
import { readFileSync } from 'node:fs';

/**
 * 线体导出（#/lines → 导出）端到端用例：
 * 文件名时间戳、UTF-8 BOM、7 列表头、状态导出原始值、含逗号/双引号/换行的描述正确转义、
 * 导出范围跟随当前查询条件。
 */

const stamp = () => `${Date.now()}`;

function uniqueCode(prefix: string) {
  return `${prefix}_${stamp()}`.slice(0, 50);
}

async function authToken(browser: any): Promise<string | null> {
  const token = (await browser.evaluate(() => localStorage.getItem('token'))) as string | null;
  return token || null;
}

function apiHeaders(token: string) {
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` };
}

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
    await browser.locator('.el-dialog:has-text("新增线体") .el-radio:has-text("禁用")').click();
  }
  if (data.description) {
    await dialog.getByRole('textbox', '描述').fill(data.description);
  }

  await dialog.getByRole('button', '确定').tap();
  await expect(screen.getByText('新增线体成功').last()).toBeVisible();
  await expect(addDialog(screen)).toBeHidden();
}

async function filterByCode(screen: any, code: string) {
  await screen.getByRole('textbox', '线体编码').fill(code);
  await screen.getByRole('button', '查询').tap();
}

/** 最小可用的 CSV 记录切分（RFC4180：引号内允许逗号与换行，"" 表示一个引号） */
function splitCsvRecords(text: string): string[] {
  const records: string[] = [];
  let record = '';
  let inQuotes = false;
  for (let i = 0; i < text.length; i += 1) {
    const ch = text[i];
    if (inQuotes) {
      if (ch === '"') {
        if (text[i + 1] === '"') {
          record += '""';
          i += 1;
        } else {
          inQuotes = false;
          record += ch;
        }
      } else {
        record += ch;
      }
      continue;
    }
    if (ch === '"') {
      inQuotes = true;
      record += ch;
    } else if (ch === '\r') {
      // 跳过 CRLF 的 CR
    } else if (ch === '\n') {
      records.push(record);
      record = '';
    } else {
      record += ch;
    }
  }
  if (record.length > 0) records.push(record);
  return records.filter((line) => line.trim().length > 0);
}

describe('线体导出', () => {
  test('导出：CSV 带 BOM、7 列表头、时间戳文件名、特殊字符描述正确转义', { session: 'admin' }, async ({ app, screen, browser }) => {
    const code = uniqueCode('E2E_EXP');
    // 描述里同时含逗号、双引号、换行
    const description = '含逗号,含引号",含换行\n第二行';
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

      // 表头固定 7 列
      expect(records[0]).toBe('ID,线体编码,线体名称,描述,启用状态,创建人,创建时间');

      // 已按编码收敛，应当只有表头 + 1 条数据
      expect(records).toHaveLength(2);
      const row = records[1];
      expect(row).toContain(code);
      expect(row).toContain(`E2E导出_${code}`);
      // 描述整体被引号包裹，内部双引号成对转义，逗号与换行不拆列
      expect(row).toContain('"含逗号,含引号"",含换行\n第二行"');
      // 状态列导出原始值 1，不转中文
      expect(row).toMatch(/,1,/);
      expect(row).not.toContain('启用');

      await expect(screen.getByText('导出成功')).toBeVisible();
    } finally {
      await apiCleanup(browser, app.baseUrl, code);
    }
  });
});
