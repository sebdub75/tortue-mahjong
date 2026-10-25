package name.dubourg.mahjong.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PlayoutsTest {

    private val red = Face(Suit.DRAGON, 1)
    private val green = Face(Suit.DRAGON, 2)

    @Test
    fun `deux piles croisées sont toujours perdues`() {
        val board = Board(
            mapOf(
                Position(0, 0, 0) to red, Position(0, 0, 1) to green,
                Position(4, 0, 0) to green, Position(4, 0, 1) to red,
            ),
        )
        assertEquals(0.0, Playouts(board).winRate(20, Random(0)), 0.0)
    }

    @Test
    fun `deux piles parallèles sont toujours gagnées`() {
        val board = Board(
            mapOf(
                Position(0, 0, 0) to red, Position(0, 0, 1) to green,
                Position(4, 0, 0) to red, Position(4, 0, 1) to green,
            ),
        )
        assertEquals(1.0, Playouts(board).winRate(20, Random(0)), 0.0)
    }

    @Test
    fun `les donnes normales sont gagnées environ deux fois sur cinq`() {
        // Le banc du PoC, avec Board, mesurait 43 % sur 600 donnes.
        val rate = (0 until 30).map { Playouts(Dealer.deal(Layouts.TURTLE, Random(it)).board).winRate(100, Random(it)) }.average()
        assertTrue("taux $rate", rate in 0.3..0.56)
    }
}
