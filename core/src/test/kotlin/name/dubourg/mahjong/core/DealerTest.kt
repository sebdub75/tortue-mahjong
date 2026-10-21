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
