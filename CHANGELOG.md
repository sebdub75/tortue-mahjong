# Changelog

**English** | [Français](CHANGELOG.fr.md)

## 0.2 — unreleased

*versionCode 2*

### Fixed

- **Playable pairs counter**: it now shows the number of pairs that can actually be
  removed one after another. It used to count every possible combination: three
  identical free tiles showed "3 pairs", although only one pair can be removed. Now
  *n* free tiles of the same pattern count as *n* / 2 pairs (three tiles: 1 pair;
  four: 2 pairs). Flowers match one another, and so do seasons.

### Changed

- **"Hint" button**: the total of its "rank/total" indicator is still the number of
  combinations it steps through, one tap at a time. It can therefore exceed the pairs
  counter: with three identical tiles, the hint shows 3 combinations for 1 pair.

## 0.1 — October 27, 2026

First release.

- Mahjong solitaire on the "turtle" layout (144 tiles, 5 levels), in landscape.
- Deals always solvable in normal mode.
- Hard mode, truly random, with "Guaranteed solution" checked by the solver.
- Expert mode ("Guaranteed difficulty"): deals picked in advance, solvable but full of
  traps.
- "Undo" button that takes back moves one by one, back to the deal.
- "Hint" button that shows the playable pairs one by one, starting with those of the
  selected tile.
- Confirmation before a new deal during a game.
- Game saved after every move, kept when the screen turns off or the app stops.
- "Plain background" option for e-ink screens.
- English and French interface.
