# Calistenic

Kotlin, Jetpack Compose, Material 3, Room ve DataStore kullanan yerel calisthenics
antrenman takip uygulaması.

Antrenman eklerken tarih seçilebilir. Aynı güne istenen sayıda hareket
kaydedilebilir. Geçmiş ekranında normalde yalnızca tarihler görünür; bir tarihe
dokunulduğunda o günün antrenmanları açılır veya tekrar dokunulduğunda kapanır.

Hazır APK'yı Android Studio kurmadan telefona yüklemek için
[`TELEFONA_KURULUM.md`](TELEFONA_KURULUM.md) dosyasını izleyin.

## Android Studio'da açma

1. Android Studio'nun güncel kararlı sürümünü ve Android SDK 35'i kurun.
2. **File > Open** ile bu klasörü seçin.
3. Gradle JDK olarak **JDK 17** seçin.
4. Gradle eşitlemesinin tamamlanmasını bekleyin.
5. API 26 veya üstü bir emülatör/telefon seçip **Run** düğmesine basın.

İlk eşitlemede Gradle ve Android bağımlılıkları internetten indirilir. Uygulamanın
çalışması için internet gerekmez; bütün antrenmanlar Room ile cihazda, ayarlar
DataStore ile yerel olarak saklanır.

## Dosya yapısı

- `MainActivity.kt`: Uygulamanın giriş noktası ve ViewModel oluşturma kodu.
- `WorkoutEntity.kt`: Room'da saklanan antrenman tablosu.
- `WorkoutDao.kt`: Ekleme, listeleme ve silme sorguları.
- `WorkoutDatabase.kt`: Tekil Room veritabanı örneği.
- `WorkoutRepository.kt`: DAO ile ViewModel arasındaki veri katmanı.
- `SettingsRepository.kt`: Varsayılan tekrar ve dinlenme sürelerini DataStore'da saklar.
- `WorkoutViewModel.kt`: Antrenmanlar ve ayarlar için ekran durumunu yönetir.
- `TimerViewModel.kt`: Coroutine tabanlı başlat/duraklat/devam/sıfırla sayacı.
- `AddWorkoutScreen.kt`: Altı set, toplam, set takibi, dinlenme ve kayıt ekranı.
- `WorkoutHistoryScreen.kt`: Room kayıtlarını listeler ve siler.
- `SettingsScreen.kt`: Kalıcı varsayılan değerleri değiştirir.
- `TimerSection.kt`: Büyük geri sayım ve alarm kontrolleri.
- `AlarmController.kt`: Varsayılan alarm sesini döngüde çalar ve telefonu titreştirir.

## Mimari akış

Compose ekranı → ViewModel → Repository → Room/DataStore

Room sorguları `Flow` döndürür. Kayıt veya silme yapıldığında geçmiş ekranı
otomatik güncellenir. Veritabanı ve ayar işlemleri coroutine içinde çalışır.
