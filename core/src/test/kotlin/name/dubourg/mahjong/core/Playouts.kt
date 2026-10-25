package name.dubourg.mahjong.core

import kotlin.math.abs
import kotlin.random.Random

/**
 * Parties jouées au hasard sur [board], comme un joueur qui choisit une paire jouable
 * quelconque. Mêmes règles que [Board], mais sur des tableaux d'entiers : des centaines
 * de parties par donne restent rapides.
 */
class Playouts(board: Board) {

    private val n = board.tiles.size
    private val group: IntArray
    private val above: Array<IntArray>
    private val left: Array<IntArray>
    private val right: Array<IntArray>

    init {
        val positions = board.tiles.keys.toList()
        // Fleurs et saisons forment chacune un seul groupe, comme dans Face.matches.
        val groupOf = HashMap<Any, Int>()
        group = IntArray(n) { i ->
            val face = board.tiles.getValue(positions[i])
            val key: Any = if (face.suit == Suit.FLOWER || face.suit == Suit.SEASON) face.suit else face
            groupOf.getOrPut(key) { groupOf.size }
        }
        fun neighbours(test: (Position, Position) -> Boolean) = Array(n) { i ->
            (0 until n).filter { j -> test(positions[i], positions[j]) }.toIntArray()
        }
        // Mêmes voisinages que isFreeAmong.
        above = neighbours { p, q -> q.z == p.z + 1 && abs(q.x - p.x) < 2 && abs(q.y - p.y) < 2 }
        left = neighbours { p, q -> q.z == p.z && abs(q.y - p.y) < 2 && q.x == p.x - 2 }
        right = neighbours { p, q -> q.z == p.z && abs(q.y - p.y) < 2 && q.x == p.x + 2 }
    }

    /** Joue une partie au hasard jusqu'au blocage ; vrai si le plateau finit vide. */
    fun play(random: Random): Boolean {
        val here = BooleanArray(n) { true }
        val free = IntArray(n)
        val pairA = IntArray(n * n / 2)
        val pairB = IntArray(n * n / 2)
        var count = n
        while (count > 0) {
            var nFree = 0
            for (i in 0 until n) {
                if (here[i] && above[i].none { here[it] } && (left[i].none { here[it] } || right[i].none { here[it] })) {
                    free[nFree++] = i
                }
            }
            var nPairs = 0
            for (a in 0 until nFree) for (b in a + 1 until nFree) {
                if (group[free[a]] == group[free[b]]) {
                    pairA[nPairs] = free[a]
                    pairB[nPairs++] = free[b]
                }
            }
            if (nPairs == 0) return false
            val k = random.nextInt(nPairs)
            here[pairA[k]] = false
            here[pairB[k]] = false
            count -= 2
        }
        return true
    }

    /** Part des [games] parties gagnées. */
    fun winRate(games: Int, random: Random): Double = (0 until games).count { play(random) } / games.toDouble()
}
