/* EPA iPhone web uygulaması: internetsiz çalışma. Sürüm her yayında değişir.
   Sayfa: internet varsa her açılışta en yenisi (ağ önce), yoksa kayıtlı sürüm.
   Kütüphane ve simgeler: önce önbellek, arka planda güncelleme. */
const CACHE = 'epa-eaa8e83f6351';
const FILES = ['./', './index.html', './manifest.webmanifest', './apple-touch-icon.png', './icon-192.png', './icon-512.png', './lib/jspdf.umd.min.js', './lib/html2canvas.min.js'];
self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES.map(f => new Request(f, {cache: 'reload'})))).then(() => self.skipWaiting()));
});
self.addEventListener('activate', e => {
  e.waitUntil(caches.keys().then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k)))).then(() => self.clients.claim()));
});
const isPage = (r, u) => r.mode === 'navigate' || u.pathname.endsWith('/') || u.pathname.endsWith('/index.html');
self.addEventListener('fetch', e => {
  const r = e.request;
  if (r.method !== 'GET') return;
  const u = new URL(r.url);
  if (u.origin !== location.origin) return;
  if (isPage(r, u)) {
    e.respondWith((async () => {
      const c = await caches.open(CACHE);
      try {
        const res = await fetch(r, {cache: 'no-store'});
        if (res && res.ok) c.put('./index.html', res.clone());
        return res;
      } catch (err) {
        return (await c.match('./index.html')) || new Response('Çevrimdışı', {status: 503});
      }
    })());
    return;
  }
  e.respondWith(caches.open(CACHE).then(async c => {
    const hit = await c.match(r, {ignoreSearch: true});
    const net = fetch(r).then(res => { if (res && res.ok) c.put(r, res.clone()); return res; }).catch(() => null);
    return hit || (await net) || new Response('', {status: 504});
  }));
});
