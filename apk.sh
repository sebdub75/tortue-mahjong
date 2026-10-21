#!/usr/bin/env bash
# Construit un APK release signé, installable sur un téléphone (fichier dans dist/).
#
# Usage : ./apk.sh
#
# La clé de signature est créée au premier lancement, hors du dépôt :
#   $MAHJONG_KEYSTORE (par défaut ~/.android/mahjong-release.jks)
# avec un mot de passe aléatoire rangé à côté, lisible par vous seul :
#   $MAHJONG_KEYSTORE_PASSFILE (par défaut ~/.android/mahjong-release.pass)
# Il faut sauvegarder ces deux fichiers : sans eux, une nouvelle version ne pourra plus
# s'installer par-dessus l'ancienne (il faudra désinstaller, et perdre les données du jeu).
#
# Le mot de passe peut aussi venir de la variable MAHJONG_KEYSTORE_PASS.
set -euo pipefail

cd "$(dirname "$0")"

export JAVA_HOME="${JAVA_HOME:-/opt/android-studio/jbr}"
export PATH="$JAVA_HOME/bin:$PATH" # apksigner a besoin de java
SDK="${ANDROID_HOME:-$(sed -n 's/^sdk\.dir=//p' local.properties 2>/dev/null)}"
SDK="${SDK:-$HOME/Android/Sdk}"
BUILD_TOOLS="$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)"
KEYSTORE="${MAHJONG_KEYSTORE:-$HOME/.android/mahjong-release.jks}"
PASSFILE="${MAHJONG_KEYSTORE_PASSFILE:-${KEYSTORE%.jks}.pass}"
ALIAS=mahjong

if [ ! -f "$KEYSTORE" ] && [ -z "${MAHJONG_KEYSTORE_PASS:-}" ] && [ ! -f "$PASSFILE" ]; then
    echo "== Création du mot de passe $PASSFILE"
    mkdir -p "$(dirname "$PASSFILE")"
    (umask 077; head -c 24 /dev/urandom | base64 | tr -d '/+=' > "$PASSFILE")
fi

# Mot de passe : variable d'environnement, sinon fichier, sinon saisie au clavier.
if [ -n "${MAHJONG_KEYSTORE_PASS:-}" ]; then
    KEYTOOL_PASS=(-storepass:env MAHJONG_KEYSTORE_PASS)
    SIGNER_PASS=(--ks-pass env:MAHJONG_KEYSTORE_PASS)
elif [ -f "$PASSFILE" ]; then
    KEYTOOL_PASS=(-storepass:file "$PASSFILE")
    SIGNER_PASS=(--ks-pass "file:$PASSFILE")
else
    KEYTOOL_PASS=()
    SIGNER_PASS=()
fi

if [ ! -f "$KEYSTORE" ]; then
    echo "== Création de la clé de signature $KEYSTORE"
    mkdir -p "$(dirname "$KEYSTORE")"
    "$JAVA_HOME/bin/keytool" -genkeypair -keystore "$KEYSTORE" -alias "$ALIAS" \
        -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=Mahjong solitaire" \
        "${KEYTOOL_PASS[@]}"
    chmod 600 "$KEYSTORE"
    echo "Pensez à sauvegarder $KEYSTORE et $PASSFILE."
fi

echo "== Construction"
./gradlew -q :app:assembleRelease
UNSIGNED=app/build/outputs/apk/release/app-release-unsigned.apk

VERSION="$(sed -n 's/^ *versionName = "\(.*\)"/\1/p' app/build.gradle.kts)"
mkdir -p dist
OUT="dist/mahjong-$VERSION.apk"
ALIGNED="$(mktemp --suffix=.apk)"
trap 'rm -f "$ALIGNED"' EXIT

echo "== Signature"
"$BUILD_TOOLS/zipalign" -f -p 4 "$UNSIGNED" "$ALIGNED"
"$BUILD_TOOLS/apksigner" sign --ks "$KEYSTORE" --ks-key-alias "$ALIAS" \
    "${SIGNER_PASS[@]}" --out "$OUT" "$ALIGNED"
"$BUILD_TOOLS/apksigner" verify "$OUT"
rm -f "$OUT.idsig"

echo "== APK prêt : $OUT ($(du -h "$OUT" | cut -f1))"
echo "Installer sur un téléphone branché : adb install -r $OUT"
