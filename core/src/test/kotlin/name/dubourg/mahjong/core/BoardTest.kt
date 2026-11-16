package name.dubourg.mahjong.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Coordonnées en demi-tuiles : deux tuiles côte à côte sont à x et x + 2. */
class BoardTest {

    private val man1 = Face(Suit.CHARACTERS, 1)

    private fun board(vararg positions: Position) = Board(positions.associateWith { man1 })

    @Test
    fun `une tuile seule est libre`() {
        val p = Position(0, 0, 0)
        assertTrue(board(p).isFree(p))
    }

    @Test
    fun `une tuile encadrée à gauche et à droite est bloquée`() {
        val p = Position(2, 0, 0)
        assertFalse(board(Position(0, 0, 0), p, Position(4, 0, 0)).isFree(p))
    }

    @Test
    fun `une tuile dégagée d'un seul côté est libre`() {
        val p = Position(2, 0, 0)
        assertTrue(board(Position(0, 0, 0), p).isFree(p))
        assertTrue(board(p, Position(4, 0, 0)).isFree(p))
    }

    @Test
    fun `une voisine décalée d'une demi-tuile en hauteur bloque aussi`() {
        val p = Position(2, 0, 0)
        assertFalse(board(Position(0, 1, 0), p, Position(4, -1, 0)).isFree(p))
    }

    @Test
    fun `une tuile sur la rangée voisine ne bloque pas`() {
        val p = Position(2, 0, 0)
        assertTrue(board(Position(0, 2, 0), p, Position(4, -2, 0)).isFree(p))
    }

    @Test
    fun `une tuile recouverte, même en partie, est bloquée`() {
        val p = Position(0, 0, 0)
        assertFalse(board(p, Position(0, 0, 1)).isFree(p))
        assertFalse(board(p, Position(1, 1, 1)).isFree(p))
        assertFalse(board(p, Position(-1, -1, 1)).isFree(p))
    }

    @Test
    fun `une tuile posée à côté au niveau supérieur ne recouvre pas`() {
        val p = Position(0, 0, 0)
        assertTrue(board(p, Position(2, 0, 1)).isFree(p))
        assertTrue(board(p, Position(0, 2, 1)).isFree(p))
    }

    @Test
    fun `seules les tuiles du même niveau bloquent les côtés`() {
        val p = Position(2, 0, 1)
        assertTrue(board(Position(0, 0, 0), p, Position(4, 0, 0)).isFree(p))
    }

    @Test
    fun `une position vide n'est pas libre`() {
        assertFalse(board(Position(0, 0, 0)).isFree(Position(4, 0, 0)))
    }

    @Test
    fun `on retire deux tuiles libres qui s'apparient`() {
        val a = Position(0, 0, 0)
        val b = Position(4, 0, 0)
        val start = Board(mapOf(a to man1, b to man1))
        assertTrue(start.canRemove(a, b))
        assertEquals(emptyMap<Position, Face>(), start.remove(a, b).tiles)
    }

