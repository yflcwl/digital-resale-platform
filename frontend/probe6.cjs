const { chromium } = require('playwright-core');
(async () => {
  const b = await chromium.launch({ executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe', headless: true });
  const p = await b.newPage({ viewport: { width: 1400, height: 900 } });
  await p.goto('http://localhost:80/', { waitUntil: 'domcontentloaded' });
  await p.waitForTimeout(2000);
  await p.click('.submit-btn').catch(()=>{});
  await p.waitForTimeout(6000);
  const out = await p.evaluate(() => {
    const menu = document.querySelector('.topbar-menu');
    const active = document.querySelector('.topbar-menu .is-active');
    const cs = active ? getComputedStyle(active) : null;
    return {
      menuTag: menu ? menu.tagName + '.' + menu.className : null,
      firstChildTag: menu ? menu.firstElementChild?.tagName + '.' + menu.firstElementChild?.className : null,
      menuHTML: menu ? menu.outerHTML.slice(0, 420) : null,
      activeCls: active ? active.className : null,
      activeBg: cs ? cs.backgroundColor : null,
      activeColor: cs ? cs.color : null,
      activeRadius: cs ? cs.borderRadius : null
    };
  });
  console.log(JSON.stringify(out, null, 2));
  await b.close();
})();
