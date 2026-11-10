const { chromium } = require('playwright-core');
(async () => {
  const b = await chromium.launch({ executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe', headless: true });
  const p = await b.newPage({ viewport: { width: 1600, height: 900 } });
  await p.goto('http://localhost:80/', { waitUntil: 'domcontentloaded' });
  await p.waitForTimeout(2000);
  await p.click('.submit-btn').catch(()=>{});
  await p.waitForTimeout(6000);
  await p.screenshot({ path: 'D:/QQ下载/毕业论文/开发文档/界面预览/topnav.png', clip: { x: 0, y: 0, width: 1600, height: 130 } });
  await b.close();
  console.log('done');
})();
