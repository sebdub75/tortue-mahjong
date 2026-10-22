package name.dubourg.mahjong.core

/**
 * Famille de tuiles, avec le nombre de valeurs qu'elle contient et le nombre
 * d'exemplaires de chaque valeur dans le jeu complet (composition de *Mahjong HD*).
 */
enum class Suit(val size: Int, val copies: Int) {
    CHARACTERS(9, 4),
    DOTS(9, 4),
    BAMBOO(9, 4),
    WIND(4, 2),   // est, sud, ouest, nord : une seule paire par vent
    DRAGON(3, 4), // rouge, vert, blanc
    FLOWER(4, 2),
    SEASON(4, 2),
}

/** Motif d'une tuile : une famille et une valeur, de 1 à [Suit.size]. */
data class Face(val suit: Suit, val rank: Int) {
    init {
        require(rank in 1..suit.size) { "Valeur $rank hors de la famille $suit" }
    }

    /** Deux tuiles s'apparient si elles sont identiques, ou si ce sont deux fleurs ou deux saisons. */
    fun matches(other: Face): Boolean =
        suit == other.suit && (rank == other.rank || suit == Suit.FLOWER || suit == Suit.SEASON)

    companion object {
        /**
         * Les 144 tuiles : 4 exemplaires des chiffres et des dragons (120 tuiles),
         * 2 exemplaires des vents, des fleurs et des saisons (3 × 8 tuiles).
         */
        fun fullSet(): List<Face> = Suit.entries.flatMap { suit ->
            (1..suit.size).flatMap { rank -> List(suit.copies) { Face(suit, rank) } }
        }
    }
}
