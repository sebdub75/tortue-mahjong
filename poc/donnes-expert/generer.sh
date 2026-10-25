#!/usr/bin/env bash
# Calcule les graines du mode expert et réécrit ExpertSeedList.kt dans :core. Voir README.md.
#
#   ./generer.sh [graines=2000]
set -euo pipefail

ici="$(cd "$(dirname "$0")" && pwd)"
racine="$(cd "$ici/../.." && pwd)"
liste="$racine/core/src/main/kotlin/name/dubourg/mahjong/core/ExpertSeedList.kt"

env MAHJONG_EXPERT_GENERATE="${1:-2000}" MAHJONG_EXPERT_OUT="$liste" \
    "$racine/gradlew" -p "$racine" :core:test --tests '*ExpertTest.génération*' -i --rerun 2>&1 \
    | grep 'graines .* expert'
echo "→ $liste"
