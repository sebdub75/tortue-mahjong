# Sources graphiques des tuiles

Les 42 motifs de la tortue, au format SVG 300 × 400. Chaque fichier ne contient que le
**symbole** : il se pose sur le fond de tuile `fluffystuff/Front.svg` (voir la fiche
[0002](../../docs/decisions/0002-tuiles-fluffystuff.md)).

| Famille | Fichiers | Nombre |
|---|---|---|
| Caractères 1–9 | `fluffystuff/Man1.svg` … `Man9.svg` | 9 |
| Ronds 1–9 | `fluffystuff/Pin1.svg` … `Pin9.svg` | 9 |
| Bambous 1–9 | `fluffystuff/Sou1.svg` … `Sou9.svg` | 9 |
| Vents est, sud, ouest, nord | `fluffystuff/Ton.svg`, `Nan.svg`, `Shaa.svg`, `Pei.svg` | 4 |
| Dragons rouge, vert, blanc | `fluffystuff/Chun.svg`, `Hatsu.svg`, `Haku.svg` | 3 |
| Fleurs 梅 蘭 菊 竹 | `fleurs-saisons/Flower1.svg` … `Flower4.svg` | 4 |
| Saisons 春 夏 秋 冬 | `fleurs-saisons/Season1.svg` … `Season4.svg` | 4 |

Plus le fond `fluffystuff/Front.svg` et le dos `fluffystuff/Back.svg`. Le dragon blanc
(`Haku.svg`) est volontairement vide : c'est la face nue.

## `fluffystuff/` — FluffyStuff

- Origine : <https://github.com/FluffyStuff/riichi-mahjong-tiles>, dossier `Regular/`,
  commit `26e127ba2117f45cdce5ea0225748cc0cfad3169` (2024-06-15).
- Licence : CC0 1.0, domaine public (`fluffystuff/LICENSE.md`).
- Fichiers copiés sans modification. Non repris : les variantes « 5 rouges »
  (`*-Dora.svg`), `Blank.svg` (point d'interrogation) et la variante `Black/`.

## `fleurs-saisons/` — fleurs et saisons

Le jeu FluffyStuff n'en contient pas. Elles sont produites par `generer.js` à partir
de la police pinceau **Yuji Boku** (SIL Open Font License 1.1, `OFL-YujiBoku.txt`) :
un grand caractère (fleurs en rouge, saisons en vert), épaissi pour égaler le trait
FluffyStuff, et un numéro de un à quatre en noir dans le coin. Les glyphes sont
convertis en tracés : les SVG ne dépendent d'aucune police.

Pour régénérer :

```sh
cd art/tiles/fleurs-saisons
curl -LO https://raw.githubusercontent.com/google/fonts/main/ofl/yujiboku/YujiBoku-Regular.ttf
# sha256 de la version utilisée :
# 94fda16384f3bdac24376a000c57e99abfa314961bd89ef27badfb7410322003
npm install
node generer.js YujiBoku-Regular.ttf .
```

La police (8 Mo) et `node_modules/` ne sont pas versionnés.
