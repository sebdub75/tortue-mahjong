# 0013 — Solution garantie en mode difficile

- **Date** : 2026-10-25
- **Statut** : accepté ; modifie [0009](0009-mode-difficile.md)

## Contexte

En mode difficile, `Dealer.dealRandom` tire la tortue au hasard sans garantie de
solution (voir 0009). Le joueur qui perd ne sait pas s'il a mal joué ou si la donne
était perdue d'avance. L'étude de faisabilité `poc/donnes-solubles/` montre qu'on peut
ne garder que les tirages qu'un solveur sait vider : avec un budget de 50 000
plateaux par tirage, 70 % des tirages sont gardés, en ~33 ms par donne gardée sur PC,
et ces donnes restent environ 4 fois plus piégeuses que les donnes normales.

## Décision

- `Dealer.dealSolvable(layout, random)` tire des donnes comme `dealRandom` et renvoie
  la première que `Solver` vide en `Dealer.SOLVER_BUDGET` (50 000) plateaux au plus,
  sous forme de `Deal`, avec la solution trouvée. Au-delà de 1 000 tirages rejetés,
  il lève une `IllegalArgumentException`, comme `deal`.
- Une option « Solution garantie », cochée par défaut, apparaît sous « Difficile »
  quand ce dernier est coché (`Settings.guaranteed`). Elle est sans objet en mode
  normal, dont les donnes sont toujours solubles.
- Le mode d'une partie devient une énumération `Mode` : `NORMAL`, `HARD` (difficile,
  solution garantie), `HARD_UNCHECKED` (difficile sans garantie, le comportement de
  0009). `Settings.mode` donne celui des prochaines donnes ; la colonne de commandes
  affiche celui de la partie en cours.
- La donne garantie est calculée hors du thread UI (`Dispatchers.Default`). Un voile
  « Distribution… » couvre l'écran pendant le calcul et absorbe les touchers.
- La question posée en sortant des options compare désormais les modes : passer de
  « difficile » à « difficile sans garantie » propose aussi une nouvelle donne.
- `SavedGame` enregistre le nom du mode (`mode`). Une sauvegarde plus ancienne, qui
  n'a que le booléen `hard`, est relue en `NORMAL` ou `HARD_UNCHECKED` : ses donnes
  difficiles n'avaient pas été vérifiées.

## Raisons

- Le solveur et le tirage filtré sont des règles de distribution : ils vivent dans
  `:core` et se testent en JUnit (`DealerTest`, `SolverTest`).
- Budget de 50 000 plateaux, recommandé par l'étude : chaque tirage est plafonné, donc
  le pire cas est borné ; les donnes jetées sont surtout insolubles ou non décidées.
- Option cochée par défaut : perdre une partie insoluble d'avance n'apprend rien au
  joueur. Elle reste décochable pour qui veut le tirage brut.
- Calcul en tâche de fond plutôt que sur place : sur téléphone, une donne peut prendre
  quelques centaines de millisecondes, assez pour figer l'écran.

## Conséquences

- En mode difficile garanti, toute donne a une solution ; le message « Plus aucune
  paire jouable » signifie alors que le joueur s'est piégé, et « Annuler » permet de
  revenir en arrière.
- Le filtre écarte aussi des donnes solubles mais très dures (15 % des tirages,
  résolues entre 50 000 et 1 million de plateaux) : le mode difficile garanti est un
  peu plus facile que le tirage brut.
- La solution trouvée n'est pas encore utilisée par l'appli ; elle pourrait servir à
  une aide « coup suivant de la solution ».
- Au premier lancement, sans sauvegarde, la donne est distribuée sur place : le mode
  est alors normal, sauf sauvegarde illisible.
- Le temps de distribution n'a pas encore été mesuré sur un vrai téléphone.
