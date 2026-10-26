# 0018 — Licence GPL-3.0-or-later

- **Date** : 2026-10-26
- **Statut** : accepté

## Contexte

Le projet va être rendu public. Sans licence, le code reste « tous droits réservés » :
on peut le lire, mais ni le réutiliser ni le redistribuer.

## Décision

Le projet est distribué sous **GPL version 3 ou toute version ultérieure**
(GPL-3.0-or-later). Le texte officiel de la FSF est à la racine, dans `LICENSE` (copie
conforme de <https://www.gnu.org/licenses/gpl-3.0.txt>, SHA-256
`3972dc97…b36986`). La licence couvre le code et les illustrations dessinées pour le
projet (fleurs, saisons, icône).

## Raisons

- Copyleft : une version modifiée et distribuée doit publier son source sous la même
  licence. Personne ne peut tirer du jeu une application fermée.
- Les composants tiers sont compatibles : tuiles FluffyStuff en CC0 (domaine public),
  AndroidX, Compose et Kotlin sous Apache 2.0, compatible avec la GPL 3.
- La GPL 3 est acceptée par F-Droid et IzzyOnDroid.
- « Ou toute version ultérieure » est la formule recommandée par la FSF ; elle évite de
  devoir rassembler tous les contributeurs pour passer à une version future.

## Conséquences

- Les crédits des composants tiers sont tenus dans la section « Crédits » du README.
- Une contribution extérieure est reçue sous la même licence.
- Les tuiles FluffyStuff restent CC0 : leur licence propre est dans
  `art/tiles/fluffystuff/LICENSE.md`.
