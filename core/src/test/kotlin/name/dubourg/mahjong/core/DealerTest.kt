package name.dubourg.mahjong.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DealerTest {

    /** Rejoue la solution et vérifie que chaque coup est permis et que le plateau finit vide. */
    private fun assertSolvedBy(deal: Deal) {
        var board = deal.board
        for ((a, b) in deal.solution) {
            assertTrue("coup interdit : $a, $b", board.canRemove(a, b))
            board = board.remove(a, b)
        }
        assertTrue("il reste ${board.tiles.size} tuiles", board.tiles.isEmpty())
    }

    @Test
    fun `une donne de la tortue occupe toutes les positions avec le jeu complet`() {
        val deal = Dealer.deal(Layouts.TURTLE, Random(1))
        assertEquals(Layouts.TURTLE.positions.toSet(), deal.board.tiles.keys)
        assertEquals(
            Face.fullSet().groupingBy { it }.eachCount(),
            deal.board.tiles.values.groupingBy { it }.eachCount(),
        )
    }

    @Test
    fun `la solution fournie vide toujours la tortue`() {
        for (seed in 0 until 200) assertSolvedBy(Dealer.deal(Layouts.TURTLE, Random(seed)))
    }

    @Test
    fun `la même graine donne la même donne`() {
        assertEquals(
            Dealer.deal(Layouts.TURTLE, Random(42)).board.tiles,
            Dealer.deal(Layouts.TURTLE, Random(42)).board.tiles,
        )
    }

    @Test
    fun `deux graines donnent deux donnes différentes`() {
        assertNotEquals(
            Dealer.deal(Layouts.TURTLE, Random(1)).board.tiles,
            Dealer.deal(Layouts.TURTLE, Random(2)).board.tiles,
        )
    }

    @Test
    fun `une petite disposition reçoit des paires qui s'apparient`() {
        val row = Layout("Rangée", List(6) { Position(2 * it, 0, 0) })
        val deal = Dealer.deal(row, Random(3))
        assertEquals(6, deal.board.tiles.size)
        assertSolvedBy(deal)
    }

    @Test
    fun `un tirage au hasard occupe toutes les positions de la tortue avec le jeu complet`() {
        val board = Dealer.dealRandom(Layouts.TURTLE, Random(1))
        assertEquals(Layouts.TURTLE.positions.toSet(), board.tiles.keys)
        assertEquals(
            Face.fullSet().groupingBy { it }.eachCount(),
            board.tiles.values.groupingBy { it }.eachCount(),
        )
    }

    @Test
    fun `un tirage au hasard dépend de la graine`() {
        assertEquals(
            Dealer.dealRandom(Layouts.TURTLE, Random(42)).tiles,
            Dealer.dealRandom(Layouts.TURTLE, Random(42)).tiles,
        )
        assertNotEquals(
            Dealer.dealRandom(Layouts.TURTLE, Random(1)).tiles,
            Dealer.dealRandom(Layouts.TURTLE, Random(2)).tiles,
        )
    }

    @Test
    fun `un tirage au hasard ne garantit pas que la disposition se vide`() {
        // Refusée par deal() : le tirage au hasard, lui, ne cherche pas de solution.
        val board = Dealer.dealRandom(Layout("Pile", listOf(Position(0, 0, 0), Position(0, 0, 1))), Random(0))
        assertEquals(2, board.tiles.size)
        assertTrue(board.playablePairs().isEmpty())
    }

    @Test
    fun `un tirage au hasard sur une petite disposition ne pose que des paires`() {
        val row = Layout("Rangée", List(6) { Position(2 * it, 0, 0) })
        val faces = Dealer.dealRandom(row, Random(3)).tiles.values.toList()
        // Chaque tuile trouve sa partenaire : les tuiles assorties vont par nombre pair.
        for (face in faces) assertEquals("$face", 0, faces.count { it.matches(face) } % 2)
    }

    @Test
    fun `un tirage soluble occupe la tortue avec le jeu complet et sa solution la vide`() {
        for (seed in 0 until 20) {
            val deal = Dealer.dealSolvable(Layouts.TURTLE, Random(seed))
            assertEquals(Layouts.TURTLE.positions.toSet(), deal.board.tiles.keys)
            assertEquals(
                Face.fullSet().groupingBy { it }.eachCount(),
                deal.board.tiles.values.groupingBy { it }.eachCount(),
            )
            assertSolvedBy(deal)
        }
    }

    @Test
    fun `un tirage soluble dépend de la graine`() {
        assertEquals(
            Dealer.dealSolvable(Layouts.TURTLE, Random(42)).board.tiles,
            Dealer.dealSolvable(Layouts.TURTLE, Random(42)).board.tiles,
        )
        assertNotEquals(
            Dealer.dealSolvable(Layouts.TURTLE, Random(1)).board.tiles,
            Dealer.dealSolvable(Layouts.TURTLE, Random(2)).board.tiles,
        )
    }

    @Test
    fun `un tirage soluble écarte les tirages que le solveur ne vide pas`() {
        // Graines dont le premier tirage est insoluble ou non résolu dans le budget : la
        // donne retenue n'est pas ce premier tirage.
        val rejected = (0 until 50).first { seed ->
            Solver(Dealer.dealRandom(Layouts.TURTLE, Random(seed)), Dealer.SOLVER_BUDGET).solve() !is Solution.Found
        }
        assertNotEquals(
            Dealer.dealRandom(Layouts.TURTLE, Random(rejected)).tiles,
            Dealer.dealSolvable(Layouts.TURTLE, Random(rejected)).board.tiles,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un tirage soluble refuse une disposition impossible à vider`() {
        Dealer.dealSolvable(Layout("Pile", listOf(Position(0, 0, 0), Position(0, 0, 1))), Random(0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un tirage au hasard refuse un nombre impair de positions`() {
        Dealer.dealRandom(Layout("Impair", listOf(Position(0, 0, 0))), Random(0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un nombre impair de positions est refusé`() {
        Dealer.deal(Layout("Impair", listOf(Position(0, 0, 0))), Random(0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `une disposition impossible à vider est refusée`() {
        // Deux tuiles empilées : celle du dessous n'est jamais libre en même temps.
        Dealer.deal(Layout("Pile", listOf(Position(0, 0, 0), Position(0, 0, 1))), Random(0))
    }
}
