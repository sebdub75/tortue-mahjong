# 0004 — Orientation paysage

- **Date** : 2026-10-21
- **Statut** : accepté

## Contexte

La tortue fait 15 tuiles de large sur 8 de haut. En portrait, sur un Pixel 8
(1080 pixels de large), une tuile ne ferait qu'environ 65 pixels de large : trop
petit pour toucher sans erreur.

## Décision

L'application est **en paysage** (`sensorLandscape` : les deux sens paysage, selon
l'orientation du téléphone).

## Raisons

- Le plateau tient entier à l'écran avec des tuiles assez grandes, sans zoom.
- C'est le choix de *Mahjong HD*.
- La décision ne touche que `:app` : `:core` ignore tout de l'écran.

## Alternatives écartées (pour l'instant)

- **Portrait avec zoom et déplacement** : plus confortable à une main, mais demande la
  gestion du zoom et une détection du toucher qui en tienne compte. Reste possible
  plus tard, en option.
