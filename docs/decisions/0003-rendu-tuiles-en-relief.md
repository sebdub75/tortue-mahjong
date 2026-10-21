# 0003 — Rendu des tuiles en relief

- **Date** : 2026-10-21
- **Statut** : accepté

## Contexte

La fiche [0002](0002-tuiles-fluffystuff.md) prévoit de dessiner le corps des tuiles par
le code et d'y poser les symboles VectorDrawable. Il fallait choisir comment donner
l'impression de relief et d'empilement, à la manière de *Mahjong HD*, avant d'écrire
le plateau complet. Une maquette (module `:app`, pyramide de 21 tuiles sur 4 niveaux,
lancée par `demo.sh`) a servi à valider le rendu sur l'émulateur.

## Décision

Une **fausse 3D en perspective cavalière**, dessinée dans le `Canvas` Compose
(`app/.../TileRenderer.kt`) :

- **Géométrie** : face de 3 × 4 (format des SVG FluffyStuff), coins arrondis à 12 % de
  la largeur. La tranche est l'extrusion de la face vers le bas à gauche, d'une
  épaisseur de 14 % de la largeur. Le contour est calculé exactement (union des deux
  rectangles arrondis et du parallélogramme tangent), sans empiler de calques.
- **Matière** : couche ivoire sous la face puis dos rouge repris de `Back.svg`. La
  tranche du bas est plus sombre que celle de gauche. La face a un léger dégradé.
- **Empilement** : chaque niveau est décalé d'une épaisseur vers le haut à droite. Une
  tuile pose d'abord une ombre floutée (`BlurMaskFilter`) sur ce qui est dessous.
- **Ordre de dessin** : niveau croissant, puis de droite à gauche, puis de haut en bas.
  Ainsi chaque face recouvre la tranche de ses voisines.
- **Toucher** : la tuile visée est la dernière dessinée dont la face contient le point,
  donc la plus haute.

## Raisons

- Tout est vectoriel : rendu net à toutes les densités et au zoom, aucun fichier
  d'image à décliner.
- Le relief suit la position réelle de la tuile (niveau, voisines), ce qu'une image
  avec relief incrusté ne permet pas.
- Le coût reste faible : quelques chemins par tuile, ce qui convient aux 144 tuiles
  de la tortue.

## Alternatives écartées

- **Vraie 3D (OpenGL, SceneView/Filament)** : rotation libre de la caméra, mais une
  pile logicielle bien plus lourde pour un jeu vu toujours sous le même angle.
- **Empilement de rectangles décalés pixel par pixel** pour simuler la tranche :
  plus simple, mais plus coûteux et sans distinction entre les deux faces de la tranche.

## Limites connues

- L'ombre floutée en `BlurMaskFilter` est accélérée matériellement à partir
  d'Android 9 (API 28). Il faudra vérifier les performances sur un vieil appareil.
- Le toucher ne tient compte que de la face, pas de la tranche.
