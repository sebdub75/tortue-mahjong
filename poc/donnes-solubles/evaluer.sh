#!/usr/bin/env bash
# Lance les bancs d'essai de l'étude « donnes solubles en mode difficile » et range
# leurs sorties dans resultats/. Voir README.md.
#
#   ./evaluer.sh vitesse [donnes=1000] [budget=1000000]
#   ./evaluer.sh difficulte [donnes=600]
#   ./evaluer.sh                       # les deux, valeurs par défaut
set -euo pipefail

ici="$(cd "$(dirname "$0")" && pwd)"
racine="$(cd "$ici/../.." && pwd)"
jour="$(date +%F)"

# Lance un test du banc et ne garde que ce qu'il affiche (lignes indentées après « === »).
lancer() {
    local test="$1" sortie="$2"
    shift 2
    echo "→ $sortie"
    env "$@" "$racine/gradlew" -p "$racine" :core:test --tests "*SolverTest.$test*" -i --rerun 2>&1 \
        | awk '/^    ===/ { on = 1 } on && !/^    / { on = 0 } on { sub(/^    /, ""); print }' \
        | tee "$ici/resultats/$sortie"
}

vitesse() {
    local n="${1:-1000}" budget="${2:-1000000}"
    lancer banc "vitesse-$n-$jour.txt" MAHJONG_BENCH="$n" MAHJONG_BENCH_NODES="$budget"
}

difficulte() {
    local n="${1:-600}"
    lancer difficult "difficulte-$n-$jour.txt" MAHJONG_DIFFICULTY="$n"
}

case "${1:-tout}" in
    vitesse) shift; vitesse "$@" ;;
    difficulte) shift; difficulte "$@" ;;
    tout) vitesse; difficulte ;;
    *) echo "usage : $0 [vitesse [donnes] [budget] | difficulte [donnes]]" >&2; exit 1 ;;
esac
