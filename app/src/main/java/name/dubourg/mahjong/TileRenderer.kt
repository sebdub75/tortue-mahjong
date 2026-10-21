package name.dubourg.mahjong

import android.graphics.BlurMaskFilter
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Dimensions d'une tuile à l'écran, en pixels. La face fait 3 × 4 comme les SVG
 * FluffyStuff ; l'épaisseur est vue en perspective cavalière, vers le bas à gauche.
 */
data class TileMetrics(val width: Float) {
    val height = width * 4f / 3f
    val thickness = width * 0.14f
    val corner = width * 0.12f

    /** Décalage de la base de la tuile par rapport à sa face. */
    val depth = Offset(-thickness, thickness)

    /** Coin haut-gauche de la face d'une tuile en (x, y, z), x et y en demi-tuiles. */
    fun faceOrigin(x: Int, y: Int, z: Int) =
        Offset(x * width / 2f + z * thickness, y * height / 2f - z * thickness)

    fun faceRect(x: Int, y: Int, z: Int) = Rect(faceOrigin(x, y, z), Size(width, height))
}

private val FaceLight = Color(0xFFFBF8F4)
private val FaceDark = Color(0xFFE9E1D6)
private val FaceEdge = Color(0x40000000)
private val SlabColor = Color(0xFFDCD2C4)
private val BackColor = Color(0xFFA53C3C) // rouge du dos FluffyStuff (Back.svg)
private val Outline = Color(0x59000000)
private val ShadowColor = Color(0x66000000)
private val Highlight = Color(0x66FFD54F)
private val Dim = Color(0x2E000000)

/**
 * Images d'une tuile (ombre, puis corps et face avec chaque symbole), dessinées une
 * fois pour une taille donnée : toutes les tuiles ont la même forme, seul le symbole change.
 * Les décalages sont en pixels entiers, par rapport au coin haut-gauche de la face.
 */
class TileSprites(val metrics: TileMetrics) {
    val shadow: ImageBitmap
    val shadowOffset: Offset
    private val body: ImageBitmap
    val bodyOffset: Offset
    private val withSymbol = HashMap<Painter, ImageBitmap>()

    /** Contour de la face, coin haut-gauche en (0, 0), pour la sélection et l'assombrissement. */
    val facePath: Path

    init {
        val w = metrics.width
        val h = metrics.height
        val t = metrics.thickness
        val r = metrics.corner
        val face = Rect(0f, 0f, w, h)
        val d = metrics.depth
        facePath = Path().apply { addRoundRect(RoundRect(face, CornerRadius(r))) }

        // Ombre : la silhouette de la tuile, prolongée et floutée sur le plan inférieur.
        val blur = t * 0.8f
        val shadowMargin = ceil(blur * 2f)
        val shadowLeft = ceil(t * 1.6f) + shadowMargin
        shadowOffset = Offset(-shadowLeft, -shadowMargin)
        shadow = sprite(shadowLeft + w + shadowMargin, shadowMargin + h + ceil(t * 1.6f) + shadowMargin) {
            translate(shadowLeft, shadowMargin) {
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply { color = ShadowColor }
                    paint.asFrameworkPaint().maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
                    canvas.drawPath(extrusion(face, r, d * 1.6f), paint)
                }
            }
        }

