# Mahjong solitaire (Android)

Jeu de Mahjong solitaire pour Android, à un seul joueur, dans l'esprit de *Mahjong HD*.

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
qu'un solveur sait vider. Bouton « Aide » qui montre, appui après appui, chacune des
paires jouables. La partie en cours est enregistrée à chaque coup.
Pas encore de mélange.

Les règles sont dans le module `:core`, testé avec JUnit (`./gradlew :core:test`).

Lancer la démo : `./demo.sh` construit l'application, démarre l'émulateur `Pixel_8` (ou
celui passé en argument) si aucun appareil n'est branché, puis installe et lance l'app.

APK pour un téléphone : `./apk.sh` produit `dist/mahjong-<version>.apk`, en version
release signée. Au premier lancement, la clé est créée dans
`~/.android/mahjong-release.jks`, avec un mot de passe aléatoire dans
`~/.android/mahjong-release.pass` (lisibles par vous seul). Sauvegarder ces deux
fichiers : sans eux, une nouvelle version ne s'installe plus par-dessus l'ancienne.
