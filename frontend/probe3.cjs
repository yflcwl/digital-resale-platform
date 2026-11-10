const { chromium } = require('playwright-core');
(async () => {
  const b = await chromium.launch({ executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe', headless: true });
  const p = await b.newPage({ viewport: { width: 1400, height: 900 } });
  await p.goto('http://localhost:80/', { waitUntil: 'domcontentloaded' });
  await p.waitForTimeout(2000);
  await p.click('.submit-btn').catch(()=>{});
  await p.waitForTimeout(6000);
  const out = await p.evaluate(async () => {
    const token = localStorage.getItem('Admin-Token') || '';
    const h = { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token };
    const j1 = await (await fetch('/dev-api/trade/product/page?pageNum=1&pageSize=5', { headers: h })).json();
    const j2 = await (await fetch('/dev-api/trade/category/tree', { headers: h })).json();
    return {
      productData: JSON.stringify(j1.data).slice(0, 400),
      catData: JSON.stringify(j2.data).slice(0, 300)
    };
  });
  console.log(JSON.stringify(out, null, 2));
  await b.close();
})();
