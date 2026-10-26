# 0019 — Publication par GitHub Actions

- **Date** : 2026-10-26
- **Statut** : accepté

## Contexte

Le code est publié sur GitHub. Les joueurs ont besoin d'un APK signé à télécharger, et
toujours signé par la même clé : Android refuse une mise à jour signée par une autre.

## Décision

Deux workflows dans `.github/workflows/` :

- `build.yml` : à chaque poussée sur `main` et à chaque pull request, lance
  `./gradlew :core:test :app:assembleDebug` ;
- `release.yml` : à la poussée d'une étiquette `vX.Y`, vérifie qu'elle correspond à
  `versionName`, lance les tests, signe l'APK avec `apk.sh` et le dépose dans une
  release GitHub (`gh release create`, notes générées depuis les commits).

La clé de signature et son mot de passe sont des secrets du dépôt
(`MAHJONG_KEYSTORE_BASE64`, `MAHJONG_KEYSTORE_PASS`). Le workflow les passe à `apk.sh`
par ses variables `MAHJONG_KEYSTORE` et `MAHJONG_KEYSTORE_PASS`, puis efface la clé.

## Raisons

- Réutiliser `apk.sh` garantit que l'APK de la CI est fait comme l'APK local.
- Sans les secrets, `apk.sh` créerait une clé neuve. Le workflow s'arrête donc s'ils
  manquent, au lieu de publier un APK impossible à installer en mise à jour.
- Une release GitHub sert aussi de source à IzzyOnDroid, qui reprend l'APK tel quel.
- Seules des actions officielles (`actions/*`, `gradle/actions`) sont utilisées ;
  la publication passe par `gh`, préinstallé, plutôt que par une action tierce.

## Conséquences

- La clé privée est confiée à GitHub. Quiconque a les droits d'administration sur le
  dépôt peut publier un APK signé. Alternative si ce n'est pas acceptable : retirer la
  signature du workflow, signer en local avec `apk.sh` et déposer l'APK à la main.
- Chaque version doit augmenter `versionCode`, sans quoi les magasins d'applications
  (F-Droid, IzzyOnDroid) n'y voient pas de mise à jour.