    @Test
    fun `on ne retire ni une tuile avec elle-même, ni une paire bloquée ou dépareillée`() {
        val a = Position(0, 0, 0)
        val b = Position(2, 0, 0)
        val c = Position(4, 0, 0)
        val other = Face(Suit.CHARACTERS, 2)
        assertFalse(Board(mapOf(a to man1)).canRemove(a, a))
        assertFalse(Board(mapOf(a to man1, b to man1, c to man1)).canRemove(a, b))
        assertFalse(Board(mapOf(a to man1, c to other)).canRemove(a, c))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `retirer une paire interdite est une erreur`() {
        val a = Position(0, 0, 0)
        Board(mapOf(a to man1, Position(0, 0, 1) to man1)).remove(a, Position(0, 0, 1))
    }

    @Test
    fun `les paires jouables sont listées`() {
        val a = Position(0, 0, 0)
        val b = Position(4, 0, 0)
        val c = Position(8, 0, 0)
        val start = Board(mapOf(a to man1, b to Face(Suit.DOTS, 1), c to man1))
        assertEquals(listOf(a to c), start.playablePairs())
    }

    @Test
    fun `les paires jouables sont groupées par motif, fleurs ensemble`() {
        // Tuiles isolées (espacées de deux tuiles), donc toutes libres ; posées dans le
        // désordre des motifs.
        val faces = listOf(
            Face(Suit.FLOWER, 2), Face(Suit.DOTS, 3), Face(Suit.CHARACTERS, 5),
            Face(Suit.FLOWER, 4), Face(Suit.DOTS, 3), Face(Suit.CHARACTERS, 5),
            Face(Suit.FLOWER, 1), Face(Suit.CHARACTERS, 2), Face(Suit.CHARACTERS, 2),
        )
        val p = faces.indices.map { Position(it * 4, 0, 0) }
        val start = Board(p.zip(faces).toMap())
        assertEquals(
            listOf(p[7] to p[8], p[2] to p[5], p[1] to p[4], p[0] to p[3], p[0] to p[6], p[3] to p[6]),
            start.playablePairs(),
        )
    }

    @Test
    fun `les paires d'une tuile donnée passent en tête, dans l'ordre des motifs`() {
        val faces = listOf(
            Face(Suit.FLOWER, 2), Face(Suit.DOTS, 3), Face(Suit.CHARACTERS, 5),
            Face(Suit.FLOWER, 4), Face(Suit.DOTS, 3), Face(Suit.CHARACTERS, 5),
            Face(Suit.FLOWER, 1), Face(Suit.CHARACTERS, 2), Face(Suit.CHARACTERS, 2),
        )
        val p = faces.indices.map { Position(it * 4, 0, 0) }
        val start = Board(p.zip(faces).toMap())
        assertEquals(
            listOf(p[0] to p[6], p[3] to p[6], p[7] to p[8], p[2] to p[5], p[1] to p[4], p[0] to p[3]),
            start.playablePairs(first = p[6]),
        )
    }

    @Test
    fun `trois tuiles libres identiques font une seule paire au compteur`() {
        val p = (0 until 3).map { Position(it * 4, 0, 0) }
        val start = Board(p.associateWith { man1 })
        assertEquals(3, start.playablePairs().size)
        assertEquals(1, start.playablePairCount())
    }

    @Test
    fun `quatre tuiles libres identiques font deux paires au compteur`() {
        val p = (0 until 4).map { Position(it * 4, 0, 0) }
        val start = Board(p.associateWith { man1 })
        assertEquals(6, start.playablePairs().size)
        assertEquals(2, start.playablePairCount())
    }

    @Test
    fun `le compteur regroupe les fleurs entre elles et ignore les tuiles bloquées`() {
        // Trois fleurs différentes : une paire. Deux 1 caractère, dont un coincé entre
        // deux voisines : aucune paire.
        val faces = listOf(Face(Suit.FLOWER, 1), Face(Suit.FLOWER, 2), Face(Suit.FLOWER, 3), man1)
        val isolated = faces.indices.map { Position(it * 4, 0, 0) }
        val blocked = listOf(Position(20, 0, 0), Position(22, 0, 0), Position(24, 0, 0))
        val start = Board(
            isolated.zip(faces).toMap() +
                mapOf(blocked[0] to Face(Suit.DOTS, 1), blocked[1] to man1, blocked[2] to Face(Suit.DOTS, 2)),
        )
        assertEquals(1, start.playablePairCount())
    }

    @Test
    fun `de 2 à 8 fleurs libres, le compteur donne n sur 2 paires`() = checkFreeGroup(Suit.FLOWER)

    @Test
    fun `de 2 à 8 saisons libres, le compteur donne n sur 2 paires`() = checkFreeGroup(Suit.SEASON)

    /**
     * n tuiles libres de la famille, de 2 à 8 (toute la famille) : n / 2 paires au
     * compteur, mais n (n - 1) / 2 combinaisons, puisqu'elles s'apparient toutes entre
     * elles. Les valeurs tournent de 1 à 4, chacune en 2 exemplaires au plus, comme dans
     * le jeu.
     */
    private fun checkFreeGroup(suit: Suit) {
        for (n in 2..8) {
            val start = Board((0 until n).associate { Position(it * 4, 0, 0) to Face(suit, it % 4 + 1) })
            assertEquals("$n tuiles $suit : paires", n / 2, start.playablePairCount())
            assertEquals("$n tuiles $suit : combinaisons", n * (n - 1) / 2, start.playablePairs().size)
        }
    }

    @Test
    fun `une tuile sans paire jouable laisse l'ordre des motifs`() {
        val a = Position(0, 0, 0)
        val b = Position(4, 0, 0)
        val c = Position(8, 0, 0)
        val start = Board(mapOf(a to man1, b to Face(Suit.DOTS, 1), c to man1))
        assertEquals(start.playablePairs(), start.playablePairs(first = b))
        assertEquals(start.playablePairs(), start.playablePairs(first = null))
    }
}
