package name.dubourg.mahjong.core

import kotlin.math.abs

/**
 * Position d'une tuile. x et y sont en demi-tuiles, ce qui permet les décalages
 * d'une demi-tuile ; une tuile occupe donc les cases [x, x + 1] × [y, y + 1].
 * z est le niveau, 0 étant la table.
 */
data class Position(val x: Int, val y: Int, val z: Int)

/** État du plateau : les tuiles encore en place. Immuable. */
class Board(val tiles: Map<Position, Face>) {

    /**
     * Une tuile est libre si rien n'est posé dessus, même en partie, et si son côté
     * gauche ou son côté droit est dégagé au même niveau.
     */
    fun isFree(p: Position): Boolean {
        if (p !in tiles) return false
        val covered = tiles.keys.any { it.z == p.z + 1 && overlaps(it, p) }
        if (covered) return false
        val sameRow = tiles.keys.filter { it.z == p.z && abs(it.y - p.y) < 2 }
        val leftBlocked = sameRow.any { it.x == p.x - 2 }
        val rightBlocked = sameRow.any { it.x == p.x + 2 }
        return !leftBlocked || !rightBlocked
    }

    /** Vrai si [a] et [b] sont deux tuiles distinctes, libres et qui s'apparient. */
    fun canRemove(a: Position, b: Position): Boolean {
        if (a == b) return false
        val faceA = tiles[a] ?: return false
        val faceB = tiles[b] ?: return false
        return faceA.matches(faceB) && isFree(a) && isFree(b)
    }

    /** Plateau sans la paire [a], [b], qui doit pouvoir être retirée. */
    fun remove(a: Position, b: Position): Board {
        require(canRemove(a, b)) { "Paire non jouable : $a, $b" }
        return Board(tiles - a - b)
    }

    /** Toutes les paires jouables, dans l'ordre des tuiles du plateau. */
    fun playablePairs(): List<Pair<Position, Position>> {
        val free = tiles.keys.filter { isFree(it) }
        return free.indices.flatMap { i ->
            (i + 1 until free.size)
                .filter { j -> tiles.getValue(free[i]).matches(tiles.getValue(free[j])) }
                .map { j -> free[i] to free[j] }
        }
    }

    private fun overlaps(a: Position, b: Position) = abs(a.x - b.x) < 2 && abs(a.y - b.y) < 2
}
