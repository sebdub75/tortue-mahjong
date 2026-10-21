package name.dubourg.mahjong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tapis sombre : icônes claires dans les barres système.
        val bars = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        setContent { Maquette() }
    }
}

/** Une tuile posée : x et y en demi-tuiles, z le niveau (0 = table). */
data class PlacedTile(val x: Int, val y: Int, val z: Int, @DrawableRes val face: Int)

/** Petite pyramide de 21 tuiles sur 4 niveaux, pour juger du rendu en relief. */
private val scene: List<PlacedTile> = buildList {
    val level0 = listOf(
        R.drawable.tile_man1, R.drawable.tile_pin5, R.drawable.tile_sou7, R.drawable.tile_ton,
        R.drawable.tile_chun, R.drawable.tile_hatsu, R.drawable.tile_flower1, R.drawable.tile_season2,
        R.drawable.tile_man9, R.drawable.tile_pin1, R.drawable.tile_sou1, R.drawable.tile_haku,
    )
    for (row in 0 until 3) for (col in 0 until 4) add(PlacedTile(col * 2, row * 2, 0, level0[row * 4 + col]))
    val level1 = listOf(
        R.drawable.tile_pin2, R.drawable.tile_sou5, R.drawable.tile_nan,
        R.drawable.tile_man3, R.drawable.tile_flower3, R.drawable.tile_season4,
    )
    for (row in 0 until 2) for (col in 0 until 3) add(PlacedTile(1 + col * 2, 1 + row * 2, 1, level1[row * 3 + col]))
    add(PlacedTile(2, 2, 2, R.drawable.tile_pei))
    add(PlacedTile(4, 2, 2, R.drawable.tile_sou9))
    add(PlacedTile(3, 2, 3, R.drawable.tile_chun))
}.sortedWith(compareBy<PlacedTile> { it.z }.thenByDescending { it.x }.thenBy { it.y })

/** Taille des tuiles et position de la scène, pour qu'elle tienne centrée dans [canvas]. */
private class SceneLayout(tiles: List<PlacedTile>, canvas: Size) {
    val metrics: TileMetrics
    val origin: Offset

    init {
        // Encombrement calculé pour une tuile de largeur 1, ombre comprise.
        val unit = TileMetrics(1f)
        val rects = tiles.map { unit.faceRect(it.x, it.y, it.z) }
        val margin = unit.thickness * 2.5f
        val left = rects.minOf { it.left } - margin
        val right = rects.maxOf { it.right }
        val top = rects.minOf { it.top }
        val bottom = rects.maxOf { it.bottom } + margin
        val scale = 0.92f * min(canvas.width / (right - left), canvas.height / (bottom - top))
        metrics = TileMetrics(scale)
        origin = Offset(
            (canvas.width - (right - left) * scale) / 2f - left * scale,
            (canvas.height - (bottom - top) * scale) / 2f - top * scale,
        )
    }

    /** Tuile la plus haute sous [point], ou null. */
    fun hit(tiles: List<PlacedTile>, point: Offset): PlacedTile? =
        tiles.lastOrNull { metrics.faceRect(it.x, it.y, it.z).contains(point - origin) }
}

@Composable
private fun Maquette() {
    val painters = scene.map { it.face }.distinct().associateWith { painterResource(it) }
    var selected by remember { mutableStateOf(emptySet<PlacedTile>()) }

    Canvas(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF2B6E55), Color(0xFF0D2B21)), radius = 2200f))
            .pointerInput(Unit) {
                detectTapGestures { point ->
                    val canvas = Size(size.width.toFloat(), size.height.toFloat())
                    SceneLayout(scene, canvas).hit(scene, point)?.let { tile ->
                        selected = if (tile in selected) selected - tile else selected + tile
                    }
                }
            },
    ) {
        val layout = SceneLayout(scene, size)
        translate(layout.origin.x, layout.origin.y) {
            for (tile in scene) {
                drawTile(
                    layout.metrics.faceRect(tile.x, tile.y, tile.z),
                    layout.metrics,
                    painters.getValue(tile.face),
                    selected = tile in selected,
                )
            }
        }
    }
}
