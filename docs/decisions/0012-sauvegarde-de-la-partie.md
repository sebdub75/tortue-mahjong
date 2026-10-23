# 0012 — Sauvegarde de la partie en cours

- **Date** : 2026-10-23
- **Statut** : accepté

## Contexte

La partie ne vivait qu'en mémoire, dans un `remember` de `GameScreen`. Éteindre l'écran
suffisait à la perdre : l'écran de verrouillage est en portrait, l'activité, fixée en
paysage, est recréée, et une nouvelle donne apparaissait. Même chose quand Android
arrête l'appli en arrière-plan.

## Décision

- `Game.encode()` met la partie en texte : une ligne pour la donne, tuile après tuile
  dans l'ordre du plateau, une ligne pour les coups joués. `Game.decode()` la
  reconstruit en rejouant les coups depuis la donne, comme l'annonçait la fiche 0007.
- `SavedGame` enregistre ce texte et le mode de la partie dans des `SharedPreferences`
  (`game`), à chaque coup, annulation ou nouvelle donne.
- Au lancement, `GameScreen` reprend la partie enregistrée ; à défaut, ou si elle est
  illisible, il distribue une nouvelle donne.
- La tuile sélectionnée et l'aide affichée ne sont pas enregistrées.

## Raisons

- Enregistrer à chaque coup plutôt qu'à la mise en arrière-plan : la partie survit
  aussi à un arrêt brutal, et le texte, de quelques kilo-octets, s'écrit en tâche de
  fond (`apply()`).
- Un seul mécanisme couvre la recréation de l'activité et l'arrêt de l'appli ;
  `rememberSaveable` n'aurait couvert que la première.
- Rejouer les coups avec `play()` revalide la sauvegarde : un texte corrompu donne une
  `IllegalArgumentException`, jamais un plateau incohérent.
- L'ordre des tuiles est conservé, donc celui de `playablePairs()` et de l'aide aussi.

## Conséquences

- La partie reprend telle quelle au lancement suivant ; seule « Nouvelle donne » la
  remplace.
- Un changement du format de `encode()` rendra les anciennes sauvegardes illisibles :
  le joueur perdra sa partie en cours, sans plantage.
