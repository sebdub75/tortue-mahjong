# Étude de faisabilité : des donnes solubles en mode difficile

- **Date** : 2026-10-24
- **Statut** : terminée ; conclusion favorable

## Ce que l'on cherche à montrer

En mode difficile, `Dealer.dealRandom` mélange les tuiles sans garantie de solution
(voir [0009](../../docs/decisions/0009-mode-difficile.md)). Le joueur qui perd ne sait
pas s'il a mal joué ou si la tortue était perdue d'avance.

Idée : tirer des donnes au hasard et ne garder que celles qu'un solveur sait vider. La
difficulté viendrait alors des pièges de la donne, et non de donnes impossibles.

L'étude répond à trois questions avant toute intégration dans l'application :

1. **Quelle part des donnes au hasard est soluble ?** Elle fixe le nombre de tirages
   nécessaires.
2. **Combien de temps faut-il pour trouver une donne soluble ?** Il faut rester
   acceptable sur un téléphone, soit environ une demi-seconde au plus.
3. **Les donnes retenues restent-elles difficiles ?** Un solveur à budget limité ne
   garde que les donnes qu'il résout vite. Si ce sont aussi les plus faciles pour le
   joueur, le mode difficile perd son sens.

## Les programmes

| Fichier | Rôle |
|---|---|
| `core/src/main/kotlin/.../core/Solver.kt` | Le solveur. Il est placé dans `:core`, où il resterait si l'idée est retenue. |
| `core/src/test/kotlin/.../core/SolverTest.kt` | Tests unitaires du solveur, plus deux bancs d'essai désactivés par défaut (activés par variable d'environnement). |
| `poc/donnes-solubles/evaluer.sh` | Lance les bancs d'essai et range leurs sorties dans `resultats/`. |
| `poc/donnes-solubles/resultats/` | Sorties brutes des bancs d'essai, datées. |

### Le solveur

C'est une recherche en profondeur sur les paires jouables, avec :

- **Coup sûr joué d'office** : quand toutes les tuiles restantes d'un groupe sont
  libres, on les retire toutes sans essayer d'autre coup. Les retirer ne peut rien
  bloquer.
- **Impasse détectée tôt** : si une tuile a au-dessus d'elle, même indirectement, toutes
  les autres tuiles restantes de son groupe, la partie est perdue. Ces tuiles partent par
  paires avant elle, et la dernière reste au-dessus, faute de partenaire libre.
- **Mémorisation des plateaux perdants** : un masque de bits par plateau.
- **Ordre des coups** : on essaie d'abord les tuiles qui en bloquent le plus, d'abord
  celles qu'elles couvrent, ensuite la longueur de leur rangée.
- **Redémarrages** : le budget commence à 2 000 plateaux et double à chaque
  redémarrage, avec un ordre des coups de plus en plus bousculé au hasard. Les plateaux
  déjà prouvés perdants restent mémorisés d'un redémarrage à l'autre.

