# 0005 — Composition du jeu : celle de *Mahjong HD*

- **Date** : 2026-10-22
- **Statut** : accepté

## Contexte

Le jeu de Mahjong classique compte 144 tuiles : 4 exemplaires de chacune des 34 faces
ordinaires (trois familles de 1 à 9, 4 vents, 3 dragons), plus 4 fleurs et 4 saisons
uniques, appariables librement à l'intérieur de leur famille.

*Mahjong HD* répartit autrement les 144 tuiles : les vents n'y sont qu'en 2
exemplaires, si bien qu'un vent ne s'apparie qu'avec son unique jumeau, et fleurs et
saisons y sont doublées.

## Décision

On reprend la composition de *Mahjong HD* :

| Famille | Composition | Tuiles | Appariement |
|---|---|---|---|
| Caractères, cercles, bambous | 3 × 9 faces × 4 | 108 | à l'identique |
| Dragons | 3 × 4 | 12 | à l'identique |
| Vents | 4 × 2 | 8 | à l'identique : une seule paire par vent |
| Fleurs | 4 × 2 | 8 | librement entre fleurs |
| Saisons | 4 × 2 | 8 | librement entre saisons |
| **Total** | | **144** | 72 paires |

Le nombre d'exemplaires est porté par la famille (`Suit.copies` dans
`core/.../Face.kt`).

## Raisons

- Le jeu vise le comportement de *Mahjong HD*.
- Les règles d'appariement (`Face.matches`) ne changent pas : seule la composition
  change. Les vents, en 2 exemplaires identiques, n'ont mécaniquement qu'une
  partenaire ; aucune variante graphique n'est nécessaire.
- Le distributeur (`Dealer`) découpe déjà chaque groupe de tuiles appariables en
  paires : il produit sans modification 1 paire par vent et 4 par famille de fleurs
  ou de saisons. Les donnes restent toujours solubles.
- Les 42 motifs dessinés (voir [0002](0002-tuiles-fluffystuff.md)) suffisent.

## Conséquences

- Les vents deviennent plus contraignants : leurs deux paires possibles sur quatre
  tuiles se réduisent à une paire imposée.
- Fleurs et saisons, plus nombreuses, offrent plus de choix.
