import { expect, test } from '@playwright/test';

test('login test', async ({ page }) => {

  await page.goto('http://localhost:8080/Login');

  await page.fill('#userId', 'a0001');
  await page.fill('#password', 'k1226');
  await page.click('#loginBtn');

  await expect(page).toHaveTitle('メインメニュー（a0001）');

});