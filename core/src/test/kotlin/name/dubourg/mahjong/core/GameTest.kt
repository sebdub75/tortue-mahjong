package name.dubourg.mahjong.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {

    private val man1 = Face(Suit.CHARACTERS, 1)
    private val pin1 = Face(Suit.DOTS, 1)

    // Deux paires côte à côte : a-b en man1, c-d en pin1, toutes jouables dans l'ordre a-b puis c-d.
    private val a = Position(0, 0, 0)
    private val b = Position(4, 0, 0)
    private val c = Position(0, 4, 0)
    private val d = Position(4, 4, 0)
    private val start = Board(mapOf(a to man1, b to man1, c to pin1, d to pin1))

    @Test
    fun `une partie neuve n'a rien à annuler`() {
        assertFalse(Game(start).canUndo)
    }

    @Test(expected = IllegalStateException::class)
    fun `annuler sans coup joué est une erreur`() {
        Game(start).undo()
    }

    @Test
    fun `jouer retire la paire et rend l'annulation possible`() {
        val game = Game(start).play(a, b)
        assertEquals(setOf(c, d), game.board.tiles.keys)
        assertTrue(game.canUndo)
    }

    @Test
    fun `annuler rend le plateau d'avant le coup`() {
        val game = Game(start).play(a, b).undo()
        assertSame(start, game.board)
        assertFalse(game.canUndo)
    }

    @Test
    fun `on annule un à un tous les coups, du dernier au premier`() {
        val first = Game(start).play(a, b)
        val second = first.play(c, d)
        assertTrue(second.board.tiles.isEmpty())

        val undoneOnce = second.undo()
        assertSame(first.board, undoneOnce.board)
        assertTrue(undoneOnce.canUndo)

        val undoneTwice = undoneOnce.undo()
        assertSame(start, undoneTwice.board)
        assertFalse(undoneTwice.canUndo)
    }

    @Test
    fun `l'annulation garde l'ordre des tuiles du plateau`() {
        val game = Game(start).play(a, b).undo()
        assertEquals(listOf(a, b, c, d), game.board.tiles.keys.toList())
    }

    @Test
    fun `on peut rejouer autrement après une annulation`() {
        val game = Game(start).play(a, b).undo().play(c, d)
        assertEquals(setOf(a, b), game.board.tiles.keys)
        assertSame(start, game.undo().board)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `jouer une paire interdite est une erreur`() {
        Game(start).play(a, c)
    }

    @Test
    fun `une partie relue a le même plateau et le même historique`() {
        val game = Game.decode(Game(start).play(a, b).encode())
        assertEquals(listOf(c, d), game.board.tiles.keys.toList())
        assertEquals(mapOf(a to man1, b to man1, c to pin1, d to pin1), game.undo().board.tiles)
        assertEquals(listOf(a, b, c, d), game.undo().board.tiles.keys.toList())
        assertFalse(game.undo().canUndo)
    }

    @Test
    fun `une partie sans coup se relit`() {
        val game = Game.decode(Game(start).encode())
        assertEquals(start.tiles, game.board.tiles)
        assertFalse(game.canUndo)
    }

    @Test
    fun `une donne complète se relit à l'identique`() {
        val deal = Dealer.deal(Layouts.TURTLE, kotlin.random.Random(7))
        val played = deal.solution.take(10).fold(Game(deal.board)) { g, (p, q) -> g.play(p, q) }
        val text = played.encode()
        assertEquals(text, Game.decode(text).encode())
        assertEquals(played.board.tiles.keys.toList(), Game.decode(text).board.tiles.keys.toList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un texte invalide est une erreur`() {
        Game.decode("n'importe quoi")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un coup interdit dans la sauvegarde est une erreur`() {
        Game.decode("0,0,0,CHARACTERS,1;4,0,0,DOTS,1\n0,0,0,4,0,0")
    }
}
