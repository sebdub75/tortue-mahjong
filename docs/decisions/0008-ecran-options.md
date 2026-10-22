# 0008 — Écran d'options : panneau superposé, réglages en `SharedPreferences`

- **Date** : 2026-10-22
- **Statut** : accepté

## Contexte

Le joueur doit pouvoir régler le jeu. Premier réglage : une case « Difficile », d'abord
sans effet, puis branchée sur la donne ([0009](0009-mode-difficile.md)).

## Décision

- Un bouton « Options » dans la colonne de commandes ouvre `OptionsPanel`
  (`OptionsPanel.kt`), un panneau posé par-dessus le plateau sur un voile sombre.
  « Fermer », un toucher sur le voile ou le bouton retour le referment.
- Les réglages vivent dans la classe `Settings` (`Settings.kt`) : un état Compose
  adossé à des `SharedPreferences` (fichier `settings`), donc conservé d'un lancement
  à l'autre.
- La case à cocher est dessinée avec `foundation` (bordure, coche « ✓ »), dans le style
  des boutons existants.

## Raisons

- Un panneau superposé plutôt qu'une navigation entre écrans : la partie en cours
  reste en place dessous, sans bibliothèque de navigation pour un seul écran.
- Pas de dépendance à Material 3 : l'application n'utilise que `foundation`, et une
  case à cocher ne justifie pas de l'ajouter.
- `SharedPreferences` plutôt que DataStore : une poignée de booléens, lus au
  démarrage ; pas besoin d'API asynchrone ni de dépendance supplémentaire.

## Conséquences

- Les réglages sont un détail de l'application, pas des règles : ils restent hors de
  `:core`. « Difficile » choisit la fonction de `Dealer` appelée par l'écran
  ([0009](0009-mode-difficile.md)) ; `Settings` reste côté `app`.
- Le mode en cours (« Normal » ou « Difficile ») s'affiche dans la colonne de
  commandes, sous les compteurs, et change dès que la case est cochée.
- Ajouter une option : une propriété dans `Settings`, une ligne dans `OptionsPanel`.
