# Google Play Mağaza Listesi — Calistenic

Bu dosya, Play Console'da mağaza listesini doldururken kopyala-yapıştır
yapabileceğiniz hazır metinleri ve gerekli görsel/teknik bilgileri içerir.

---

## Uygulama Adı (max 30 karakter)
```
Calistenic
```
> İsteğe bağlı daha açıklayıcı alternatif: `Calistenic – Antrenman Takip`

## Kısa Açıklama (max 80 karakter)
```
Calisthenics antrenmanlarını planla, set/tekrar takip et, dinlenme sayacı ile çalış.
```
> Not: 80 karakteri aşarsa şu kısaltmayı kullanın:
> `Antrenmanlarını planla, set ve tekrarları takip et, dinlenme sayacıyla çalış.`

## Tam Açıklama (max 4000 karakter)
```
Calistenic, vücut ağırlığı (calisthenics) antrenmanlarını basit ve hızlı şekilde
takip etmen için tasarlanmış, tamamen çevrimdışı çalışan bir uygulamadır.

▸ ANTRENMAN EKLE
Hareket adını gir, set ve tekrar sayılarını belirle, istediğin tarihe kaydet.
Aynı güne dilediğin kadar hareket ekleyebilirsin.

▸ AKILLI DİNLENME SAYACI
Setler ve hareketler arası dinlenme sürelerini ayarla. Sayaç arka planda da
çalışır; süre dolduğunda bildirim ve titreşimle seni uyarır.

▸ GEÇMİŞİNİ GÖR
Antrenman geçmişin tarihe göre düzenlenir. Bir güne dokun, o günün tüm
hareketlerini ve toplam tekrarlarını gör.

▸ TAMAMEN ÇEVRİMDIŞI & GİZLİ
İnternet gerektirmez. Hiçbir veri toplanmaz, sunucuya gönderilmez. Tüm
verilerin yalnızca senin cihazında saklanır.

▸ MODERN TASARIM
Material 3 tasarım dili, açık ve koyu tema desteği.

Reklamsız. Takipsiz. Sadece antrenman.
```

---

## Kategorizasyon
- **Uygulama türü:** Uygulama
- **Kategori:** Sağlık ve Fitness
- **Etiketler:** fitness, antrenman, calisthenics, egzersiz, spor

## İletişim Bilgileri
- **E-posta:** sami.ozsoy@n2mobil.com.tr
- **Gizlilik Politikası URL'si:** _(PRIVACY_POLICY.md dosyasını bir yere host edin —
  örn. GitHub Pages / Gist ham bağlantısı — ve URL'yi buraya girin)_

---

## Gerekli Görseller (playstore/ klasöründe hazır)
| Öğe | Boyut | Dosya |
|-----|-------|-------|
| Uygulama ikonu | 512×512 PNG | `playstore/play_store_icon_512.png` |
| Öne çıkan görsel (Feature graphic) | 1024×500 PNG | `playstore/feature_graphic_1024x500.png` |
| Telefon ekran görüntüleri | min. 2 adet, 16:9 veya 9:16 | _Uygulamadan alın_ |

> Ekran görüntüleri: Play en az 2, en fazla 8 telefon ekran görüntüsü ister
> (1080×1920 önerilir). Depodaki `calistenic-*.png` görselleri başlangıç için
> kullanılabilir; ideal olan emülatör/cihazdan temiz çekimlerdir.

---

## Data Safety (Veri Güvenliği) Formu — Cevaplar
Play Console "Veri güvenliği" bölümünde şu şekilde doldurun:

- **Uygulamanız veri topluyor veya paylaşıyor mu?** → **Hayır**
- **Veriler aktarım sırasında şifreleniyor mu?** → Uygulanmaz (veri toplanmıyor)
- **Kullanıcılar verilerinin silinmesini isteyebilir mi?** → Veriler cihazda;
  uygulamayı kaldırınca silinir.

---

## ÖNEMLİ: Foreground Service (specialUse) Gerekçesi
Uygulama, dinlenme sayacının ekran kapalıyken/arka plandayken doğru çalışması için
`FOREGROUND_SERVICE_SPECIAL_USE` iznini kullanır. Play Console bu izni
incelemede gerekçe ister. **App content > Foreground service** bölümünde şunu girin:

```
Uygulama, antrenman setleri arasındaki dinlenme süresini saniye hassasiyetinde
ölçen bir geri sayım sayacı sunar. Kullanıcı uygulamayı arka plana aldığında veya
ekranı kapattığında bile sayacın kesintisiz çalışması ve süre dolduğunda bildirim
ile uyarması gerekir. Bu işlev, mevcut WorkManager/AlarmManager API'leri ile
saniye hassasiyetinde sürekli güncellenen bir sayaç olarak güvenilir biçimde
sağlanamadığından ön plan hizmeti (foreground service) kullanılmaktadır. Hizmet
yalnızca sayaç aktifken çalışır ve sayaç bitince durdurulur.
```

> NOT: Google bu izni reddederse, sayacı `setExactAndAllowWhileIdle` (AlarmManager)
> + bildirim yaklaşımına çevirmek alternatif çözümdür.

---

## Sürüm Bilgileri
- **applicationId:** `com.samiozsoy.calistenic`
- **versionName:** `1.0.0`
- **versionCode:** `1`
- **minSdk:** 26 (Android 8.0) — **targetSdk:** 35 (Android 15)

## Derleme (imzalı AAB)
```bash
./gradlew bundleRelease
# Çıktı: app/build/outputs/bundle/release/app-release.aab
```
İmzalama bilgileri `keystore.properties` dosyasından okunur (depoya dahil değildir).
