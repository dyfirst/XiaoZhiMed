import { expect, test } from '@playwright/test';

test('聊天主流程可以发送消息并展示回复', async ({ page }) => {
  await page.route('**/api/xiaozhi/chat', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'text/plain; charset=utf-8',
      body: '这是来自模拟后端的流式回复。',
    });
  });

  await page.goto('/chat');
  await page.getByPlaceholder('输入症状、科室诉求或预约需求，回车发送，Shift + 回车换行').fill('我想挂神经内科');
  await page.getByRole('button', { name: '发送消息' }).click();

  await expect(page.locator('.message-body').filter({ hasText: '这是来自模拟后端的流式回复。' })).toBeVisible();
  await expect(page.getByText('用户提问')).toBeVisible();
});
