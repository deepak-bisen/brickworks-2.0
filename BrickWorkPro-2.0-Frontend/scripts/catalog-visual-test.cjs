const { chromium } = require('playwright');
const path = require('path');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page.goto('http://localhost:4200/products', { waitUntil: 'networkidle', timeout: 60000 });
  await page.waitForTimeout(3000);

  const cards = await page.$$eval('article', (articles) =>
    articles.slice(0, 8).map((a) => {
      const img = a.querySelector('.catalog-card-image, img');
      const media = a.querySelector('.catalog-card-media');
      if (!img || !media) return { error: 'no img/media', html: a.innerHTML.slice(0, 200) };
      const imgStyle = getComputedStyle(img);
      const scaleX = img.naturalWidth ? img.clientWidth / img.naturalWidth : 0;
      const scaleY = img.naturalHeight ? img.clientHeight / img.naturalHeight : 0;
      const scale = Math.max(scaleX, scaleY);
      return {
        alt: img.alt,
        natural: { w: img.naturalWidth, h: img.naturalHeight },
        rendered: { w: img.clientWidth, h: img.clientHeight },
        media: { w: media.clientWidth, h: media.clientHeight },
        objectFit: imgStyle.objectFit,
        scaleX: Number(scaleX.toFixed(3)),
        scaleY: Number(scaleY.toFixed(3)),
        likelyCropped: scaleX > scaleY + 0.05 || scaleY > scaleX + 0.05,
        cssClasses: img.className,
        mediaClasses: media.className,
      };
    }),
  );

  console.log(JSON.stringify(cards, null, 2));
  const out = path.join(__dirname, '..', '..', 'scripts', 'catalog-visual-test.png');
  await page.screenshot({ path: out, fullPage: true });
  console.log('Screenshot:', out);
  await browser.close();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});