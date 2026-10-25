# 0009 — Mode difficile : donne tirée au hasard

- **Date** : 2026-10-22
- **Statut** : accepté ; modifié par [0013](0013-solution-garantie.md) (solution garantie)

## Contexte

Les donnes normales sont toujours solubles : `Dealer.deal` construit d'abord un ordre
de retrait valide, puis pose les faces paire par paire (voir `Dealer`). Le mode
difficile, choisi dans l'écran d'options ([0008](0008-ecran-options.md)), doit au
contraire tirer la tortue au hasard, sans garantie qu'elle puisse être vidée.

## Décision

- `Dealer.dealRandom(layout, random)` mélange les tuiles et les pose sur les
  positions, sans chercher de solution. Il renvoie un `Board` : il n'y a pas de
  solution à fournir, contrairement à `Deal`.
- Les faces sont piochées par paires entières avant le mélange : sur une disposition
  plus petite que le jeu complet, chaque tuile a toujours sa partenaire. Sur la
  tortue, qui prend les 144 tuiles, cela revient à mélanger le jeu complet.
- L'écran passe `settings.hard` à chaque nouvelle donne. Chaque partie retient le
  mode dans lequel elle a été distribuée (`GameState.hard`), et c'est ce mode
  qu'affiche la colonne de commandes.
- Changer le mode ne touche pas la partie en cours. En sortant des options, si le mode
  coché diffère de celui de la partie, un panneau demande : « Vous êtes passé en mode
  difficile (normal). Souhaitez-vous une nouvelle donne ? », avec « Continuer la
  partie » et « Nouvelle donne ». Refuser garde la partie : le nouveau mode vaudra
  pour la donne suivante.

## Raisons

- Le tirage au hasard est une règle de distribution : il vit dans `:core`, à côté de
  `deal`, et se teste en JUnit (`DealerTest`).
- Ne pas relancer la donne d'office : on ne perd pas une partie entamée sur une
  fausse manœuvre dans les options. La question n'est posée qu'à la sortie, une
  fois : cocher puis décocher ne déclenche rien.
- Le panneau de question reprend le voile et le cadre des options (`ModalPanel`).

## Conséquences

- En mode difficile, une partie peut être perdue d'avance, voire bloquée dès la
  donne ; le message « Plus aucune paire jouable » propose alors une nouvelle donne.
- Rien n'indique au joueur si la donne en cours est soluble.
