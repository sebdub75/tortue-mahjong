# 0016 — Option « Fond uni » pour les écrans e-ink

- **Date** : 2026-10-25
- **Statut** : accepté

## Contexte

Depuis la maquette initiale, le tapis de l'écran de jeu est un dégradé radial, vert
clair `#2B6E55` au centre et vert foncé `#0D2B21` à 2600 px. Sur une liseuse e-ink, il
fait apparaître un halo : converti en gris, il va d'environ 87/255 à 33/255, un écart
que les 16 niveaux de gris de l'écran ne rendent qu'en 3 ou 4 marches, donc en anneaux
concentriques autour du centre.

Un tapis uni pour tous a été essayé : sur un écran OLED, le dégradé donne au plateau
une profondeur qu'on perd avec un aplat.

## Décision

Une case « Fond uni » dans les Options, décochée par défaut (`Settings.plainTable`,
clé `plainTable`). Cochée, le tapis est d'une couleur unie, `#1F5443`, à peu près le
vert que le dégradé donne autour du plateau. Elle est placée en tête des options,
avant les modes de jeu.

## Raisons

- Le dégradé reste l'aspect par défaut, le meilleur sur un écran couleur.
- Détecter un écran e-ink n'est pas fiable sous Android : aucune API ne le dit, et les
  liseuses s'annoncent comme des tablettes ordinaires. Le choix revient au joueur.
- Un dégradé plus doux réduirait le nombre d'anneaux sans les supprimer.

## Conséquences

- L'option prend effet tout de suite, sans nouvelle donne.
- L'icône (`ic_launcher_background.xml`, voir 0006) garde son dégradé : elle est dessinée
  par le lanceur, hors de portée des options.
- Les panneaux translucides (options, dialogues) gardent leur fond `#0D2B21`.
