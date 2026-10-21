package name.dubourg.mahjong.core

/** Famille de tuiles, avec le nombre de valeurs qu'elle contient. */
enum class Suit(val size: Int) {
    CHARACTERS(9),
    DOTS(9),
    BAMBOO(9),
    WIND(4),   // est, sud, ouest, nord
    DRAGON(3), // rouge, vert, blanc
    FLOWER(4),
    SEASON(4),
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
        /** Les 144 tuiles : 4 exemplaires des 34 faces ordinaires, puis chaque fleur et chaque saison. */
        fun fullSet(): List<Face> = Suit.entries.flatMap { suit ->
            val copies = if (suit == Suit.FLOWER || suit == Suit.SEASON) 1 else 4
            (1..suit.size).flatMap { rank -> List(copies) { Face(suit, rank) } }
        }
    }
}
