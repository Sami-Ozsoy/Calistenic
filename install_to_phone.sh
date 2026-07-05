#!/usr/bin/env bash

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
APK_PATH="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"

if ! command -v adb >/dev/null 2>&1; then
    echo "Hata: adb bulunamadı."
    echo "Kurulum: brew install --cask android-platform-tools"
    exit 1
fi

if [[ ! -f "$APK_PATH" ]]; then
    echo "Hata: APK bulunamadı: $APK_PATH"
    echo "Önce projeyi Android Studio'da derleyin veya ./gradlew assembleDebug çalıştırın."
    exit 1
fi

adb start-server >/dev/null

DEVICE_LINES="$(adb devices | tail -n +2 | sed '/^[[:space:]]*$/d')"

if [[ -z "$DEVICE_LINES" ]]; then
    echo "Telefon bulunamadı."
    echo "1. Telefonu veri aktarabilen USB kablosuyla bağlayın."
    echo "2. Geliştirici seçenekleri > USB hata ayıklama ayarını açın."
    echo "3. Telefonda çıkan USB hata ayıklama iznini onaylayın."
    echo "4. Bu dosyayı tekrar çalıştırın: ./install_to_phone.sh"
    exit 2
fi

if echo "$DEVICE_LINES" | grep -q "unauthorized"; then
    echo "Telefon bağlı fakat yetki verilmemiş."
    echo "Telefon ekranındaki 'USB hata ayıklamasına izin verilsin mi?' sorusunu onaylayın."
    exit 3
fi

READY_COUNT="$(echo "$DEVICE_LINES" | awk '$2 == "device" { count++ } END { print count+0 }')"

if [[ "$READY_COUNT" -eq 0 ]]; then
    echo "Bağlı telefon kurulum için hazır değil:"
    echo "$DEVICE_LINES"
    exit 4
fi

if [[ "$READY_COUNT" -gt 1 ]]; then
    echo "Birden fazla Android cihaz bağlı. Yalnızca hedef telefonu bağlı bırakın."
    echo "$DEVICE_LINES"
    exit 5
fi

echo "Calistenic telefona yükleniyor..."
adb install -r "$APK_PATH"
echo "Kurulum tamamlandı. Telefonda uygulamalar listesinden Calistenic'i açabilirsiniz."

