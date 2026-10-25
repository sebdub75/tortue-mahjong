package name.dubourg.mahjong.core

/**
 * Graines des donnes du mode expert, calculées à l'avance sur PC (poc/donnes-expert) :
 * `Dealer.dealRandom(Layouts.TURTLE, Random(graine))` y est soluble et piégeuse. Le
 * générateur de `kotlin.random.Random` est le même sur toutes les plateformes, donc une
 * graine donne la même tortue sur PC et sur téléphone.
 */
object ExpertSeeds {
    /** Plateaux accordés au solveur pour reconnaître une donne soluble. */
    const val SOLVER_BUDGET = 1_000_000

    /** Parties jouées au hasard pour mesurer la difficulté d'une donne. */
    const val GAMES = 400

    /** Part de ces parties gagnées au plus : 1 %, le tiers le plus dur des donnes solubles. */
    const val MAX_WIN_RATE = 0.01

    /** Graines retenues, générées dans ExpertSeedList.kt. */
    val seeds: List<Int> get() = EXPERT_SEED_LIST.asList()
}
