# EPA

Bireysel atletik performans merkezleri için futbol odaklı akademi yönetimi: oyuncu kütüğü, veli bilgileri, ödeme takvimi ve tahsilat, yoklama, bilimsel test bataryası (DFB, EPPP, Eurofit), büyüme ve biyolojik olgunlaşma (PHV) analizi, gelişim planı, sağlık ve sakatlık kaydı, veli raporu.

## APK
Her `main` gönderiminde GitHub Actions APK'yı derler:
- **Releases** bölümünde `EPA.apk` (etiket `v1.4.N`)
- `apk` dalında `EPA.apk`

Telefonda dosyayı açıp kurun (ilk seferde "bilinmeyen kaynaklardan yükleme" izni istenir).

## iPhone
App Store dışından iPhone'a uygulama kurulamadığı için EPA iPhone'da **web uygulaması** olarak çalışır. Her `main` gönderiminde `gh-pages` dalına yayımlanır. GitHub › Settings › Pages › Source: *Deploy from a branch*, Branch: `gh-pages` / `(root)` seçildiğinde adres **https://erenerdem19777-stack.github.io/epa/** olur. iPhone'da Safari ile açılır, **Paylaş › Ana Ekrana Ekle** ile logolu uygulama olarak eklenir. Kurallar, eşitleme dosyaları ve hesaplar Android ile aynıdır.

## Giriş ve yetkiler
İlk açılışta yönetici hesabı (kullanıcı adı + şifre) oluşturulur ve bir kurtarma kodu verilir. **Ayarlar › Kullanıcılar** bölümünden yardımcı eklenir; yardımcı yalnızca yoklama girer ve telefonuna sadece oyuncu adları ile grupları gider. Uygulama 10 dakika kullanılmazsa ya da 5 dakikadan uzun arka planda kalırsa kilitlenir.

## Muhasebe
Giderler (kategori, KDV, belge no, tedarikçi, ödendi/ödenecek), diğer gelirler, nakit kasa ve banka bakiyeleri, kasa-banka transferi, sabit gider şablonları, kategori bütçeleri, yıllık gelir-gider tablosu, KDV özeti ve muhasebeci için gelir-gider defteri (CSV). Aidat tahsilatları gelire kendiliğinden eklenir.

## İki telefon (sahip ve yardımcı)
**Eşitle** sekmesinde her telefona bir ad verin. Kayıt giren kişi **Kayıtları gönder** ile dosyayı WhatsApp'tan gönderir; diğeri dosyaya dokunup **EPA ile birleştir** der. Yeni kayıtlar eklenir, değişenlerde en son değiştirilen geçerli olur, silmeler de karşıya geçer.

## Veriler
APK sürümünde kayıtlar telefonda saklanır. Uygulama silinirse kayıtlar da silinir: **Ayarlar › Tam yedek (JSON)** ile düzenli yedek alın, yeni cihazda **Yedekten geri yükle** ile geri yükleyin.

Uygulamanın kaynağı: `app/src/main/assets/index.html`.
