# Notes de version

[English](CHANGELOG.md) | **Français**

## 0.2 — non publiée

*versionCode 2*

### Corrigé

- **Compteur de paires jouables** : il annonce désormais le nombre de paires qu'on
  peut réellement retirer l'une après l'autre. Avant, il comptait toutes les
  combinaisons possibles : trois tuiles libres identiques affichaient « 3 paires »,
  alors qu'une seule paire peut être retirée. Désormais, *n* tuiles libres d'un même
  motif comptent pour *n* / 2 paires (trois tuiles : 1 paire ; quatre : 2 paires).
  Les fleurs comptent entre elles, de même que les saisons.

### Modifié

- **Bouton « Aide »** : le total de l'indicateur « rang/total » reste le nombre de
  combinaisons parcourues, appui après appui. Il peut donc dépasser le compteur de
  paires : avec trois tuiles identiques, l'aide montre 3 combinaisons pour 1 paire.

## 0.1 — 27 octobre 2026

Première version publiée.

- Mahjong solitaire sur la disposition « tortue » (144 tuiles, 5 niveaux), en paysage.
- Donnes toujours solubles en mode normal.
- Mode difficile, tiré vraiment au hasard, avec « Solution garantie » par le solveur.
- Mode expert : donnes choisies à l'avance, solubles mais très piégeuses.
- Bouton « Annuler » qui défait les coups un à un jusqu'à la donne.
- Bouton « Aide » qui montre les paires jouables une à une, en commençant par celles de
  la tuile sélectionnée.
- Confirmation avant une nouvelle donne en cours de partie.
- Partie enregistrée à chaque coup, conservée quand l'écran s'éteint ou que l'appli
  s'arrête.
- Option « Fond uni » pour les écrans e-ink.
- Interface en anglais et en français.
