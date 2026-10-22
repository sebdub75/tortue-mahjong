package name.dubourg.mahjong.core

import kotlin.random.Random

/** Une donne : le plateau de départ et une suite de coups qui le vide. */
class Deal(val board: Board, val solution: List<Pair<Position, Position>>)

/**
 * Distribue les donnes : toujours solubles avec [deal], au hasard avec [dealRandom].
 *
 * On simule d'abord une partie sur la disposition seule, sans les faces : à chaque tour,
 * deux positions libres tirées au hasard sont retirées. Si l'on se retrouve avec moins de
 * deux positions libres avant la fin, on recommence. On obtient un ordre de retrait
 * valide, et chaque paire de cet ordre reçoit ensuite deux faces qui s'apparient.
 */
object Dealer {

    private const val MAX_ATTEMPTS = 1000

    fun deal(layout: Layout, random: Random): Deal {
        val positions = layout.positions
        checkSize(positions)

        val order = (1..MAX_ATTEMPTS).asSequence()
            .mapNotNull { removalOrder(positions, random) }
            .firstOrNull()
            ?: throw IllegalArgumentException("Disposition « ${layout.name} » impossible à vider")

        val faces = matchingPairs(random).take(order.size)
        val tiles = HashMap<Position, Face>()
        for ((pair, matching) in order.zip(faces)) {
            tiles[pair.first] = matching.first
            tiles[pair.second] = matching.second
        }
        // Les tuiles gardent l'ordre de la disposition, pour un plateau reproductible.
        return Deal(Board(positions.associateWith { tiles.getValue(it) }), order)
    }

    /**
     * Pose les tuiles du jeu, mélangées, sur les positions, sans chercher de solution :
     * la donne peut mener à une impasse quel que soit l'ordre des coups (mode difficile).
     */
    fun dealRandom(layout: Layout, random: Random): Board {
        val positions = layout.positions
        checkSize(positions)
        // Des paires entières : chaque tuile a sa partenaire, même sur une petite disposition.
        val faces = matchingPairs(random).take(positions.size / 2)
            .flatMap { listOf(it.first, it.second) }
            .shuffled(random)
        return Board(positions.zip(faces).toMap())
    }

    private fun checkSize(positions: List<Position>) {
        require(positions.size % 2 == 0) { "Nombre impair de positions : ${positions.size}" }
        require(positions.size <= Face.fullSet().size) { "Plus de positions que de tuiles" }
    }

    /** Un ordre de retrait par paires qui vide [positions], ou null si le hasard a mené à une impasse. */
    private fun removalOrder(positions: List<Position>, random: Random): List<Pair<Position, Position>>? {
        val remaining = positions.toMutableSet()
        val order = ArrayList<Pair<Position, Position>>(positions.size / 2)
        while (remaining.isNotEmpty()) {
            val free = remaining.filter { isFreeAmong(it, remaining) }
            if (free.size < 2) return null
            val (a, b) = free.shuffled(random).take(2)
            remaining -= a
            remaining -= b
            order += a to b
        }
        return order
    }

    /**
     * Les 72 paires du jeu complet, mélangées. Chaque groupe de tuiles qui s'apparient
     * est découpé en paires : quatre exemplaires d'une face donnent deux paires, les
     * deux exemplaires d'un vent une seule, les huit fleurs (ou saisons) quatre.
     */
    private fun matchingPairs(random: Random): List<Pair<Face, Face>> =
        Face.fullSet()
            .groupBy { if (it.suit == Suit.FLOWER || it.suit == Suit.SEASON) it.suit else it }
            .values
            .flatMap { group -> group.shuffled(random).chunked(2) { it[0] to it[1] } }
            .shuffled(random)
}
