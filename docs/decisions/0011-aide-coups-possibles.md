# 0011 — Aide : parcourir les coups possibles

- **Date** : 2026-10-23
- **Statut** : accepté ; complété le 2026-10-25 (paires de la tuile sélectionnée d'abord)

## Contexte

Le joueur doit pouvoir demander de l'aide : chaque appui sur un bouton montre une des
paires jouables, et des appuis successifs les passent toutes en revue.

## Décision

- Bouton « Aide » dans la colonne de commandes, sous « Annuler ». Son libellé donne le
  rang de la paire montrée : « Aide 3/14 ». Il est grisé quand aucune paire n'est
  jouable : le message « Plus aucune paire jouable » prend alors le relais.
- Après la dernière paire, l'appui suivant revient à la première.
- Toucher le fond du plateau, hors de toute tuile, éteint l'aide et abandonne la
  tuile sélectionnée. Le bouton n'éteint jamais l'aide, puisqu'il reboucle.
- Les deux tuiles de la paire montrée ont la face voilée de bleu (`HintColor`), une
  teinte distincte du doré de la sélection.
- `Board.playablePairs()` groupe les paires par motif, dans l'ordre du jeu
  (caractères, ronds, bambous, vents, dragons, fleurs, saisons). Fleurs et saisons
  s'apparient entre elles et forment donc chacune un seul groupe. Cet ordre est
  déterministe : une annulation rend la même liste.
- Si une tuile est sélectionnée au premier appui, ses paires passent en tête
  (`Board.playablePairs(first)`), les autres suivent dans l'ordre des motifs. L'ordre
  est fixé à ce premier appui (`GameState.hintOrder`) : toucher ensuite une tuile
  suggérée la sélectionne sans rebattre l'ordre.
- Le rang de l'aide vit dans `GameState` (`hint`), pas dans `:core` : c'est un état
  d'affichage. Il est remis à zéro à chaque coup joué ou annulé. Sélectionner une des
  deux tuiles suggérées ne l'efface pas, pour qu'on puisse les toucher ; sélectionner
  une autre tuile l'éteint, et le prochain appui repart des paires de cette tuile.

## Raisons

- Grouper par motif rapproche les paires d'un même motif quand trois ou quatre
  exemplaires sont libres : on voit d'un coup d'œil toutes les combinaisons.
- La liste est calculée au premier appui à partir du plateau et de la sélection, puis
  gardée tant que l'aide reste allumée : le plateau ne change pas sans l'éteindre.
- Sélectionner une tuile puis demander l'aide répond à la question « avec quoi
  cette tuile peut-elle partir ? ».

## Conséquences

- L'aide montre un coup *jouable*, pas un coup *gagnant* : en mode difficile, ou même
  en mode normal, la suivre peut mener à une impasse.
- L'usage de l'aide n'est ni compté ni pénalisé.
