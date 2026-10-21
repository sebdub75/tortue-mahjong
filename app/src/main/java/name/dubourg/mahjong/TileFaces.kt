package name.dubourg.mahjong

import androidx.annotation.DrawableRes
import name.dubourg.mahjong.core.Face
import name.dubourg.mahjong.core.Suit

private val characters = intArrayOf(
    R.drawable.tile_man1, R.drawable.tile_man2, R.drawable.tile_man3,
    R.drawable.tile_man4, R.drawable.tile_man5, R.drawable.tile_man6,
    R.drawable.tile_man7, R.drawable.tile_man8, R.drawable.tile_man9,
)
private val dots = intArrayOf(
    R.drawable.tile_pin1, R.drawable.tile_pin2, R.drawable.tile_pin3,
    R.drawable.tile_pin4, R.drawable.tile_pin5, R.drawable.tile_pin6,
    R.drawable.tile_pin7, R.drawable.tile_pin8, R.drawable.tile_pin9,
)
private val bamboo = intArrayOf(
    R.drawable.tile_sou1, R.drawable.tile_sou2, R.drawable.tile_sou3,
    R.drawable.tile_sou4, R.drawable.tile_sou5, R.drawable.tile_sou6,
    R.drawable.tile_sou7, R.drawable.tile_sou8, R.drawable.tile_sou9,
)
// Est, sud, ouest, nord.
private val winds = intArrayOf(R.drawable.tile_ton, R.drawable.tile_nan, R.drawable.tile_shaa, R.drawable.tile_pei)
// Rouge, vert, blanc.
private val dragons = intArrayOf(R.drawable.tile_chun, R.drawable.tile_hatsu, R.drawable.tile_haku)
private val flowers = intArrayOf(
    R.drawable.tile_flower1, R.drawable.tile_flower2, R.drawable.tile_flower3, R.drawable.tile_flower4,
)
private val seasons = intArrayOf(
    R.drawable.tile_season1, R.drawable.tile_season2, R.drawable.tile_season3, R.drawable.tile_season4,
)

/** Symbole VectorDrawable d'une face. */
@get:DrawableRes
val Face.drawable: Int
    get() = when (suit) {
        Suit.CHARACTERS -> characters
        Suit.DOTS -> dots
        Suit.BAMBOO -> bamboo
        Suit.WIND -> winds
        Suit.DRAGON -> dragons
        Suit.FLOWER -> flowers
        Suit.SEASON -> seasons
    }[rank - 1]
