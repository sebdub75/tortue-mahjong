# Mahjong solitaire Android

Jeu de Mahjong solitaire à un joueur, façon *Mahjong HD* : pyramide « tortue » de 144
tuiles sur 5 niveaux, on retire des paires de tuiles identiques et libres. Ce n'est pas
un Mahjong à plusieurs joueurs.

- Objectif et règles : [README.md](README.md)
- Décisions d'architecture : `docs/decisions/` (une fiche numérotée par décision ; en
  ajouter une pour chaque choix structurant)
- Technologie : Kotlin + Jetpack Compose, règles dans un module `:core` en Kotlin pur
  testé avec JUnit (voir [0001](docs/decisions/0001-choix-kotlin-compose.md))
- Tuiles : jeu SVG FluffyStuff (CC0), fleurs et saisons illustrées dans le même style
  par `art/tiles/fleurs-saisons/generer.js` ; sources dans `art/tiles/` (voir
  [0002](docs/decisions/0002-tuiles-fluffystuff.md),
  [0010](docs/decisions/0010-fleurs-saisons-illustrees.md))
- Rendu : fausse 3D en perspective cavalière dans le `Canvas`, `app/.../TileRenderer.kt`
  (voir [0003](docs/decisions/0003-rendu-tuiles-en-relief.md)) ; démo : `./demo.sh`
- Application en paysage (voir [0004](docs/decisions/0004-orientation-paysage.md))
- Composition du jeu : celle de *Mahjong HD*, vents, fleurs et saisons en 2 exemplaires
  (voir [0005](docs/decisions/0005-composition-du-jeu.md))
- Icône : paire de 中 en relief, icône adaptative vectorielle générée par
  `node art/icone/generer.js` (voir [0006](docs/decisions/0006-icone-application.md))
- Règles : `core/` (`Face`, `Board`, `Layouts`, `Dealer`, `Game`), coordonnées en demi-tuiles ;
  tests : `./gradlew :core:test`
- Annulation : `Game` garde la pile des plateaux précédents
  (voir [0007](docs/decisions/0007-annulation-des-coups.md))
- Options : panneau `OptionsPanel` par-dessus le plateau, réglages dans `Settings`
  (`SharedPreferences`) (voir [0008](docs/decisions/0008-ecran-options.md))
- Mode difficile : donne tirée au hasard ; option « Solution garantie » (cochée par
  défaut) qui ne garde que les tirages vidés par `Solver` (`Dealer.dealSolvable`,
  calculé hors du thread UI), sinon `Dealer.dealRandom` ; quatre modes dans `Mode` ; à
  la sortie des options, nouvelle donne proposée si le mode a changé (voir
  [0009](docs/decisions/0009-mode-difficile.md),
  [0013](docs/decisions/0013-solution-garantie.md), étude `poc/donnes-solubles/`)
- Mode expert : sous « Solution garantie », option « Difficulté garantie » ; donne prise
  parmi des graines calculées à l'avance (`ExpertSeeds`, `ExpertSeedList.kt` généré par
  `poc/donnes-expert/generer.sh`), servie par `Dealer.dealExpert` (voir
  [0014](docs/decisions/0014-mode-expert.md))
- Aide : bouton « Aide » qui montre en bleu, appui après appui, chaque paire de
  `Board.playablePairs(first)` (groupées par motif, celles de la tuile sélectionnée
  d'abord), rang dans `GameState.hint` (voir
  [0011](docs/decisions/0011-aide-coups-possibles.md))
- Sauvegarde : `SavedGame` enregistre à chaque coup `Game.encode()` (donne + coups),
  reprise au lancement par `Game.decode()` (voir
  [0012](docs/decisions/0012-sauvegarde-de-la-partie.md))

Documentation rédigée en français.
