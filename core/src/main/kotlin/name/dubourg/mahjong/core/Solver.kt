package name.dubourg.mahjong.core

/** Issue d'une recherche de solution par [Solver]. */
sealed class Solution {
    /** Une suite de coups qui vide le plateau. */
    class Found(val moves: List<Pair<Position, Position>>) : Solution()

    /** Aucune suite de coups ne vide le plateau. */
    object None : Solution()

    /** Budget épuisé avant de conclure. */
    object GaveUp : Solution()
}

/**
 * Cherche une suite de coups qui vide [board], par une recherche en profondeur.
 *
 * Deux élagages : un groupe de tuiles qui s'apparient dont toutes les tuiles restantes
 * sont libres est retiré d'office, sans essayer d'autre coup (le retirer ne bloque rien) ;
 * et les plateaux déjà reconnus perdants sont mémorisés. La recherche abandonne au-delà
 * de [maxNodes] plateaux visités.
 */
class Solver(board: Board, private val maxNodes: Int = 1_000_000) {

    private val positions = board.tiles.keys.toList()
    private val n = positions.size
    private val group: IntArray
    private val groupSize: IntArray
    private val above: Array<IntArray>
    private val left: Array<IntArray>
    private val right: Array<IntArray>

    // Tuiles du même groupe posées au-dessus de chaque tuile, même indirectement.
    private val matesAbove: Array<IntArray>

    // Priorité d'une tuile : plus elle bloque de tuiles, plus tôt on essaie de la retirer.
    private val priority: IntArray

    // Tuiles encore en place : un bit par tuile.
    private val remaining = LongArray((n + 63) / 64)
    private val remainingInGroup: IntArray
    private val dead = HashSet<List<Long>>()
    private val moves = ArrayList<Pair<Position, Position>>()

    /** Nombre de plateaux visités par la dernière recherche. */
    var nodes = 0
        private set

    init {
        // Fleurs et saisons forment chacune un seul groupe, comme dans Face.matches.
        val groupOf = HashMap<Any, Int>()
        group = IntArray(n) { i ->
            val face = board.tiles.getValue(positions[i])
            val key: Any = if (face.suit == Suit.FLOWER || face.suit == Suit.SEASON) face.suit else face
            groupOf.getOrPut(key) { groupOf.size }
        }
        groupSize = IntArray(groupOf.size)
        for (g in group) groupSize[g]++
        remainingInGroup = groupSize.copyOf()

        fun neighbours(test: (Position, Position) -> Boolean) = Array(n) { i ->
            (0 until n).filter { j -> test(positions[i], positions[j]) }.toIntArray()
        }
        // Mêmes voisinages que isFreeAmong.
        above = neighbours { p, q -> q.z == p.z + 1 && kotlin.math.abs(q.x - p.x) < 2 && kotlin.math.abs(q.y - p.y) < 2 }
        left = neighbours { p, q -> q.z == p.z && kotlin.math.abs(q.y - p.y) < 2 && q.x == p.x - 2 }
        right = neighbours { p, q -> q.z == p.z && kotlin.math.abs(q.y - p.y) < 2 && q.x == p.x + 2 }

        val allAbove = Array(n) { HashSet<Int>() }
        // Les niveaux du haut d'abord : ce qui est au-dessus d'une tuile est déjà connu.
        for (i in (0 until n).sortedByDescending { positions[it].z }) {
            for (j in above[i]) { allAbove[i] += j; allAbove[i] += allAbove[j] }
        }
        matesAbove = Array(n) { i -> allAbove[i].filter { group[it] == group[i] }.toIntArray() }

        fun chain(i: Int, side: Array<IntArray>): Int = side[i].maxOfOrNull { 1 + chain(it, side) } ?: 0
        val below = IntArray(n)
        for (i in 0 until n) for (j in allAbove[i]) below[j]++
        priority = IntArray(n) { i -> 2 * below[i] + maxOf(chain(i, left), chain(i, right)) }

        for (i in 0 until n) set(i)
    }

    // Bruit ajouté aux priorités à chaque redémarrage, et plafond du redémarrage en cours.
    private val random = kotlin.random.Random(0)
    private var noise = 0
    private var restartLimit = 0

