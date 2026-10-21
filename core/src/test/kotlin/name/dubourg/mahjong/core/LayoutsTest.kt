package name.dubourg.mahjong.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class LayoutsTest {

    private val turtle = Layouts.TURTLE

    @Test
    fun `la tortue compte 144 positions distinctes`() {
        assertEquals(144, turtle.positions.size)
        assertEquals(144, turtle.positions.toSet().size)
    }

    @Test
    fun `la tortue a 87, 36, 16, 4 puis 1 tuiles par niveau`() {
        val perLevel = turtle.positions.groupingBy { it.z }.eachCount().toSortedMap()
        assertEquals(mapOf(0 to 87, 1 to 36, 2 to 16, 3 to 4, 4 to 1), perLevel)
    }

    @Test
    fun `aucune tuile n'en chevauche une autre au même niveau`() {
        for (a in turtle.positions) for (b in turtle.positions) {
            if (a == b || a.z != b.z) continue
            assertTrue("$a chevauche $b", abs(a.x - b.x) >= 2 || abs(a.y - b.y) >= 2)
        }
    }

    @Test
    fun `chaque tuile surélevée repose entièrement sur le niveau inférieur`() {
        for (p in turtle.positions.filter { it.z > 0 }) {
            // Les quatre demi-cases couvertes par la tuile doivent être soutenues.
            for (cx in p.x..p.x + 1) for (cy in p.y..p.y + 1) {
                val supported = turtle.positions.any {
                    it.z == p.z - 1 && cx in it.x..it.x + 1 && cy in it.y..it.y + 1
                }
                assertTrue("$p porte à faux en ($cx, $cy)", supported)
            }
        }
    }

    @Test
    fun `au départ, la tortue a 35 tuiles libres`() {
        val board = Board(turtle.positions.associateWith { Face(Suit.DOTS, 1) })
        assertEquals(35, turtle.positions.count { board.isFree(it) })
    }

    @Test
    fun `le sommet et les deux extrémités sont libres au départ`() {
        val board = Board(turtle.positions.associateWith { Face(Suit.DOTS, 1) })
        assertTrue(board.isFree(Position(13, 7, 4)))
        assertTrue(board.isFree(Position(0, 7, 0)))
        assertTrue(board.isFree(Position(28, 7, 0)))
    }
}
