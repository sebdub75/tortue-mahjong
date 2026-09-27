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

Projet en phase de cadrage : aucun code pour l'instant.
