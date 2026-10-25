# 0014 — Mode expert : donnes choisies à l'avance

- **Date** : 2026-10-25
- **Statut** : accepté ; complète [0013](0013-solution-garantie.md)

## Contexte

Les donnes du mode difficile garanti (0013) sont solubles, mais leur difficulté est très
inégale : un joueur au hasard en gagne 8,8 % en moyenne, mais certaines plus de 20 fois
sur 100. Donner un budget plus grand au solveur ne les durcirait guère. Choisir les
donnes dures sur téléphone serait trop lent : il faut résoudre chaque tirage avec un
gros budget et jouer des centaines de parties. L'étude `poc/donnes-expert/` mesure la
répartition de la difficulté et fixe le seuil.

## Décision

- Une option « Difficulté garantie », décochée par défaut, apparaît sous « Solution
  garantie » quand celle-ci est cochée (`Settings.expert`). Elle donne un quatrième
  mode, `Mode.EXPERT`.
- Les donnes expert sont calculées à l'avance sur PC : `poc/donnes-expert/generer.sh`
  écrit dans `ExpertSeedList.kt` les 2 000 premières graines dont la tortue au hasard
  est soluble en un million de plateaux et gagnée au plus 1 fois sur 100 par un joueur
  au hasard (400 parties). Les seuils sont dans `ExpertSeeds`.
- `Dealer.dealExpert(random)` tire une graine de la liste et renvoie
  `dealRandom(TURTLE, Random(graine))`.

## Raisons

- Le générateur de `kotlin.random.Random` est écrit en Kotlin pur : une graine donne la
  même tortue sur PC et sur téléphone. Livrer des graines plutôt que des plateaux ne
  coûte que quelques kilo-octets.
- Hors ligne, on mesure la difficulté elle-même, par des parties au hasard, au lieu de
  la déduire du temps du solveur.
- Seuil de 1 % : le tiers le plus dur des donnes solubles, environ 9 fois plus dur que
  le mode difficile garanti. 2 % est presque la médiane ; 0 % ne garde que des donnes
  à chemin très étroit.
- Les graines sont dans un fichier Kotlin généré plutôt qu'en ressource : rien à lire
  au lancement, et l'empaquetage Android n'entre pas en jeu.

## Conséquences

- La donne expert est instantanée, sans voile « Distribution… ».
- 2 000 donnes : un joueur peut retomber sur une donne déjà vue, mais rarement.
- Modifier `Dealer.dealRandom`, `Face.fullSet()` ou l'ordre de `Layouts.TURTLE` change
  les tortues des graines : il faut relancer `generer.sh`. `ExpertTest` revérifie les
  premières graines.
- La difficulté n'est mesurée que par un joueur au hasard ; elle reste à confirmer par
  de vraies parties.
