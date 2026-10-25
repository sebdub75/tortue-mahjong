package name.dubourg.mahjong.core

import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.stream.IntStream
import kotlin.random.Random

/** Bancs d'essai de l'étude « mode expert » (poc/donnes-expert), désactivés par défaut. */
class ExpertTest {

    /**
     * Génère ExpertSeeds.kt : les premières graines dont la donne au hasard est soluble en
     * [ExpertSeeds.SOLVER_BUDGET] plateaux et gagnée par un joueur au hasard au plus 1 fois
     * sur 100. Lancé par poc/donnes-expert/generer.sh :
     * MAHJONG_EXPERT_GENERATE=2000 MAHJONG_EXPERT_OUT=<fichier> ./gradlew :core:test --tests '*ExpertTest.génération*'
     */
    @Test
    fun `génération des graines expert`() {
        val count = System.getenv("MAHJONG_EXPERT_GENERATE")?.toIntOrNull()
        assumeTrue(count != null)
        val out = java.io.File(System.getenv("MAHJONG_EXPERT_OUT"))

        val seeds = ArrayList<Int>()
        var start = 0
        // Par tranches calculées en parallèle, gardées dans l'ordre des graines : le
        // résultat ne dépend pas du découpage.
        while (seeds.size < count!!) {
            seeds += IntStream.range(start, start + 600).parallel().filter { isExpert(it) }.toArray().toList()
            start += 600
            println("graines $start : ${seeds.size} expert")
        }
        val kept = seeds.take(count)
        out.writeText(
            buildString {
                append("// Généré par poc/donnes-expert/generer.sh le ${java.time.LocalDate.now()} : ne pas modifier à la main.\n")
                append("// ${kept.size} graines retenues parmi les ${kept.last() + 1} premières.\n")
                append("package name.dubourg.mahjong.core\n\n")
                append("internal val EXPERT_SEED_LIST = intArrayOf(\n")
                for (line in kept.chunked(12)) append("    ").append(line.joinToString(", ")).append(",\n")
                append(")\n")
            },
        )
    }

    /** Vrai si la donne de [seed] est soluble et gagnée au plus 1 fois sur 100 au hasard. */
    private fun isExpert(seed: Int): Boolean {
        val board = Dealer.dealRandom(Layouts.TURTLE, Random(seed))
        if (Solver(board, ExpertSeeds.SOLVER_BUDGET).solve() !is Solution.Found) return false
        return Playouts(board).winRate(ExpertSeeds.GAMES, Random(seed)) <= ExpertSeeds.MAX_WIN_RATE
    }

    @Test
    fun `les premières graines expert sont solubles et dures`() {
        for (seed in ExpertSeeds.seeds.take(3)) assertTrue("graine $seed", isExpert(seed))
    }

    /**
     * Répartition des taux de victoire d'un joueur au hasard sur les donnes au hasard
     * solubles, pour choisir le seuil du mode expert. Lancé par poc/donnes-expert/evaluer.sh :
     * MAHJONG_EXPERT=1000 ./gradlew :core:test --tests '*ExpertTest.répartition*' -i
     */
    @Test
    fun `répartition des taux de victoire`() {
        val count = System.getenv("MAHJONG_EXPERT")?.toIntOrNull()
        assumeTrue(count != null)
        val maxNodes = System.getenv("MAHJONG_EXPERT_NODES")?.toIntOrNull() ?: 1_000_000
        val games = System.getenv("MAHJONG_EXPERT_GAMES")?.toIntOrNull() ?: 400

        class Run(val seed: Int, val nodes: Int, val rate: Double)
        // Une donne par graine, indépendante des autres : calcul en parallèle.
        val runs = IntStream.range(0, count!!).parallel().mapToObj { seed ->
            val board = Dealer.dealRandom(Layouts.TURTLE, Random(seed))
            val solver = Solver(board, maxNodes)
            if (solver.solve() is Solution.Found) Run(seed, solver.nodes, Playouts(board).winRate(games, Random(seed))) else null
        }.toList().filterNotNull()
        val normal = IntStream.range(0, 200).parallel().mapToDouble { seed ->
            Playouts(Dealer.deal(Layouts.TURTLE, Random(seed)).board).winRate(games, Random(seed))
        }.average().asDouble

        fun pct(x: Double) = "%5.1f %%".format(100 * x)
        val served = runs.filter { it.nodes <= Dealer.SOLVER_BUDGET }
        println("=== $count donnes au hasard, budget $maxNodes plateaux, $games parties par donne ===")
        println("solubles : ${runs.size} (${pct(runs.size.toDouble() / count)} des tirages)")
        println("dont résolues en ${Dealer.SOLVER_BUDGET} plateaux (mode difficile actuel) : ${served.size}")
        println("victoires moyennes : normales ${pct(normal)}, difficiles actuelles ${pct(served.map { it.rate }.average())}, solubles ${pct(runs.map { it.rate }.average())}")

        val rates = runs.map { it.rate }.sorted()
        println("quantiles des solubles : " + listOf(0.1, 0.25, 0.5, 0.75, 0.9).joinToString("  ") { q ->
            "p${(q * 100).toInt()} ${pct(rates[((rates.size - 1) * q).toInt()])}"
        })
        println("seuil (victoires <=)  solubles  tirages  difficiles actuelles  plateaux médians")
        for (t in listOf(0.0, 0.0025, 0.005, 0.01, 0.02, 0.03, 0.05, 0.1)) {
            val kept = runs.filter { it.rate <= t + 1e-9 }
            val nodes = kept.map { it.nodes }.sorted().let { if (it.isEmpty()) 0 else it[it.size / 2] }
            println(
                "%19s  %8s  %7s  %20s  %16d".format(
                    pct(t), pct(kept.size.toDouble() / runs.size), pct(kept.size.toDouble() / count),
                    pct(served.count { it.rate <= t + 1e-9 }.toDouble() / served.size), nodes,
                ),
            )
        }
    }
}
