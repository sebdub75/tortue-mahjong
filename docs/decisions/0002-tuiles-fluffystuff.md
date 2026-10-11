# 0002 — Graphismes des tuiles : FluffyStuff

- **Date** : 2026-10-11
- **Statut** : accepté

## Contexte

La tortue de 144 tuiles utilise **42 motifs** : trois familles numérotées de 1 à 9
(caractères, ronds, bambous), 4 vents, 3 dragons, 4 fleurs et 4 saisons. Plutôt que de
tout dessiner, on cherche un jeu de tuiles libre, réutilisable sans contrainte dans une
application diffusée sur le Play Store.

## Décision

Utiliser le jeu **[FluffyStuff/riichi-mahjong-tiles](https://github.com/FluffyStuff/riichi-mahjong-tiles)**,
variante `Regular`.

- **Licence** : CC0 1.0 (domaine public). Aucune attribution ni clause de partage
  n'est exigée ; on crédite quand même l'auteur dans l'écran « À propos ».
- **Format** : SVG au format 300 × 400, avec **le fond de tuile (`Front.svg`) séparé du
  symbole** (`Man1.svg`…). Le dos est fourni (`Back.svg`). Il existe aussi une variante
  `Black`.

## Conséquences

- **Fleurs et saisons à produire** : le jeu est fait pour le Riichi japonais et ne
  contient ni fleurs ni saisons. Les 8 symboles manquants reprennent le modèle des
  tuiles d'honneur : un grand caractère au pinceau (梅 蘭 菊 竹 en rouge, 春 夏 秋 冬 en
  vert) et un numéro en noir dans le coin. Ils sont générés depuis la police libre
  Yuji Boku (OFL) avec la palette FluffyStuff. Détails et régénération :
  [art/tiles/README.md](../../art/tiles/README.md).
- **Rendu** : les symboles sont convertis en *VectorDrawable* Android. Le corps de la
  tuile (face, épaisseur, ombre selon le niveau, surbrillance des tuiles libres) est
  dessiné par le code dans le `Canvas` Compose, puis le symbole est posé par-dessus. Les
  tuiles restent nettes à toutes les densités d'écran et au zoom.
- **Hors périmètre** : les variantes « 5 rouges » (`Man5-Dora.svg`…) et la tuile vierge
  ne servent pas en solitaire.

## Alternatives écartées

- **Code Inferno** ([OpenGameArt](https://opengameart.org/content/mahjong-tileset)) : seul
  jeu complet en 42 motifs, mais sous CC BY 3.0 (crédit obligatoire), en images PNG avec
  le relief incrusté dans chaque image : à décliner pour chaque densité d'écran, et le
  relief ne peut pas suivre le niveau de la tuile.
- **Wikimedia Commons** (auteur Cangjie6) : complet et en SVG, mais sous CC BY-SA 4.0
  (partage à l'identique des images dérivées), et il faut nettoyer les fichiers
  (déclarations DTD issues d'Illustrator) avant de les utiliser.
- **KMahjongg, GNOME Mahjongg** : sous GPL, qui s'imposerait à toute l'application.
- **Glyphes Unicode (U+1F000–U+1F02B)** : monochromes et peu lisibles en petit ; au
  mieux pour un prototype.
