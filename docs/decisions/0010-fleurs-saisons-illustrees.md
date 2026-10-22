# 0010 — Fleurs et saisons illustrées

- **Date** : 2026-10-23
- **Statut** : accepté

## Contexte

Les fleurs et les saisons sont les seules familles qui s'apparient librement : deux
fleurs quelconques forment une paire, de même deux saisons. Jusqu'ici, leurs symboles
reprenaient le modèle des tuiles d'honneur (voir [0002](0002-tuiles-fluffystuff.md)) :
un grand caractère au pinceau (梅 蘭 菊 竹, 春 夏 秋 冬) et un numéro chinois dans le
coin.

Pour qui ne lit pas le chinois, elles se confondaient avec les dragons, les vents et
les caractères (萬). Rien ne signalait leur règle particulière.

## Décision

Remplacer les caractères par des **illustrations** dessinées pour le projet, dans le
style FluffyStuff : aplats cernés de noir, palette FluffyStuff, plus deux verts clairs
pour les feuillages frais.

| Famille | Motifs | Marque de famille |
|---|---|---|
| Fleurs | prunier, orchidée, chrysanthème, bambou | aucune : une plante en entier |
| Saisons | pousse, soleil, feuille d'érable, flocon | un trait vert en haut et en bas |

- Plus de numéro dans le coin : il ne sert à rien en jeu, puisque l'appariement est
  libre au sein de la famille.
- Les symboles sont produits par `art/tiles/fleurs-saisons/generer.js`, sans
  dépendance, puis convertis en VectorDrawable comme les autres tuiles.

## Raisons

- Une image se distingue d'un caractère au premier coup d'œil, même en petit sur un
  plateau chargé. C'est aussi l'usage des jeux traditionnels, où fleurs et saisons
  sont des dessins.
- Le trait des saisons sépare les deux familles sans réduire l'illustration. Aucune
  autre tuile ne porte de trait horizontal sur toute sa largeur.
- Œuvre originale : pas de licence tierce. La police Yuji Boku (OFL) n'est plus
  nécessaire.

## Conséquences

- Le bambou-fleur est la tuile la plus proche d'une autre famille (les bambous 2 à 9 :
  tiges vertes). Ses tiges penchées et ses feuilles l'en distinguent.
- Le 2026-10-23, une donne sur l'émulateur montrait le prunier, l'orchidée, le bambou,
  la pousse, la feuille d'érable et le flocon. Le chrysanthème et le soleil n'ont été
  vus que dans le rendu SVG de Firefox.

## Alternatives écartées

- **Liseré ou fond teinté par famille, en gardant les caractères** : signale la famille,
  mais la tuile reste illisible pour qui ne lit pas le chinois.
- **Repères occidentaux (1–9, E S O N…) en option** : utile à toutes les familles, mais
  ne rend pas visible la règle des fleurs et des saisons. Reste possible plus tard.
- **Cadres pour les saisons** (cercle, octogone, festonné, coins grecs, losange) : plus
  lourds et réduisant l'illustration, ou peu lisibles en petit.
- **Illustrations d'un jeu libre existant** : celles qu'on a trouvées sont sous
  CC BY-SA (voir [0002](0002-tuiles-fluffystuff.md)).
