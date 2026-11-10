const { chromium } = require('playwright-core');

const OUT = 'D:/QQ下载/毕业论文/开发文档/界面预览';

(async () => {
  const browser = await chromium.launch({
    executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe',
    headless: true,
    args: ['--disable-gpu', '--hide-scrollbars']
  });
  const ctx = await browser.newContext({ viewport: { width: 1600, height: 1000 }, deviceScaleFactor: 1 });
  const page = await ctx.newPage();

  page.on('console', (m) => {
    if (m.type() === 'error') console.log('[console error]', m.text().slice(0, 160));
  });

  await page.goto('http://localhost:80/', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(2500);

  // 登录（表单已预填 admin/admin123，验证码已关闭）
  await page.click('.submit-btn').catch(() => {});
  await page.waitForTimeout(7000);

  const url = page.url();
  console.log('登录后地址:', url);

  await page.screenshot({ path: `${OUT}/layout-home.png`, fullPage: false });

  // 表格页
  await page.goto('http://localhost:80/system/user', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(5000);
  await page.screenshot({ path: `${OUT}/layout-table.png`, fullPage: false });

  // 折叠侧边栏
  await page.click('#hamburger-container').catch(() => {});
  await page.waitForTimeout(1200);
  await page.screenshot({ path: `${OUT}/layout-collapsed.png`, fullPage: false });

  await browser.close();
  console.log('done');
})();
