package name.dubourg.mahjong.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import kotlin.random.Random

class SolverTest {

    private val red = Face(Suit.DRAGON, 1)
    private val green = Face(Suit.DRAGON, 2)

    /** Rejoue les coups et vérifie que chacun est permis et que le plateau finit vide. */
    private fun assertEmptiedBy(board: Board, moves: List<Pair<Position, Position>>) {
        var b = board
        for ((p, q) in moves) {
            assertTrue("coup interdit : $p, $q", b.canRemove(p, q))
            b = b.remove(p, q)
        }
        assertTrue("il reste ${b.tiles.size} tuiles", b.tiles.isEmpty())
    }

    @Test
    fun `deux piles croisées sont insolubles`() {
        // Rouge sous vert, vert sous rouge : les deux tuiles du dessus ne s'apparient pas.
        val board = Board(
            mapOf(
                Position(0, 0, 0) to red, Position(0, 0, 1) to green,
                Position(4, 0, 0) to green, Position(4, 0, 1) to red,
            ),
        )
        assertTrue(Solver(board).solve() is Solution.None)
    }

    @Test
    fun `deux piles parallèles se vident`() {
        val board = Board(
            mapOf(
                Position(0, 0, 0) to red, Position(0, 0, 1) to green,
                Position(4, 0, 0) to red, Position(4, 0, 1) to green,
            ),
        )
        val solution = Solver(board).solve()
        assertTrue(solution is Solution.Found)
        assertEmptiedBy(board, (solution as Solution.Found).moves)
    }

    @Test
    fun `les donnes normales sont toujours résolues`() {
        for (seed in 0 until 50) {
            val board = Dealer.deal(Layouts.TURTLE, Random(seed)).board
            val solution = Solver(board).solve()
            assertNotEquals("donne $seed déclarée insoluble", Solution.None, solution)
            if (solution is Solution.Found) assertEmptiedBy(board, solution.moves)
        }
    }

    @Test
    fun `la solution trouvée sur une donne au hasard est valable`() {
        var found = 0
        for (seed in 0 until 50) {
            val board = Dealer.dealRandom(Layouts.TURTLE, Random(seed))
            val solution = Solver(board).solve()
            if (solution is Solution.Found) {
                assertEmptiedBy(board, solution.moves)
                found++
            }
        }
        assertTrue("aucune donne résolue", found > 0)
    }

    /**
     * Banc d'essai de l'étude de faisabilité, lancé par poc/donnes-solubles/evaluer.sh :
     * MAHJONG_BENCH=1000 ./gradlew :core:test --tests '*SolverTest.banc*' -i
     */
    @Test
    fun `banc d'essai sur des donnes au hasard`() {
        val count = System.getenv("MAHJONG_BENCH")?.toIntOrNull()
        assumeTrue(count != null)
        val maxNodes = System.getenv("MAHJONG_BENCH_NODES")?.toIntOrNull() ?: 1_000_000

        // Échauffement de la JVM.
        repeat(50) { Solver(Dealer.dealRandom(Layouts.TURTLE, Random(-1 - it)), maxNodes).solve() }

        class Run(val seed: Int, val outcome: String, val nodes: Int, val millis: Double, val blocked: Boolean)
        val runs = (0 until count!!).map { seed ->
            val board = Dealer.dealRandom(Layouts.TURTLE, Random(seed))
            val solver = Solver(board, maxNodes)
            val start = System.nanoTime()
            val outcome = when (solver.solve()) {
                is Solution.Found -> "soluble"
                Solution.None -> "insoluble"
                Solution.GaveUp -> "abandon"
            }
            Run(seed, outcome, solver.nodes, (System.nanoTime() - start) / 1e6, board.playablePairs().isEmpty())
        }

        fun pct(k: Int) = "%.1f %%".format(100.0 * k / count)
        fun quantiles(label: String, values: List<Double>) {
            if (values.isEmpty()) return
            val s = values.sorted()
            fun q(p: Double) = s[((s.size - 1) * p).toInt()]
            println(
                "%-22s n=%4d  médiane %8.1f  p90 %8.1f  p99 %8.1f  max %8.1f"
                    .format(label, s.size, q(0.5), q(0.9), q(0.99), s.last()),
            )
        }

        println("=== $count donnes au hasard, budget $maxNodes plateaux ===")
        for ((outcome, list) in runs.groupBy { it.outcome }.toSortedMap()) {
            println("$outcome : ${list.size} (${pct(list.size)})")
        }
        println("bloquées dès la donne : ${runs.count { it.blocked }} (${pct(runs.count { it.blocked })})")
        for ((outcome, list) in runs.groupBy { it.outcome }.toSortedMap()) {
            quantiles("ms $outcome", list.map { it.millis })
            quantiles("plateaux $outcome", list.map { it.nodes.toDouble() })
        }
        quantiles("ms toutes", runs.map { it.millis })
        // Coût d'une donne acceptée si l'on jette toute donne non résolue en `budget` plateaux.
        // Le temps d'un essai coupé est extrapolé du temps moyen par plateau de la même donne.
        println("budget/essai  acceptées  ms/essai moyen  ms par donne acceptée (moyenne)")
        for (budget in listOf(5_000, 10_000, 20_000, 50_000, 100_000, 200_000, 500_000, 1_000_000, 5_000_000)) {
            if (budget > maxNodes) break
            val accepted = runs.count { it.outcome == "soluble" && it.nodes <= budget }
            val cost = runs.sumOf { if (it.nodes <= budget) it.millis else it.millis * budget / it.nodes }
            println(
                "%12d  %9s  %14.1f  %14.1f".format(
                    budget, pct(accepted), cost / count, if (accepted == 0) Double.NaN else cost / accepted,
                ),
            )
        }
        println("abandons : graines ${runs.filter { it.outcome == "abandon" }.map { it.seed }.take(20)}")
        assertEquals(count, runs.size)
    }

