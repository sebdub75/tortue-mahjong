# Tortue Mahjong — Mahjong solitaire (Android)

Jeu de Mahjong solitaire pour Android, à un seul joueur, dans l'esprit de *Mahjong HD*.

## Esprit du projet

Tortue Mahjong veut rester **simple** : une seule disposition, la tortue, et peu
d'options. Pour un jeu plus complet (nombreuses dispositions, thèmes, défis…),
*Mahjong HD* est recommandé.

Son originalité tient à son **solveur**, qui cherche une solution à chaque donne :

- **Solution garantie, même au hasard** : en mode difficile, la tortue est tirée
  vraiment au hasard, et non construite à rebours comme les donnes faciles, ce qui
  donne des parties bien plus piégeuses. Le solveur ne garde que les tirages qu'il sait
  vider. Si l'on se retrouve bloqué, c'est qu'on s'est piégé, pas que la donne était
  perdue d'avance, et « Annuler » permet de retrouver le bon chemin.
- **Mode expert, difficile mais soluble** : des donnes choisies à l'avance parmi des
  milliers de tirages, toutes solubles, mais qu'un joueur jouant au hasard ne gagne
  qu'une fois sur cent au plus. Elles sont environ neuf fois plus dures que celles du
  mode difficile.

Détails : [0013](docs/decisions/0013-solution-garantie.md) et
[0014](docs/decisions/0014-mode-expert.md).

## Principe du jeu

- Les 144 tuiles sont empilées selon la disposition classique « tortue » : une pyramide
  sur 5 niveaux.
- Le joueur retire des **paires de tuiles identiques** jusqu'à vider le plateau.
  Deux fleurs quelconques s'apparient, de même deux saisons.
- Composition du jeu, comme dans *Mahjong HD* : caractères, cercles et bambous (1 à 9)
  et dragons en 4 exemplaires ; vents, fleurs et saisons en 2 exemplaires. Chaque vent
  n'a donc qu'une seule partenaire. Voir
  [0005](docs/decisions/0005-composition-du-jeu.md).
- Une tuile ne peut être jouée que si elle est **libre** :
  - aucune tuile n'est posée dessus ;
  - son côté gauche **ou** son côté droit est dégagé.
- La partie est gagnée quand le plateau est vide, perdue quand plus aucune paire libre
  n'est disponible.

## Technologie retenue

**Kotlin + Jetpack Compose**, avec le plateau dessiné dans un `Canvas`.

Détail et alternatives écartées : [docs/decisions/0001-choix-kotlin-compose.md](docs/decisions/0001-choix-kotlin-compose.md).

## État

Première version jouable : une donne de tortue, soluble en mode normal, en paysage. On
touche deux tuiles libres qui s'apparient pour les retirer ; les tuiles bloquées sont
assombries. Compteurs de tuiles et de paires jouables, bouton « Nouvelle donne »,
bouton « Annuler » qui défait les coups un à un jusqu'à la donne, message en fin de
partie. Écran d'options avec une case « Difficile » : les donnes sont alors tirées au
hasard ; avec « Solution garantie », cochée par défaut, seules sont gardées celles
qu'un solveur sait vider ; avec « Difficulté garantie », les donnes sont prises parmi
des tortues solubles et très piégeuses choisies à l'avance. Bouton « Aide » qui montre,
appui après appui, chacune des paires jouables. La partie en cours est enregistrée à chaque coup.
Pas encore de mélange.

Les règles sont dans le module `:core`, testé avec JUnit (`./gradlew :core:test`).

Lancer la démo : `./demo.sh` construit l'application, démarre l'émulateur `Pixel_8` (ou
celui passé en argument) si aucun appareil n'est branché, puis installe et lance l'app.

APK pour un téléphone : `./apk.sh` produit `dist/mahjong-<version>.apk`, en version
release signée. Au premier lancement, la clé est créée dans
`~/.android/mahjong-release.jks`, avec un mot de passe aléatoire dans
`~/.android/mahjong-release.pass` (lisibles par vous seul). Sauvegarder ces deux
fichiers : sans eux, une nouvelle version ne s'installe plus par-dessus l'ancienne.

## Publication

GitHub Actions lance les tests et construit l'APK debug à chaque poussée sur `main`
(`.github/workflows/build.yml`). Pour publier une version :

1. augmenter `versionCode` et `versionName` dans `app/build.gradle.kts` ;
2. poser et pousser l'étiquette correspondante : `git tag v0.2 && git push origin v0.2`.

Le workflow `release.yml` vérifie que l'étiquette correspond à `versionName`, signe
l'APK avec `apk.sh` et le dépose dans une release GitHub. Il lui faut deux secrets du
dépôt : `MAHJONG_KEYSTORE_BASE64` (sortie de `base64 -w0 ~/.android/mahjong-release.jks`)
et `MAHJONG_KEYSTORE_PASS` (contenu de `~/.android/mahjong-release.pass`). Voir
[0019](docs/decisions/0019-publication-github-actions.md).

## Licence

Le code et les illustrations propres au projet sont distribués sous **licence publique
générale GNU, version 3 ou toute version ultérieure** (GPL-3.0-or-later) : texte complet
dans [LICENSE](LICENSE). Voir [0018](docs/decisions/0018-licence-gpl.md).

## Crédits

- **Tuiles** : jeu [riichi-mahjong-tiles](https://github.com/FluffyStuff/riichi-mahjong-tiles)
  de FluffyStuff, versé au domaine public (CC0 1.0). Fichiers copiés sans modification
  dans `art/tiles/fluffystuff/`.
- **Fleurs, saisons et icône** : dessinées pour le projet, dans le style FluffyStuff,
  par `art/tiles/fleurs-saisons/generer.js` et `art/icone/generer.js`.
- **Bibliothèques** : [Kotlin](https://kotlinlang.org/),
  [Jetpack Compose et AndroidX](https://developer.android.com/jetpack) (Apache 2.0) ;
  tests avec [JUnit 4](https://junit.org/junit4/) (EPL 1.0).
- **Conversion des tuiles en VectorDrawable** (outils de développement, absents de
  l'APK) : [svg2vectordrawable](https://github.com/Ashung/svg2vectordrawable) d'Ashung
  Hung (MIT), [svgpath](https://github.com/fontello/svgpath) (MIT),
  [svg-path-properties](https://github.com/rveciana/svg-path-properties) de Roger
  Veciana i Rovira (ISC), [@xmldom/xmldom](https://github.com/xmldom/xmldom) (MIT).
- **Inspiration** : *Mahjong HD*, dont le projet reprend la disposition et la
  composition du jeu. Projet indépendant, sans lien avec les auteurs de *Mahjong HD*.
