package name.dubourg.mahjong.core

/** Disposition des tuiles sur le plateau, indépendante des faces. */
class Layout(val name: String, val positions: List<Position>)

/** Rangée horizontale de [count] tuiles jointives, la première en (x, y, z), en demi-tuiles. */
private data class Row(val z: Int, val y: Int, val x: Int, val count: Int = 1) {
    fun positions() = List(count) { Position(x + 2 * it, y, z) }
}

private fun layout(name: String, vararg rows: Row) = Layout(name, rows.flatMap { it.positions() })

object Layouts {

    /**
     * La tortue classique : 144 tuiles sur 5 niveaux, 15 tuiles de large et 8 de haut.
     * Les coordonnées sont en demi-tuiles.
     */
    val TURTLE = layout(
        "Tortue",
        // Niveau 0 : 87 tuiles.
        Row(z = 0, y = 0, x = 2, count = 12),
        Row(z = 0, y = 2, x = 6, count = 8),
        Row(z = 0, y = 4, x = 4, count = 10),
        Row(z = 0, y = 6, x = 2, count = 12),
        Row(z = 0, y = 8, x = 2, count = 12),
        Row(z = 0, y = 10, x = 4, count = 10),
        Row(z = 0, y = 12, x = 6, count = 8),
        Row(z = 0, y = 14, x = 2, count = 12),
        Row(z = 0, y = 7, x = 0),            // extrémité gauche, entre deux rangées
        Row(z = 0, y = 7, x = 26, count = 2), // extrémité droite
        // Niveau 1 : carré de 6 × 6.
        Row(z = 1, y = 2, x = 8, count = 6),
        Row(z = 1, y = 4, x = 8, count = 6),
        Row(z = 1, y = 6, x = 8, count = 6),
        Row(z = 1, y = 8, x = 8, count = 6),
        Row(z = 1, y = 10, x = 8, count = 6),
        Row(z = 1, y = 12, x = 8, count = 6),
        // Niveau 2 : carré de 4 × 4.
        Row(z = 2, y = 4, x = 10, count = 4),
        Row(z = 2, y = 6, x = 10, count = 4),
        Row(z = 2, y = 8, x = 10, count = 4),
        Row(z = 2, y = 10, x = 10, count = 4),
        // Niveau 3 : carré de 2 × 2.
        Row(z = 3, y = 6, x = 12, count = 2),
        Row(z = 3, y = 8, x = 12, count = 2),
        // Niveau 4 : le sommet, à cheval sur les quatre tuiles du niveau 3.
        Row(z = 4, y = 7, x = 13),
    )
}
