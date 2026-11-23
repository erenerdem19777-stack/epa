"""iPhone web uygulamasını app/src/main/assets/index.html'den üretir (site/ klasörü)."""
import os, shutil, sys
ver = (sys.argv[1] if len(sys.argv) > 1 else 'dev')[:12]
os.makedirs('site/lib', exist_ok=True)
h = open('app/src/main/assets/index.html', encoding='utf-8').read()
head = ('<script>window.EPA_PWA=1</script>\n'
        '<link rel="manifest" href="manifest.webmanifest">\n'
        '<link rel="apple-touch-icon" href="apple-touch-icon.png">\n'
        '<meta name="apple-mobile-web-app-capable" content="yes">\n'
        '<meta name="mobile-web-app-capable" content="yes">\n'
        '<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">\n'
        '<meta name="apple-mobile-web-app-title" content="EPA">\n'
        '<meta name="theme-color" content="#111111">\n'
        '<meta name="format-detection" content="telephone=no">\n'
        '<meta name="description" content="EP Academy Sport & Performance · akademi yönetim uygulaması. iPhone\'da Safari ile açıp Paylaş › Ana Ekrana Ekle.">\n'
        '<meta property="og:type" content="website">\n'
        '<meta property="og:site_name" content="EPA">\n'
        '<meta property="og:title" content="EPA · EP Academy Sport & Performance">\n'
        '<meta property="og:description" content="Akademi yönetim uygulaması. iPhone\'da Safari ile açın, Paylaş › Ana Ekrana Ekle.">\n'
        '<meta property="og:url" content="https://erenerdem19777-stack.github.io/epa/">\n'
        '<meta property="og:image" content="https://erenerdem19777-stack.github.io/epa/og-image.png">\n'
        '<meta property="og:image:width" content="1200">\n'
        '<meta property="og:image:height" content="630">\n'
        '<meta name="twitter:card" content="summary_large_image">\n')
assert '<title>' in h
h = h.replace('<title>', head + '<title>', 1)
open('site/index.html', 'w', encoding='utf-8').write(h)
for f in ('manifest.webmanifest', 'apple-touch-icon.png', 'icon-192.png', 'icon-512.png', 'icon-maskable-512.png', 'og-image.png'):
    shutil.copy('pwa/' + f, 'site/' + f)
open('site/sw.js', 'w').write(open('pwa/sw.js').read().replace('__VERSION__', ver))
for f in ('jspdf.umd.min.js', 'html2canvas.min.js'):
    shutil.copy('app/src/main/assets/lib/' + f, 'site/lib/' + f)
open('site/.nojekyll', 'w').write('')
print('site ready', ver)
