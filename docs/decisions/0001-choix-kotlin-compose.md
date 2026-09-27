# 0001 — Choix de Kotlin + Jetpack Compose

- **Date** : 2026-09-27
- **Statut** : accepté

## Contexte

Le jeu est un Mahjong solitaire à un joueur (voir le [README](../../README.md)) : une
pyramide de 144 tuiles sur 5 niveaux, dont on retire des paires identiques et libres.
Techniquement, c'est un jeu 2D sans temps réel ni physique : des sprites statiques, deux
touchers pour former une paire, une animation de disparition.

## Décision

Développer en **Kotlin avec Jetpack Compose**, le plateau étant dessiné dans un `Canvas`.

Organisation visée :

- **`:core`** (Kotlin pur, sans dépendance Android) : modèle des tuiles `(x, y, z, face)`,
  dispositions décrites en données, règle « tuile libre », génération de donnes
  **toujours solubles** (paires posées à l'envers en partant du plateau vide), indice,
  mélange, annulation. Testé avec JUnit.
- **`:app`** (Android/Compose) : rendu couche par couche avec un léger décalage par
  niveau, détection de la tuile touchée du niveau le plus haut au plus bas, animations,
  menus, sauvegarde.

## Raisons

- Un moteur de jeu n'apporte rien à ce type de jeu : Compose, `Canvas` et `Animatable`
  suffisent pour tourner à 60 images par seconde.
- Outillage Android standard : APK de quelques Mo, accès direct à Play Games (succès,
  classements), aux achats intégrés (Billing) et à DataStore, sans plugin tiers.
- Les règles, isolées dans `:core`, se testent rapidement sans émulateur.
- Kotlin est vite familier pour un développeur C++ (typage statique, `sealed class`
  proches de `std::variant`).

## Alternatives écartées

- **Godot 4** : pertinent pour viser aussi iOS ou le PC ; APK d'environ 30 Mo et
  intégration Android par plugins.
- **C++ natif (NDK avec SDL ou raylib)** : faisable, mais la compilation et le passage
  Java/C++ (JNI) compliquent le projet sans bénéfice ici.
- **Flutter** : comparable à Compose, mais en Dart.
- **Unity** : surdimensionné.
