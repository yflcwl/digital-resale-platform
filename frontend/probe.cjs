const { chromium } = require('playwright-core');
(async () => {
  const b = await chromium.launch({ executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe', headless: true });
  const p = await b.newPage({ viewport: { width: 1600, height: 1000 } });
  await p.goto('http://localhost:80/', { waitUntil: 'domcontentloaded' });
  await p.waitForTimeout(2500);
  await p.click('.submit-btn').catch(()=>{});
  await p.waitForTimeout(6000);
  const info = await p.evaluate(() => {
    const btn = document.querySelector('.empty .el-button');
    if (!btn) return 'no button found';
    const cs = getComputedStyle(btn);
    return {
      outer: btn.outerHTML.slice(0, 200),
      bg: cs.backgroundColor, color: cs.color, h: cs.height, w: cs.width, fs: cs.fontSize,
      text: btn.innerText
    };
  });
  console.log(JSON.stringify(info, null, 2));
  await b.close();
})();
