import { expect, test } from '@playwright/test';

test('login test', async ({ page }) => {

  await page.goto('http://localhost:8080/Login');

  await page.getByLabel('ユーザーID').fill('a0001');
  await page.getByLabel('パスワード').fill('k1226');
  await page.getByRole('button', { name: 'ログイン' }).click();

  await expect(page).toHaveTitle('メインメニュー（a0001）');

});