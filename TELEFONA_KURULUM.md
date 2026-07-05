# Calistenic uygulamasını telefona yükleme

Android Studio zorunlu değildir. Hazır APK dosyası `adb` kullanılarak doğrudan
telefona yüklenebilir.

## Bilgisayarda hazır olanlar

- Android Platform Tools (`adb`) kuruldu.
- Yüklenecek APK:
  `app/build/outputs/apk/debug/app-debug.apk`
- Otomatik yükleme komutu:
  `./install_to_phone.sh`

## Telefonda bir kez yapılacak ayarlar

Menü isimleri telefon markasına göre biraz değişebilir.

1. **Ayarlar > Telefon hakkında** bölümünü açın.
2. **Yapım numarası** alanına art arda 7 kez dokunun.
   - Samsung'da bu alan genellikle **Yazılım bilgileri** içindedir.
   - Xiaomi'de **MIUI/HyperOS sürümü** alanına dokunmak gerekebilir.
3. Telefon şifresini girin. “Artık geliştiricisiniz” mesajı görünür.
4. Ayarlara geri dönüp **Geliştirici seçenekleri** bölümünü açın.
5. **USB hata ayıklama** seçeneğini etkinleştirin.

## USB ile yükleme

1. Telefonun kilidini açın.
2. Telefonu veri aktarabilen bir USB kablosuyla Mac'e bağlayın.
3. USB bağlantı türü sorulursa **Dosya aktarımı** seçin.
4. Telefonda “USB hata ayıklamasına izin verilsin mi?” penceresi çıkarsa
   **Bu bilgisayara her zaman izin ver** ve ardından **İzin ver** seçin.
5. Bu proje klasöründe Terminal açıp çalıştırın:

```bash
./install_to_phone.sh
```

Kurulum tamamlanınca telefondaki uygulamalar listesinde **Calistenic** görünür.

## Kurulum engellenirse

Telefon “USB üzerinden uygulama yükleme” veya benzeri bir onay gösterebilir.
Bu onayı verip komutu tekrar çalıştırın. Google Play Protect bir tarama
gösterirse APK bu bilgisayarda yerel olarak oluşturulduğu için taramayı
tamamlayıp kuruluma devam edebilirsiniz.

## Android Studio ne zaman gerekir?

Kod üzerinde değişiklik yapmak, yeni APK üretmek veya Android emülatörü
kullanmak için Android Studio önerilir. Sadece mevcut APK'yı telefona yüklemek
için gerekli değildir.

