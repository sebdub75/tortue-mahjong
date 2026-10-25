# 0015 — Taille de police : agrandissement plafonné, commandes en icônes

- **Date** : 2026-10-25
- **Statut** : accepté

## Contexte

Les textes sont en `sp` : ils suivent la taille de police choisie dans les réglages du
téléphone. Les marges, la largeur de la colonne de commandes (150 dp) et la hauteur de
l'écran (411 dp sur un Pixel 8 en paysage) ne la suivent pas. Avec une grande police,
les libellés passaient sur deux ou trois lignes et la colonne débordait par le bas ; le
bouton « Options », dernier de la colonne, sortait de l'écran et devenait inaccessible.

## Décision

- **Plafond** : dans `MainActivity`, l'échelle de police du système est ramenée à
  `MAX_FONT_SCALE` (1,3) au plus, en fournissant à l'interface une `LocalDensity`
  modifiée. En dessous du plafond, la densité du système est gardée telle quelle.
- **Options en icône** : le bouton devient une roue dentée (`ic_options.xml`, icône
  « settings » de Material Icons, Apache 2.0), dessinée en `dp`, dans le coin haut droit
  du plateau. Cette zone est toujours vide : la tortue n'y a que ses tuiles d'extrémité,
  à mi-hauteur.
- **Annuler et Aide en icônes**, en bas de la colonne : flèche de retour
  (`ic_undo.xml`, « undo ») et loupe (`ic_hint.xml`, « search » : on cherche les
  paires), même source que la roue dentée. Pendant l'aide, le rang de la paire montrée
  (« 2/12 ») s'affiche à droite de la loupe. Le bas de la loupe reste aligné sur la
  dernière rangée de la tortue.
- **Nouvelle donne reste en texte**, sous les compteurs : aucune icône (flèches
  croisées, dé, flèche circulaire, tuile) ne la disait sans ambiguïté. Placée en haut,
  elle est séparée des coups de la partie, en bas, qu'elle abandonne.
- **Compteurs compacts** : « 144 tuiles », « 13 paires » et le mode tiennent chacun sur
  une ligne, au lieu d'un intitulé suivi de la valeur.

## Raisons

- Le plafond respecte en partie le réglage d'accessibilité : la police grossit jusqu'à
  30 %, l'agrandissement le plus courant, sans casser la mise en page. L'ignorer
  complètement (`fontScale = 1`) aurait été plus simple, mais moins respectueux.
- Une icône en `dp` ne dépend plus de la police : Options reste toujours accessible,
  même si un autre élément venait à déborder, et Annuler et Aide gardent leur taille.
- Les compteurs sur une ligne libèrent la hauteur nécessaire : à l'échelle 1,3, la
  colonne tient dans la hauteur d'un téléphone avec de la marge.

## Conséquences

- Au-delà de 1,3, la police de l'application ne grossit plus.
- Les icônes ont une description (`contentDescription`) lue par TalkBack.
- Les dialogues (« Gagné ! », confirmations) gardent leurs boutons en texte : ils ont
  la place.
- Vérification : `adb shell settings put system font_scale 2.0`, puis relancer
  l'application.
