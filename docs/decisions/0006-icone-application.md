# 0006 — Icône de l'application : une paire de 中 en relief

- **Date** : 2026-10-22
- **Statut** : accepté

## Contexte

L'application n'avait pas d'icône : le lanceur affichait l'icône Android par défaut.
Quatre pistes ont été envisagées : une paire de tuiles en relief, la silhouette de la
tortue, une tuile unique de face, une tuile fleur ou saison.

## Décision

Une paire de dragons rouges 中 côte à côte, dessinées comme dans le jeu (relief en
perspective cavalière, tranche rouge et ivoire, voir [0003](0003-rendu-tuiles-en-relief.md)),
sur le tapis vert de la table.

C'est une icône adaptative (`minSdk` 26) entièrement vectorielle :

| Fichier | Rôle |
|---|---|
| `res/mipmap-anydpi-v26/ic_launcher.xml` | assemblage des couches |
| `res/drawable/ic_launcher_background.xml` | tapis vert, même dégradé radial que l'écran de jeu |
| `res/drawable/ic_launcher_foreground.xml` | les deux tuiles et leur ombre |
| `res/drawable/ic_launcher_monochrome.xml` | icône à thème (Android 13+) : tranche en demi-teinte, contour et symbole pleins |

Les trois drawables sont produits par `art/icone/generer.js` (Node, sans dépendance) :

```sh
node art/icone/generer.js
```

Le script reprend les proportions de `TileMetrics`, la construction `extrusion()` et
les teintes de `TileRenderer.kt`. Le symbole 中 est celui de `tile_chun.xml`, posé sur
la face par une transformation de groupe.

## Raisons

- La paire résume la règle du jeu : retirer deux tuiles identiques.
- Le relief et le tapis reprennent l'écran de jeu : l'icône et l'application se
  reconnaissent l'une l'autre.
- 中 est la tuile la plus lisible en petit (un seul caractère, rouge sur ivoire).
- Tout en vectoriel : aucune image matricielle à générer pour chaque densité d'écran.

## Conséquences

- Les deux tuiles tiennent dans le disque central de 66 dp sur 108, toujours visible
  quel que soit le masque du lanceur ; seule l'ombre peut être rognée.
- VectorDrawable ne sait pas flouter : l'ombre portée est un aplat translucide.
- La tuile de droite est découpée par le contour de celle de gauche plutôt que
  recouverte : l'icône monochrome, où les faces sont transparentes, reste juste.
- Si les teintes ou proportions de `TileRenderer.kt` changent, relancer le script.
