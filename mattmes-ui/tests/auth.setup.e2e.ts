import { test } from '@e2e-dev/web';
import { expect, credentials } from 'e2e';

test.setup('sign in as admin', { sessions: ['admin'] }, async ({ app, screen, browser, session }) => {
  const admin = credentials.user('admin');
  await app.open('/');
  await screen.getByPlaceholder('请输入工号').fill(admin.username);
  await screen.getByPlaceholder('请输入密码').fill(admin.password);
  await screen.getByRole('button', '登录').tap();

  // 单点登录：账号已在别处登录时会弹 409 确认框，确认后继续
  const force = screen.getByRole('button', '确认登录');
  try {
    await force.waitFor({ state: 'visible', timeout: 5_000 });
    await force.tap();
  } catch {
    // 未出现冲突弹窗，正常登录
  }

  await expect(browser).not.toHaveURL(/\/login/);
  await session.save('admin');
});
