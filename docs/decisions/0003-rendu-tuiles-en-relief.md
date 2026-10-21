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
- **Images précalculées** (`TileSprites`) : toutes les tuiles ont la même forme. L'ombre
  et le corps sont donc dessinés une fois, puis une image par symbole (corps compris),
  refaites seulement quand la taille des tuiles change. Une tuile à l'écran se résume à
  deux images calées sur le pixel, plus la surbrillance ou l'assombrissement. Mesuré
  sur l'émulateur avec la tortue : recalculer les chemins et redessiner les symboles
  vectoriels à chaque image coûtait 250 à 400 ms sur le fil principal, contre environ
  20 ms avec les images.
- **Ordre de dessin** : niveau croissant, puis de droite à gauche, puis de haut en bas.
  Ainsi chaque face recouvre la tranche de ses voisines.
- **Toucher** : la tuile visée est la dernière dessinée dont la face contient le point,
  donc la plus haute. Le geste est un simple appui puis relâché, sans délai d'appui
  long : avec `detectTapGestures`, une image lente faisait perdre des touchers.

## Raisons

- Tout est vectoriel : rendu net à toutes les densités et au zoom, aucun fichier
  d'image à décliner.
- Le relief suit la position réelle de la tuile (niveau, voisines), ce qu'une image
  avec relief incrusté ne permet pas.
- Le coût reste faible : deux images par tuile, ce qui convient aux 144 tuiles de la
  tortue.

## Alternatives écartées

- **Vraie 3D (OpenGL, SceneView/Filament)** : rotation libre de la caméra, mais une
  pile logicielle bien plus lourde pour un jeu vu toujours sous le même angle.
- **Empilement de rectangles décalés pixel par pixel** pour simuler la tranche :
  plus simple, mais plus coûteux et sans distinction entre les deux faces de la tranche.

## Limites connues

- Le flou de l'ombre est calculé une fois dans une image ordinaire : il ne dépend pas
  de l'accélération matérielle.
- Sur l'émulateur sans GPU (SwiftShader), le rendu d'une image du plateau prend encore
  environ 100 ms. Les performances restent à vérifier sur un vrai téléphone.
- Le toucher ne tient compte que de la face, pas de la tranche.
