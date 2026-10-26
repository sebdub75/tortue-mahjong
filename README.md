# Tortue Mahjong — Mahjong solitaire (Android)

**English** | [Français](README.fr.md)

A single-player Mahjong solitaire game for Android, in the spirit of *Mahjong HD*.

## Philosophy

Tortue Mahjong aims to stay **simple**: a single layout, the turtle, and few options.
If you are looking for a more complete game (many layouts, themes, challenges…),
*Mahjong HD* is recommended.

What sets it apart is its **solver**, which looks for a solution to every deal:

- **Guaranteed solution, even for random deals**: in hard mode, the turtle is dealt
  truly at random, rather than built from a valid removal order like easy deals, which
  makes for much trickier games. The solver keeps only the deals it can clear. If you
  get stuck, you trapped yourself — the deal was not lost from the start — and Undo
  lets you find your way back.
- **Expert mode, hard but solvable**: deals picked in advance from thousands of random
  ones, all solvable, yet won at most once in a hundred games by a player making random
  moves. They are about nine times harder than hard-mode deals.

## How to play

- The 144 tiles are stacked in the classic "turtle" layout: a five-level pyramid.
- Remove **pairs of identical tiles** until the board is empty. Any two flowers make a
  pair, and so do any two seasons.
- Tile set, as in *Mahjong HD*: characters, circles and bamboos (1 to 9) and dragons in
  4 copies; winds, flowers and seasons in 2 copies. Each wind therefore has a single
  partner.
- A tile can only be played if it is **free**:
  - no tile lies on top of it;
  - its left **or** right side is clear.
- You win when the board is empty, and lose when no free pair is left.

Blocked tiles are dimmed. The magnifier button shows the playable pairs one after
another, the arrow button undoes moves, and the game is saved after every move. The
interface is in English and French.

## Installation

Download the latest APK from the [Releases](../../releases) page and open it on your
phone (Android 8.0 or later). You may need to allow installing apps from your browser
or file manager.

## Building

Kotlin and Jetpack Compose. Run the rules tests with `./gradlew :core:test`, and build
a signed release APK with `./apk.sh`. Developer notes and design decisions are in
French: see [README.fr.md](README.fr.md) and [docs/decisions/](docs/decisions/).

## License

The code and the artwork made for the project are released under the **GNU General
Public License, version 3 or any later version** (GPL-3.0-or-later): full text in
[LICENSE](LICENSE).

## Credits

- **Tiles**: [riichi-mahjong-tiles](https://github.com/FluffyStuff/riichi-mahjong-tiles)
  by FluffyStuff, dedicated to the public domain (CC0 1.0). Files copied unchanged into
  `art/tiles/fluffystuff/`.
- **Flowers, seasons and icon**: drawn for the project, in the FluffyStuff style, by
  `art/tiles/fleurs-saisons/generer.js` and `art/icone/generer.js`.
- **Libraries**: [Kotlin](https://kotlinlang.org/),
  [Jetpack Compose and AndroidX](https://developer.android.com/jetpack) (Apache 2.0);
  tests with [JUnit 4](https://junit.org/junit4/) (EPL 1.0).
- **Tile conversion to VectorDrawable** (development tools, not shipped in the APK):
  [svg2vectordrawable](https://github.com/Ashung/svg2vectordrawable) by Ashung Hung
  (MIT), [svgpath](https://github.com/fontello/svgpath) (MIT),
  [svg-path-properties](https://github.com/rveciana/svg-path-properties) by Roger
  Veciana i Rovira (ISC), [@xmldom/xmldom](https://github.com/xmldom/xmldom) (MIT).
- **Inspiration**: *Mahjong HD*, whose layout and tile set this project follows. An
  independent project, not affiliated with the authors of *Mahjong HD*.
