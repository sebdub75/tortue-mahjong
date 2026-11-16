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

    /** Voir [isFreeAmong] ; une position vide n'est pas libre. */
    fun isFree(p: Position): Boolean = p in tiles && isFreeAmong(p, tiles.keys)

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

    /**
     * Toutes les paires jouables, groupées par motif dans l'ordre du jeu (fleurs d'un
     * seul tenant, saisons de même), puis dans l'ordre des tuiles du plateau. Les paires
     * qui contiennent [first], s'il est donné, passent en tête, dans ce même ordre.
     */
    fun playablePairs(first: Position? = null): List<Pair<Position, Position>> {
        val free = tiles.keys.filter { isFree(it) }
        return free.indices.flatMap { i ->
            (i + 1 until free.size)
                .filter { j -> tiles.getValue(free[i]).matches(tiles.getValue(free[j])) }
                .map { j -> free[i] to free[j] }
        }.sortedWith(
            compareBy({ first == null || (it.first != first && it.second != first) },
                { tiles.getValue(it.first).suit }, { tiles.getValue(it.first).groupRank }),
        )
    }

    /**
     * Nombre de paires jouables qu'on peut retirer l'une après l'autre sans se gêner :
     * n tuiles libres d'un même motif en donnent n / 2, là où [playablePairs] liste
     * toutes les combinaisons (3 tuiles identiques : 3 combinaisons, mais 1 seule paire).
     */
    fun playablePairCount(): Int =
        tiles.filterKeys { isFree(it) }.values
            .groupingBy { it.suit to it.groupRank }.eachCount()
            .values.sumOf { it / 2 }
}

/** Rang de la tuile parmi les groupes de sa famille : fleurs et saisons n'en forment qu'un. */
private val Face.groupRank: Int
    get() = if (suit == Suit.FLOWER || suit == Suit.SEASON) 0 else rank

/**
 * Règle de la tuile libre, sur les seules positions occupées [occupied] : rien n'est
 * posé sur [p], même en partie, et son côté gauche ou son côté droit est dégagé au
 * même niveau. Une voisine décalée d'une demi-tuile en hauteur compte comme voisine.
 */
internal fun isFreeAmong(p: Position, occupied: Collection<Position>): Boolean {
    val covered = occupied.any { it.z == p.z + 1 && abs(it.x - p.x) < 2 && abs(it.y - p.y) < 2 }
    if (covered) return false
    val sameRow = occupied.filter { it.z == p.z && abs(it.y - p.y) < 2 }
    val leftBlocked = sameRow.any { it.x == p.x - 2 }
    val rightBlocked = sameRow.any { it.x == p.x + 2 }
    return !leftBlocked || !rightBlocked
}
