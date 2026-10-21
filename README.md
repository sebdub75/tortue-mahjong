# Mahjong solitaire (Android)

Jeu de Mahjong solitaire pour Android, à un seul joueur, dans l'esprit de *Mahjong HD*.

## Principe du jeu

- Les 144 tuiles sont empilées selon la disposition classique « tortue » : une pyramide
  sur 5 niveaux.
- Le joueur retire des **paires de tuiles identiques** jusqu'à vider le plateau.
- Une tuile ne peut être jouée que si elle est **libre** :
  - aucune tuile n'est posée dessus ;
  - son côté gauche **ou** son côté droit est dégagé.
- La partie est gagnée quand le plateau est vide, perdue quand plus aucune paire libre
  n'est disponible.

## Technologie retenue

**Kotlin + Jetpack Compose**, avec le plateau dessiné dans un `Canvas`.

Détail et alternatives écartées : [docs/decisions/0001-choix-kotlin-compose.md](docs/decisions/0001-choix-kotlin-compose.md).

## État

Maquette de rendu : le module `:app` affiche une petite pyramide de 21 tuiles en relief
(tranche, ombre portée, empilement sur 4 niveaux) ; toucher une tuile la sélectionne.
Le module `:core` contient les faces et leur appariement, la disposition « tortue » et
la règle de la tuile libre, testés avec JUnit (`./gradlew :core:test`). Il n'est pas
encore branché sur l'affichage. L'application est en paysage.

Lancer la démo : `./demo.sh` construit l'application, démarre l'émulateur `Pixel_8` (ou
celui passé en argument) si aucun appareil n'est branché, puis installe et lance l'app.