        // Corps : tranche (dos rouge sur toute l'épaisseur, couche ivoire sous la face),
        // puis la face.
        val margin = 2f
        val bodyLeft = ceil(t) + margin
        bodyOffset = Offset(-bodyLeft, -margin)
        body = sprite(bodyLeft + w + margin, margin + h + ceil(t) + margin) {
            translate(bodyLeft, margin) {
                val hull = extrusion(face, r, d)
                drawPath(hull, BackColor)
                drawPath(extrusion(face, r, d * 0.55f), SlabColor)
                // La tranche du bas, moins éclairée que celle de gauche.
                clipPath(belowDiagonal(Offset(face.left, face.bottom), d)) {
                    drawPath(hull, Color.Black.copy(alpha = 0.18f))
                }
                drawPath(hull, Outline, style = Stroke(width = 1.5f))
                drawPath(facePath, Brush.linearGradient(listOf(FaceLight, FaceDark), face.topRight, face.bottomLeft))
                drawPath(facePath, FaceEdge, style = Stroke(width = 1.5f))
            }
        }
    }

    /**
     * Corps de la tuile avec [symbol] posé sur la face, dessiné au premier usage :
     * redessiner les symboles vectoriels à chaque image coûterait trop cher.
     */
    fun tile(symbol: Painter): ImageBitmap = withSymbol.getOrPut(symbol) {
        sprite(body.width.toFloat(), body.height.toFloat()) {
            drawImage(body)
            // Symbole, au format 300 × 400 comme la face, légèrement réduit pour la marge.
            val inset = 0.06f
            val w = metrics.width
            val h = metrics.height
            translate(-bodyOffset.x + w * inset, -bodyOffset.y + h * inset) {
                with(symbol) { draw(Size(w * (1 - 2 * inset), h * (1 - 2 * inset))) }
            }
        }
    }
}

/** Dessine [block] dans une nouvelle image de [width] × [height] pixels. */
private fun sprite(width: Float, height: Float, block: DrawScope.() -> Unit): ImageBitmap {
    val image = ImageBitmap(ceil(width).toInt(), ceil(height).toInt())
    val size = Size(image.width.toFloat(), image.height.toFloat())
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), size, block)
    return image
}

/**
 * Dessine une tuile complète dont la face occupe [face] : ombre portée, corps, symbole.
 * Une tuile [dimmed] (bloquée) a la face assombrie.
 *
 * Les tuiles doivent être dessinées niveau par niveau, et dans un niveau de droite à
 * gauche puis de haut en bas, pour que chaque face recouvre la tranche de ses voisines.
 */
fun DrawScope.drawTile(
    face: Rect,
    sprites: TileSprites,
    symbol: Painter,
    selected: Boolean = false,
    dimmed: Boolean = false,
) {
    // Calée sur le pixel, pour que les images ne soient pas rééchantillonnées.
    val origin = Offset(face.left.roundToInt().toFloat(), face.top.roundToInt().toFloat())
    drawImage(sprites.shadow, origin + sprites.shadowOffset)
    drawImage(sprites.tile(symbol), origin + sprites.bodyOffset)

    translate(origin.x, origin.y) {
        if (selected) drawPath(sprites.facePath, Highlight)
        if (dimmed) drawPath(sprites.facePath, Dim)
    }
}

/**
 * Volume balayé par le rectangle arrondi [rect] translaté de 0 à [d] : l'union des deux
 * extrémités et du parallélogramme qui les relie. [d] doit pointer vers le bas à
 * gauche ; les points de tangence sont alors sur les coins haut-gauche et bas-droit.
 */
private fun extrusion(rect: Rect, r: Float, d: Offset): Path {
    val k = r / sqrt(2f)
    val topLeft = Offset(rect.left + r - k, rect.top + r - k)
    val bottomRight = Offset(rect.right - r + k, rect.bottom - r + k)
    val band = Path().apply {
        moveTo(topLeft.x, topLeft.y)
        lineTo(bottomRight.x, bottomRight.y)
        lineTo(bottomRight.x + d.x, bottomRight.y + d.y)
        lineTo(topLeft.x + d.x, topLeft.y + d.y)
        close()
    }
    val start = Path().apply { addRoundRect(RoundRect(rect, CornerRadius(r))) }
    val end = Path().apply { addRoundRect(RoundRect(rect.translate(d), CornerRadius(r))) }
    return Path.combine(PathOperation.Union, Path.combine(PathOperation.Union, start, end), band)
}

/** Demi-plan situé sous la droite passant par [corner] dans la direction [d]. */
private fun belowDiagonal(corner: Offset, d: Offset): Path {
    val far = 1e4f
    val u = d / d.getDistance() * far
    val n = Offset(far, far)
    return Path().apply {
        moveTo(corner.x - u.x, corner.y - u.y)
        lineTo(corner.x + u.x, corner.y + u.y)
        lineTo(corner.x + u.x + n.x, corner.y + u.y + n.y)
        lineTo(corner.x - u.x + n.x, corner.y - u.y + n.y)
        close()
    }
}
