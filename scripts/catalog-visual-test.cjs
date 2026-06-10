const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page.goto('http://localhost:4200/products', { waitUntil: 'networkidle', timeout: 60000 });
  await page.waitForSelector('article .catalog-card-image, article img', { timeout: 30000 }).catch(() => {});
  await page.waitForTimeout(2000);
  const cards = await page.$$eval('article', articles => articles.slice(0, 8).map(a => {
    const img = a.querySelector('.catalog-card-image, img');
    const media = a.querySelector('.catalog-card-media');
    if (!img || !media) return { error: 'no img/media' };
    const imgStyle = getComputedStyle(img);
    const mediaStyle = getComputedStyle(media);
    return {
      alt: img.alt,
      natural: { w: img.naturalWidth, h: img.naturalHeight },
      rendered: { w: img.clientWidth, h: img.clientHeight },
      media: { w: media.clientWidth, h: media.clientHeight },
      objectFit: imgStyle.objectFit,
      objectPosition: imgStyle.objectPosition,
      croppedX: img.naturalWidth > 0 && img.clientWidth >= img.naturalWidth * 0.98 && img.naturalHeight > img.clientHeight,
      croppedY: img.naturalHeight > 0 && img.clientHeight >= img.naturalHeight * 0.98 && img.naturalWidth > img.clientWidth,
      visible: img.complete && img.naturalWidth > 0,
    };
  }));
  console.log(JSON.stringify(cards, null, 2));
  await page.screenshot({ path: 'scripts/catalog-visual-test.png', fullPage: true });
  await browser.close();
})().catch(e => { console.error(e); process.exit(1); });
