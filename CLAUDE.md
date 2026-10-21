# Mahjong solitaire Android

Jeu de Mahjong solitaire à un joueur, façon *Mahjong HD* : pyramide « tortue » de 144
tuiles sur 5 niveaux, on retire des paires de tuiles identiques et libres. Ce n'est pas
un Mahjong à plusieurs joueurs.

- Objectif et règles : [README.md](README.md)
- Décisions d'architecture : `docs/decisions/` (une fiche numérotée par décision ; en
  ajouter une pour chaque choix structurant)
- Technologie : Kotlin + Jetpack Compose, règles dans un module `:core` en Kotlin pur
  testé avec JUnit (voir [0001](docs/decisions/0001-choix-kotlin-compose.md))
- Tuiles : jeu SVG FluffyStuff (CC0), fleurs et saisons générées dans le même style ;
  sources dans `art/tiles/` (voir [0002](docs/decisions/0002-tuiles-fluffystuff.md))
- Rendu : fausse 3D en perspective cavalière dans le `Canvas`, `app/.../TileRenderer.kt`
  (voir [0003](docs/decisions/0003-rendu-tuiles-en-relief.md)) ; démo : `./demo.sh`
- Application en paysage (voir [0004](docs/decisions/0004-orientation-paysage.md))
- Règles : `core/` (`Face`, `Board`, `Layouts`, `Dealer`), coordonnées en demi-tuiles ;
  tests : `./gradlew :core:test`

Documentation rédigée en français.