Le solveur renvoie `Found` (avec les coups), `None` (preuve d'insolubilité) ou `GaveUp`
(budget épuisé).

Pistes essayées puis écartées :

- **Recherche sans redémarrage** : 44 % des donnes n'étaient pas décidées avec un
  million de plateaux.
- **Détection de cycles entre paires forcées** (deux dernières tuiles d'un groupe
  bloquées par une autre paire forcée, et réciproquement) : la recherche devenait cinq
  fois plus lente, pour une seule donne insoluble de plus détectée sur 200.

## Lancer l'évaluation

Depuis la racine du projet :

```sh
./gradlew :core:test                                   # tests unitaires (les bancs d'essai sont sautés)
poc/donnes-solubles/evaluer.sh vitesse 1000 1000000    # banc de vitesse : 1 000 donnes, budget 1 M plateaux
poc/donnes-solubles/evaluer.sh difficulte 600          # banc de difficulté : 600 graines
poc/donnes-solubles/evaluer.sh                         # les deux, valeurs par défaut
```

Chaque banc écrit `resultats/<banc>-<donnes>-<date>.txt`. Il faut compter environ :

- 8 minutes pour le banc de vitesse avec 1 000 donnes ;
- bien plus de 10 minutes pour le banc de difficulté avec 600 graines, car les parties
  simulées passent par `Board`, qui n'est pas optimisé.

Les donnes sont tirées par graine (0, 1, 2…) : les résultats sont reproductibles, aux
temps près.

### Le banc de vitesse

Le banc de vitesse (`SolverTest.banc…`) résout `dealRandom(TURTLE, Random(graine))` pour
chaque graine, après 50 donnes d'échauffement de la JVM. Pour chaque donne, il note
l'issue, le nombre de plateaux visités et le temps.

Il en déduit le coût d'une donne acceptée en fonction du budget accordé à chaque tirage.
Avec un budget plus petit, une donne aurait été jetée plus tôt. Son temps est alors
extrapolé à partir du temps moyen par plateau de la même donne. Les redémarrages ne
dépendent pas du budget total, ce qui rend cette extrapolation possible.

### Le banc de difficulté

Le banc de difficulté (`SolverTest.difficult…`) classe les donnes en quatre populations :

- les donnes normales (`Dealer.deal`) ;
- les donnes au hasard résolues en 20 000 plateaux au plus ;
- celles résolues entre 20 000 et 1 million de plateaux ;
- celles non décidées à 1 million.

Pour chaque donne, il joue 100 parties avec deux joueurs simulés :

- un joueur **au hasard**, qui joue une paire jouable quelconque ;
- un joueur **prudent**, qui joue d'abord les coups sûrs, puis au hasard.

Le taux de victoire de ces joueurs mesure à quel point la donne pardonne les erreurs.
Plus il est bas, plus la donne est piégeuse.

## Résultats

Machine : PC, JVM 11 via Gradle. Un téléphone est sans doute 3 à 10 fois plus lent.

### Part des donnes solubles et vitesse ([sortie brute](resultats/vitesse-1000-2026-10-24.txt))

Mesures sur 1 000 donnes au hasard, avec un budget d'un million de plateaux :

| Issue | Part | Temps |
|---|---|---|
| Solution trouvée | 84,7 % | médiane 3,7 ms, p90 149 ms, max 1,2 s |
| Insolubilité prouvée | 2,8 % | moins d'1 ms (impasse visible dès la donne) |
| Non décidée | 12,5 % | 1,2 à 1,6 s (budget épuisé) |
| Bloquée dès la donne | 0 % | — |

La vraie part de donnes solubles se situe donc **entre 85 % et 97 %**. Avec un budget
de 5 millions de plateaux, 12,5 % des 200 donnes testées restaient encore non décidées.

Coût d'une donne acceptée selon le budget par tirage :

| Budget par tirage | Donnes gardées | Temps moyen par donne gardée (PC) |
|---|---|---|
| 5 000 plateaux | 46,5 % | 8,6 ms |
| 20 000 | 63,8 % | 17,9 ms |
| 50 000 | 69,7 % | 32,6 ms |
| 200 000 | 79,1 % | 78,9 ms |
| 1 000 000 | 84,7 % | 240 ms |

### Difficulté des donnes retenues ([sortie brute](resultats/difficulte-600-2026-10-24.txt))

Taux de victoire sur 100 parties par donne. Les 600 graines de `dealRandom` se
répartissent en 382 donnes résolues en 20 000 plateaux au plus, 121 entre 20 000 et
1 million, 76 non décidées et 21 prouvées insolubles. Ces dernières sont écartées.

| Population | Donnes | Joueur au hasard : moyenne / médiane | Donnes gagnées moins de 5 fois sur 100 |
|---|---|---|---|
| Normales (`Dealer.deal`) | 600 | 43,2 % / 42 % | 2,2 % |
| Au hasard, résolues en 20 000 plateaux au plus | 382 | 9,6 % / 5 % | 49,7 % |
| Au hasard, résolues entre 20 000 et 1 M | 121 | 2,5 % / 0 % | 81,0 % |
| Au hasard, non décidées | 76 | 0,1 % / 0 % | 100 % |

Le joueur prudent donne le même classement, avec des taux légèrement plus bas : 35,9 %,
8,4 %, 2,3 % et 0,1 %. Jouer tôt les coups sûrs n'aide pas un joueur au hasard. Laissés
en réserve, ces coups sont des coups sans risque qu'il pourra jouer plus tard, au lieu
d'un coup piégeux.

Lecture :

- **Les donnes filtrées restent bien plus dures que les normales** : un joueur au hasard
  gagne environ 4 fois moins souvent, et la moitié de ces donnes sont gagnées moins de
  5 fois sur 100. La construction de `Dealer.deal` produit des donnes très tolérantes.
- **Le filtre introduit un biais, mais modéré** : les donnes que le solveur résout vite
  sont plus faciles (9,6 %) que celles qui lui résistent (2,5 %). Le temps du solveur
  suit donc la difficulté pour le joueur.
- **Les donnes non décidées sont presque toutes perdues** : aucune n'est gagnée plus
  de 5 fois sur 100. Elles sont probablement insolubles pour la plupart, ou solubles
  par un chemin très étroit. Les jeter ne retire rien d'intéressant.

### Donnes laissées de côté avec le budget recommandé

Avec 50 000 plateaux par tirage, on jette 30,3 % des donnes tirées au hasard. Aucune
donne insoluble n'arrive au joueur : chaque donne gardée a une solution trouvée.

| Donnes jetées | Part des tirages | Nature |
|---|---|---|
| Insolubilité prouvée | 2,8 % | insolubles |
| Non décidées, même à 1 M | 12,5 % | probablement insolubles pour la plupart |
| Solution trouvée entre 50 000 et 1 M | 15,0 % | solubles, parmi les plus dures |

La vraie part de donnes insolubles se situe donc entre 2,8 % et 15,3 % des tirages.

## Conclusions

- **Faisable en temps** : avec un budget de 20 000 à 50 000 plateaux par tirage, il faut
  20 à 35 ms par donne sur PC, soit environ 50 à 300 ms sur téléphone, hors du thread
  UI. Chaque tirage est plafonné, donc le pire cas est borné.
- **Le solveur ne sait pas tout prouver** : 12,5 % des donnes restent non décidées.
  Pour le produit ce n'est pas gênant, puisqu'on les jette. Mais on ne connaît pas
  exactement la part de donnes insolubles.
- **Le mode difficile garde son sens** : les donnes filtrées sont environ 4 fois plus
  piégeuses que les normales pour un joueur au hasard.
- **Le budget sert de réglage de difficulté** : un budget plus grand fait entrer des
  donnes plus dures. On pourrait aussi ne garder que les donnes résolues entre deux
  bornes, par exemple pour un niveau « expert ».
- **Recommandation** : passer à l'intégration avec un budget de 50 000 plateaux par
  tirage. Calculer hors du thread UI, mesurer le temps sur un vrai téléphone, et ajouter
  une fiche de décision qui modifie 0009.

## Limites

- Les temps ont été mesurés sur PC, en une seule passe, sans mesure sur téléphone.
- Le solveur n'est pas optimisé : la clé de mémorisation est une `List<Long>`, et des
  listes sont allouées à chaque plateau visité. Un facteur 2 à 5 est probablement
  accessible.
- Les joueurs simulés ne sont que des indicateurs de difficulté ; ils ne remplacent pas
  l'avis de vrais joueurs.