    /**
     * Redémarre la recherche avec un budget qui double, et un ordre des coups de plus en
     * plus bousculé : un mauvais choix fait tôt ne coûte pas tout le budget. Les plateaux
     * perdants restent mémorisés d'un redémarrage à l'autre.
     */
    fun solve(): Solution {
        nodes = 0
        var budget = 2_000
        var restart = 0
        while (true) {
            restartLimit = minOf(maxNodes, nodes + budget)
            noise = minOf(restart, 8)
            try {
                return if (search(n)) Solution.Found(moves.toList()) else Solution.None
            } catch (e: BudgetExhausted) {
                if (nodes >= maxNodes) return Solution.GaveUp
                // Défait les coups en cours, puis relance.
                while (moves.isNotEmpty()) {
                    val (a, b) = moves.last()
                    putBack(positions.indexOf(a), positions.indexOf(b))
                }
                budget *= 2
                restart++
            }
        }
    }

    private object BudgetExhausted : RuntimeException() {
        private fun readResolve(): Any = BudgetExhausted
        override fun fillInStackTrace(): Throwable = this
    }

    private fun has(i: Int) = remaining[i shr 6] and (1L shl (i and 63)) != 0L
    private fun set(i: Int) { remaining[i shr 6] = remaining[i shr 6] or (1L shl (i and 63)) }
    private fun clear(i: Int) { remaining[i shr 6] = remaining[i shr 6] and (1L shl (i and 63)).inv() }

    private fun isFree(i: Int): Boolean =
        above[i].none { has(it) } && (left[i].none { has(it) } || right[i].none { has(it) })

    /**
     * Impasse certaine : une tuile a au-dessus d'elle toutes les autres tuiles restantes
     * de son groupe. Elles partent par paires avant elle, et la dernière, faute de
     * partenaire libre, reste au-dessus.
     */
    private fun stuck(): Boolean = (0 until n).any { i ->
        has(i) && matesAbove[i].size >= remainingInGroup[group[i]] - 1 &&
            matesAbove[i].count { has(it) } == remainingInGroup[group[i]] - 1
    }

    private fun take(a: Int, b: Int) {
        clear(a); clear(b)
        remainingInGroup[group[a]] -= 2
        moves += positions[a] to positions[b]
    }

    private fun putBack(a: Int, b: Int) {
        set(a); set(b)
        remainingInGroup[group[a]] += 2
        moves.removeAt(moves.size - 1)
    }

    private fun search(count: Int): Boolean {
        if (count == 0) return true
        if (++nodes > restartLimit) throw BudgetExhausted
        val key = remaining.toList()
        if (key in dead) return false
        if (stuck()) {
            dead += key
            return false
        }

        val free = Array(groupSize.size) { ArrayList<Int>(4) }
        for (i in 0 until n) if (has(i) && isFree(i)) free[group[i]] += i

        // Coup sûr : tout ce qui reste du groupe est libre, on le retire en entier.
        val whole = free.indices.firstOrNull { g -> free[g].size >= 2 && free[g].size == remainingInGroup[g] }
        if (whole != null) {
            val tiles = free[whole]
            for (k in tiles.indices step 2) take(tiles[k], tiles[k + 1])
            if (search(count - tiles.size)) return true
            for (k in tiles.indices.reversed().filter { it % 2 == 0 }) putBack(tiles[k], tiles[k + 1])
            dead += key
            return false
        }

        val pairs = ArrayList<Pair<Int, Int>>()
        for (tiles in free) for (a in tiles.indices) for (b in a + 1 until tiles.size) pairs += tiles[a] to tiles[b]
        // Le bruit est tiré une fois par paire : un comparateur instable fait échouer le tri.
        val ordered = pairs.map { (a, b) -> Triple(a, b, priority[a] + priority[b] + if (noise == 0) 0 else random.nextInt(noise * 4)) }
            .sortedByDescending { it.third }
        for ((a, b) in ordered) {
            take(a, b)
            if (search(count - 2)) return true
            putBack(a, b)
        }
        dead += key
        return false
    }
}
