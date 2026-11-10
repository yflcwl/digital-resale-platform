const { chromium } = require('playwright-core');
(async () => {
  const b = await chromium.launch({ executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe', headless: true });
  const p = await b.newPage({ viewport: { width: 1400, height: 900 } });
  await p.goto('http://localhost:80/', { waitUntil: 'domcontentloaded' });
  await p.waitForTimeout(2000);
  await p.click('.submit-btn').catch(()=>{});
  await p.waitForTimeout(6000);

  const out = await p.evaluate(async () => {
    const keys = Object.keys(localStorage);
    const tokenKey = keys.find(k => /token/i.test(k));
    const token = tokenKey ? localStorage.getItem(tokenKey) : '';
    const h = { 'Content-Type': 'application/json' };
    if (token) h['Authorization'] = 'Bearer ' + token;
    const r1 = await fetch('/dev-api/trade/product/page?pageNum=1&pageSize=5', { headers: h });
    const j1 = await r1.json().catch(() => null);
    const r2 = await fetch('/dev-api/trade/category/tree', { headers: h });
    const j2 = await r2.json().catch(() => null);
    return {
      tokenKey,
      productStatus: r1.status,
      productTopKeys: j1 ? Object.keys(j1) : null,
      productTotal: j1 && (j1.total ?? j1.data?.total),
      productRowsLen: (j1?.rows || j1?.data?.rows || []).length,
      catIsArray: Array.isArray(j2?.data),
      catLen: Array.isArray(j2?.data) ? j2.data.length : (j2 ? Object.keys(j2) : null)
    };
  });
  console.log(JSON.stringify(out, null, 2));
  await b.close();
})();
