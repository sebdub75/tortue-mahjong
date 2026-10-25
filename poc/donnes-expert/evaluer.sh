#!/usr/bin/env bash
# Lance le banc d'essai de l'étude « mode expert » et range sa sortie dans resultats/.
# Voir README.md.
#
#   ./evaluer.sh [donnes=1000] [budget=1000000] [parties=400]
set -euo pipefail

ici="$(cd "$(dirname "$0")" && pwd)"
racine="$(cd "$ici/../.." && pwd)"
n="${1:-1000}" budget="${2:-1000000}" parties="${3:-400}"
sortie="repartition-$n-$(date +%F).txt"

echo "→ $sortie"
# Ne garde que ce qu'affiche le banc (lignes indentées après « === »).
env MAHJONG_EXPERT="$n" MAHJONG_EXPERT_NODES="$budget" MAHJONG_EXPERT_GAMES="$parties" \
    "$racine/gradlew" -p "$racine" :core:test --tests '*ExpertTest.répartition*' -i --rerun 2>&1 \
    | awk '/^    ===/ { on = 1 } on && !/^    / { on = 0 } on { sub(/^    /, ""); print }' \
    | tee "$ici/resultats/$sortie"
