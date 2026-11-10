const { chromium } = require('playwright-core');
(async () => {
  const b = await chromium.launch({ executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe', headless: true });
  const p = await b.newPage({ viewport: { width: 1400, height: 900 } });
  await p.goto('http://localhost:80/', { waitUntil: 'domcontentloaded' });
  await p.waitForTimeout(2000);
  await p.click('.submit-btn').catch(()=>{});
  await p.waitForTimeout(6000);
  // 直接看页面里商品市场页请求的结果
  const out = await p.evaluate(async () => {
    const token = localStorage.getItem('Admin-Token') || '';
    const h = { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token, clientid: 'e5cd7e4891bf95d1d19206ce24a7b32e' };
    const r = await fetch('/dev-api/trade/product/page?pageNum=1&pageSize=5', { headers: h });
    const j = await r.json();
    return { status: r.status, code: j.code, msg: j.msg, dataType: typeof j.data, dataSample: JSON.stringify(j.data).slice(0, 260) };
  });
  console.log(JSON.stringify(out, null, 2));
  await b.close();
})();
