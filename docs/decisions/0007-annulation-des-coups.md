# 0007 — Annulation des coups : une pile de plateaux

- **Date** : 2026-10-22
- **Statut** : accepté

## Contexte

Le joueur doit pouvoir annuler ses coups un à un, jusqu'à revenir à la donne. C'est
aussi la seule issue, hors nouvelle donne, quand plus aucune paire n'est jouable.

## Décision

La classe `Game` de `:core` porte le plateau courant et un lien vers la partie
précédente : `play(a, b)` empile, `undo()` dépile. Elle est immuable, comme `Board`.

L'écran (`GameState` dans `GameScreen.kt`) ne garde que la `Game` courante et la tuile
sélectionnée. Le bouton « Annuler » figure dans la colonne de commandes, grisé s'il n'y
a rien à annuler, et dans le message « Plus aucune paire jouable ».

## Raisons

- On empile les plateaux plutôt que les paires retirées : `Board` est immuable, donc
  garder l'ancien ne coûte qu'une référence, et annuler rend exactement le plateau
  d'avant, ordre des tuiles compris (l'ordre de `playablePairs()` ne change pas).
- Au plus 72 plateaux de 144 tuiles : la mémoire est négligeable.
- L'historique est une règle du jeu, pas un détail d'affichage : il vit dans `:core`
  et se teste en JUnit (`GameTest`).

## Conséquences

- Pas de « refaire » : rejouer après une annulation abandonne la branche annulée.
- Une future sauvegarde de partie devra sérialiser la suite des coups, pas les
  plateaux, puis la rejouer depuis la donne.
