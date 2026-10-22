package name.dubourg.mahjong.core

/**
 * Partie : le plateau courant et les plateaux qui l'ont précédé, pour annuler les
 * coups un à un jusqu'à la donne. Immuable.
 *
 * L'historique garde les plateaux eux-mêmes plutôt que les paires retirées : annuler
 * rend exactement le plateau d'avant, ordre des tuiles compris, sans recalcul.
 */
class Game private constructor(val board: Board, private val previous: Game?) {

    constructor(board: Board) : this(board, null)

    /** Vrai s'il reste au moins un coup à annuler. */
    val canUndo: Boolean get() = previous != null

    /** Partie après le retrait de la paire [a], [b], qui doit pouvoir être retirée. */
    fun play(a: Position, b: Position): Game = Game(board.remove(a, b), this)

    /** Partie telle qu'avant le dernier coup. */
    fun undo(): Game = checkNotNull(previous) { "Aucun coup à annuler" }
}
