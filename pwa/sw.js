/* EPA iPhone web uygulaması: internetsiz çalışma. Sürüm her yayında değişir. */
const CACHE = 'epa-__VERSION__';
const FILES = ['./', './index.html', './manifest.webmanifest', './apple-touch-icon.png', './icon-192.png', './icon-512.png', './lib/jspdf.umd.min.js', './lib/html2canvas.min.js'];
self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES)).then(() => self.skipWaiting()));
});
self.addEventListener('activate', e => {
  e.waitUntil(caches.keys().then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener('fetch', e => {
  const r = e.request;
  if (r.method !== 'GET') return;
  const u = new URL(r.url);
  if (u.origin !== location.origin) return; // yazı tipleri vb. ağdan
  e.respondWith(
    caches.open(CACHE).then(async c => {
      const hit = await c.match(r, {ignoreSearch: true}) || (r.mode === 'navigate' ? await c.match('./index.html') : null);
      const net = fetch(r).then(res => { if (res && res.ok) c.put(r, res.clone()); return res; }).catch(() => null);
      return hit || (await net) || new Response('Çevrimdışı', {status: 503});
    })
  );
});
