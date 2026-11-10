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
    const h = { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token, clientid: 'e5cd7e4891bf95d1d19206ce24a7b32e' };
    const j1 = await (await fetch('/dev-api/trade/product/page?pageNum=1&pageSize=5', { headers: h })).json();
    const j2 = await (await fetch('/dev-api/trade/category/tree', { headers: h })).json();
    return {
      p_code: j1.code, p_msg: j1.msg, p_keys: Object.keys(j1),
      p_total: j1.total ?? j1.data?.total, p_rows: (j1.rows || j1.data?.rows || []).length,
      p_body: JSON.stringify(j1).slice(0, 220),
      c_code: j2.code, c_isArr: Array.isArray(j2.data), c_len: Array.isArray(j2.data) ? j2.data.length : null
    };
  });
  console.log(JSON.stringify(out, null, 2));
  await b.close();
})();
