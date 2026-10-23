package name.dubourg.mahjong.core

/**
 * Partie : le plateau courant et les plateaux qui l'ont précédé, pour annuler les
 * coups un à un jusqu'à la donne. Immuable.
 *
 * L'historique garde les plateaux eux-mêmes plutôt que les paires retirées : annuler
 * rend exactement le plateau d'avant, ordre des tuiles compris, sans recalcul.
 */
class Game private constructor(
    val board: Board,
    private val previous: Game?,
    private val move: Pair<Position, Position>?,
) {

    constructor(board: Board) : this(board, null, null)

    /** Vrai s'il reste au moins un coup à annuler. */
    val canUndo: Boolean get() = previous != null

    /** Partie après le retrait de la paire [a], [b], qui doit pouvoir être retirée. */
    fun play(a: Position, b: Position): Game = Game(board.remove(a, b), this, a to b)

    /** Partie telle qu'avant le dernier coup. */
    fun undo(): Game = checkNotNull(previous) { "Aucun coup à annuler" }

    /**
     * Partie en texte, pour la sauvegarde : une ligne pour la donne, tuile après tuile
     * dans l'ordre du plateau, une ligne pour les coups joués, du premier au dernier.
     * [decode] la reconstruit en rejouant les coups.
     */
    fun encode(): String {
        val games = generateSequence(this) { it.previous }.toList().asReversed()
        val deal = games.first().board.tiles.entries.joinToString(";") { (p, face) ->
            "${p.encode()},${face.suit.name},${face.rank}"
        }
        val moves = games.mapNotNull { it.move }.joinToString(";") { (a, b) -> "${a.encode()},${b.encode()}" }
        return "$deal\n$moves"
    }

    companion object {
        /** Partie décrite par [text], produit par [encode] ; IllegalArgumentException s'il est invalide. */
        fun decode(text: String): Game {
            val lines = text.split("\n")
            require(lines.size == 2) { "Partie illisible" }
            val tiles = fields(lines[0]).associate { f ->
                require(f.size == 5) { "Tuile illisible : $f" }
                position(f, 0) to Face(Suit.valueOf(f[3]), f[4].toInt())
            }
            return fields(lines[1]).fold(Game(Board(tiles))) { game, f ->
                require(f.size == 6) { "Coup illisible : $f" }
                game.play(position(f, 0), position(f, 3))
            }
        }

        private fun fields(line: String) = if (line.isEmpty()) emptyList() else line.split(";").map { it.split(",") }

        private fun position(f: List<String>, from: Int) = Position(f[from].toInt(), f[from + 1].toInt(), f[from + 2].toInt())

        private fun Position.encode() = "$x,$y,$z"
    }
}
