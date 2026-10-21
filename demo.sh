#!/usr/bin/env bash
# Construit la maquette, démarre un émulateur si aucun appareil n'est branché,
# installe l'application et la lance.
#
# Usage : ./demo.sh [AVD]      (AVD par défaut : Pixel_8)
set -euo pipefail

cd "$(dirname "$0")"

AVD="${1:-Pixel_8}"
PACKAGE=name.dubourg.mahjong
APK=app/build/outputs/apk/debug/app-debug.apk

export JAVA_HOME="${JAVA_HOME:-/opt/android-studio/jbr}"
SDK="${ANDROID_HOME:-$(sed -n 's/^sdk\.dir=//p' local.properties 2>/dev/null)}"
SDK="${SDK:-$HOME/Android/Sdk}"
ADB="$SDK/platform-tools/adb"
EMULATOR="$SDK/emulator/emulator"

echo "== Construction"
./gradlew -q :app:assembleDebug

if ! "$ADB" devices | grep -q 'device$'; then
    echo "== Démarrage de l'émulateur $AVD"
    nohup "$EMULATOR" -avd "$AVD" -no-audio -no-boot-anim >/dev/null 2>&1 &
    "$ADB" wait-for-device
    until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; do
        sleep 2
    done
fi

echo "== Installation"
if ! "$ADB" install -r "$APK" 2>&1 | tee /dev/stderr | grep -q '^Success'; then
    # Signature différente (build fait sur une autre machine ou avec une autre clé) :
    # on repart d'une installation propre.
    echo "Installation refusée, désinstallation puis nouvel essai"
    "$ADB" uninstall "$PACKAGE" >/dev/null || true
    "$ADB" install "$APK"
fi

echo "== Lancement"
"$ADB" shell am start -n "$PACKAGE/.MainActivity" >/dev/null
