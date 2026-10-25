# Étude : un mode expert aux donnes choisies à l'avance

- **Date** : 2026-10-25
- **Statut** : terminée ; intégrée (voir [0014](../../docs/decisions/0014-mode-expert.md))

## Ce que l'on cherche

Le mode difficile garanti ([0013](../../docs/decisions/0013-solution-garantie.md)) sert des
donnes au hasard que le solveur sait vider en 50 000 plateaux. L'étude
[donnes-solubles](../donnes-solubles/README.md) montre qu'un budget plus grand ne les
rendrait guère plus dures en moyenne. Pour un vrai niveau « expert », il faut **choisir**
les donnes dures, ce qui coûte trop cher sur téléphone. D'où l'idée : les choisir à
l'avance sur PC et livrer leurs graines dans l'appli.

`Dealer.dealRandom(TURTLE, Random(graine))` donne la même tortue sur toutes les
plateformes : le générateur de `kotlin.random.Random` est écrit en Kotlin pur.

Il reste à fixer le seuil de difficulté.

## Les programmes

| Fichier | Rôle |
|---|---|
| `core/src/test/kotlin/.../core/Playouts.kt` | Parties jouées au hasard sur des tableaux d'entiers, bien plus rapides que par `Board`. |
| `core/src/test/kotlin/.../core/ExpertTest.kt` | Banc de répartition et générateur des graines, désactivés par défaut. |
| `poc/donnes-expert/evaluer.sh` | Lance le banc de répartition, range sa sortie dans `resultats/`. |
| `poc/donnes-expert/generer.sh` | Réécrit `core/src/main/kotlin/.../core/ExpertSeedList.kt`. |

```sh
poc/donnes-expert/evaluer.sh 2000              # répartition sur 2 000 tirages, ~6 min
poc/donnes-expert/generer.sh 2000              # 2 000 graines expert
```

La difficulté d'une donne est la part de 400 parties gagnées par un joueur qui joue
une paire jouable quelconque, tirée au hasard. Ce n'est qu'un indicateur : un humain
réfléchit.

## Résultats ([sortie brute](resultats/repartition-2000-2026-10-25.txt))

Sur 2 000 tirages, 84 % sont solubles (budget d'un million de plateaux). Calage : les
donnes normales sont gagnées à 45,7 % (43,2 % avec le banc par `Board` de l'étude
précédente), les difficiles actuelles à 8,8 %.

La difficulté des donnes solubles est très étalée : 10 % ne sont jamais gagnées au
hasard, la médiane est à 3 %, et 10 % sont gagnées plus de 22 fois sur 100.

| Seuil : victoires au plus | Donnes solubles gardées | Tirages gardés | Difficiles actuelles sous le seuil |
|---|---|---|---|
| 0 % | 15 % | 13 % | 11 % |
| 0,5 % | 26 % | 22 % | 19 % |
| **1 %** | **34 %** | **28 %** | **27 %** |
| 2 % | 44 % | 37 % | 38 % |
| 5 % | 60 % | 51 % | 55 % |
| 10 % | 75 % | 63 % | 71 % |

## Conclusions

- **Seuil retenu : 1 %**, le tiers le plus dur des donnes solubles, soit au plus 4
  victoires sur 400. C'est environ 9 fois plus dur que le mode difficile actuel en
  moyenne, et 45 fois plus que le mode normal. 2 % est presque la médiane ; 0 % touche
  la limite de la mesure et ne garde que des donnes à chemin très étroit.
- **Budget du solveur : un million de plateaux.** L'étude précédente montre qu'aller
  jusqu'à 5 millions ne trouve presque aucune donne soluble de plus.
- **Le mode difficile actuel est très inégal** : 30 % de ses donnes sont gagnées plus de
  10 fois sur 100. On pourrait écarter les plus faciles avec le même outil ; ce n'est
  pas fait.

## Limites

- Joueur au hasard seulement ; l'avis de vrais joueurs reste à recueillir.
- Le nombre de donnes expert est fini.
- Toute modification de `Dealer.dealRandom`, de `Face.fullSet()` ou de l'ordre de
  `Layouts.TURTLE` change les tortues des graines : il faut alors relancer
  `generer.sh`. `ExpertTest` revérifie les premières graines de la liste.