    /** Groupe de tuiles qui s'apparient, comme dans Face.matches. */
    private fun groupOf(face: Face): Any =
        if (face.suit == Suit.FLOWER || face.suit == Suit.SEASON) face.suit else face

    /**
     * Joue une partie au hasard jusqu'au blocage ; vrai si le plateau finit vide. Le joueur
     * [careful] joue d'abord les coups sûrs (tout ce qui reste d'un groupe est libre).
     */
    private fun playout(start: Board, random: Random, careful: Boolean): Boolean {
        var board = start
        while (board.tiles.isNotEmpty()) {
            val pairs = board.playablePairs()
            if (pairs.isEmpty()) return false
            val safe = if (!careful) emptyList() else {
                val left = board.tiles.values.groupingBy { groupOf(it) }.eachCount()
                val free = board.tiles.filterKeys { board.isFree(it) }.values.groupingBy { groupOf(it) }.eachCount()
                pairs.filter { (a, _) -> groupOf(board.tiles.getValue(a)).let { free[it] == left[it] } }
            }
            val (a, b) = (safe.ifEmpty { pairs }).random(random)
            board = board.remove(a, b)
        }
        return true
    }

    /**
     * Difficulté des donnes, mesurée par le taux de victoire de joueurs au hasard, lancée
     * par poc/donnes-solubles/evaluer.sh : MAHJONG_DIFFICULTY=600 ./gradlew :core:test --tests '*SolverTest.difficult*' -i
     */
    @Test
    fun `difficulté des donnes selon leur origine`() {
        val count = System.getenv("MAHJONG_DIFFICULTY")?.toIntOrNull()
        assumeTrue(count != null)
        val playouts = 100

        val populations = linkedMapOf<String, MutableList<Board>>(
            "normales" to MutableList(count!!) { Dealer.deal(Layouts.TURTLE, Random(it)).board },
            "difficiles <= 20 000" to mutableListOf(),
            "difficiles 20 000 - 1 M" to mutableListOf(),
            "difficiles indécises" to mutableListOf(),
        )
        for (seed in 0 until count) {
            val board = Dealer.dealRandom(Layouts.TURTLE, Random(seed))
            val solver = Solver(board, 1_000_000)
            val key = when (solver.solve()) {
                is Solution.Found -> if (solver.nodes <= 20_000) "difficiles <= 20 000" else "difficiles 20 000 - 1 M"
                Solution.GaveUp -> "difficiles indécises"
                Solution.None -> null
            }
            if (key != null) populations.getValue(key) += board
        }

        println("=== Taux de victoire de joueurs au hasard, $playouts parties par donne ===")
        println("%-24s %5s  %-28s  %-28s".format("population", "donnes", "au hasard : moy / méd / <5 %", "prudent : moy / méd / <5 %"))
        val random = Random(7)
        for ((name, boards) in populations) {
            if (boards.isEmpty()) continue
            fun stats(careful: Boolean): String {
                val rates = boards.map { b -> (0 until playouts).count { playout(b, random, careful) } / playouts.toDouble() }.sorted()
                return "%5.1f %% / %5.1f %% / %5.1f %%".format(
                    100 * rates.average(), 100 * rates[rates.size / 2], 100.0 * rates.count { it < 0.05 } / rates.size,
                )
            }
            println("%-24s %5d  %-28s  %-28s".format(name, boards.size, stats(false), stats(true)))
        }
    }
}
