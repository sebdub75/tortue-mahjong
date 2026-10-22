package name.dubourg.mahjong.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceTest {

    @Test
    fun `le jeu complet compte 144 tuiles et 42 faces`() {
        val set = Face.fullSet()
        assertEquals(144, set.size)
        assertEquals(42, set.toSet().size)
    }

    @Test
    fun `chiffres et dragons sont présents 4 fois, vents, fleurs et saisons 2 fois`() {
        val counts = Face.fullSet().groupingBy { it }.eachCount()
        for ((face, count) in counts) {
            val expected = when (face.suit) {
                Suit.WIND, Suit.FLOWER, Suit.SEASON -> 2
                else -> 4
            }
            assertEquals("$face", expected, count)
        }
    }

    @Test
    fun `le jeu complet compte 8 vents, 12 dragons, 8 fleurs et 8 saisons`() {
        val bySuit = Face.fullSet().groupingBy { it.suit }.eachCount()
        assertEquals(8, bySuit[Suit.WIND])
        assertEquals(8, bySuit[Suit.FLOWER])
        assertEquals(8, bySuit[Suit.SEASON])
        assertEquals(12, bySuit[Suit.DRAGON])
    }

    @Test
    fun `chaque vent n'a qu'une seule partenaire dans le jeu complet`() {
        val set = Face.fullSet()
        for (wind in (1..Suit.WIND.size).map { Face(Suit.WIND, it) }) {
            assertEquals("$wind", 2, set.count { it.matches(wind) })
        }
    }

    @Test
    fun `deux tuiles identiques s'apparient`() {
        assertTrue(Face(Suit.DOTS, 5).matches(Face(Suit.DOTS, 5)))
        assertTrue(Face(Suit.DRAGON, 1).matches(Face(Suit.DRAGON, 1)))
    }

    @Test
    fun `deux valeurs ou deux familles différentes ne s'apparient pas`() {
        assertFalse(Face(Suit.DOTS, 5).matches(Face(Suit.DOTS, 6)))
        assertFalse(Face(Suit.DOTS, 5).matches(Face(Suit.BAMBOO, 5)))
        assertFalse(Face(Suit.WIND, 1).matches(Face(Suit.DRAGON, 1)))
    }

    @Test
    fun `deux fleurs différentes s'apparient, de même deux saisons`() {
        assertTrue(Face(Suit.FLOWER, 1).matches(Face(Suit.FLOWER, 3)))
        assertTrue(Face(Suit.SEASON, 2).matches(Face(Suit.SEASON, 4)))
    }

    @Test
    fun `une fleur ne s'apparie pas avec une saison`() {
        assertFalse(Face(Suit.FLOWER, 1).matches(Face(Suit.SEASON, 1)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `une valeur hors de la famille est refusée`() {
        Face(Suit.WIND, 5)
    }
}
