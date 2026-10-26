# 0017 — Textes traduits, anglais par défaut

- **Date** : 2026-10-26
- **Statut** : accepté

## Contexte

Jusqu'ici, tous les textes de l'interface étaient écrits en français, en dur dans le
code Kotlin (`GameScreen`, `OptionsPanel`, libellés de `Mode`). Sur un téléphone réglé
dans une autre langue, l'application restait en français.

## Décision

Les textes passent dans les ressources Android, lus par `stringResource` et
`pluralStringResource` :

- `res/values/strings.xml` : anglais, langue par défaut ;
- `res/values-fr/strings.xml` : français.

`Mode` porte des identifiants de ressources (`@StringRes`) au lieu de chaînes. Les
compteurs de tuiles et de paires sont des `<plurals>` : la règle d'accord de chaque
langue s'applique d'elle-même (« 0 paire » en français, « 0 pairs » en anglais), ce qui
remplace l'accord fait à la main. La proposition de nouvelle donne est un texte à trou
(`%1$s`), car le nom du mode n'est pas à la même place dans les deux langues.

## Raisons

- Android choisit les ressources selon la langue du téléphone ; une langue sans
  traduction prend celles de `values/`. Avec l'anglais par défaut, un téléphone en
  allemand ou en espagnol affiche l'anglais, plus largement compris que le français.
- Les `<plurals>` suivent les règles CLDR : en français, la catégorie « one » couvre 0
  et 1, exactement l'accord voulu jusqu'ici.

## Conséquences

- Tout nouveau texte va dans les deux fichiers ; lint signale une traduction manquante
  (`MissingTranslation`). Le nom de l'application et la ligne de version, identiques
  dans toutes les langues, sont marqués `translatable="false"`.
- Le rang de l'aide (`3/17`) et la date de la marque du build ne sont pas traduits.
- L'anglais « unguaranteed hard » décrit le mode difficile sans garantie dans la
  proposition de nouvelle donne ; la colonne de commandes affiche simplement « Hard ».
- Pour essayer une langue sans changer celle du téléphone (Android 13 et plus) :
  `adb shell cmd locale set-app-locales name.dubourg.mahjong --locales en-US`.
