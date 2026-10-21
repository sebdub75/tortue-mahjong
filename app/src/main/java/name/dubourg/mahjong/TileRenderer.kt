package name.dubourg.mahjong

import android.graphics.BlurMaskFilter
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
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

/**
 * Dessine une tuile complète : ombre portée, tranche (dos rouge puis couche ivoire),
 * face et symbole. Les tuiles doivent être dessinées niveau par niveau, et dans un
 * niveau de droite à gauche puis de haut en bas, pour que chaque face recouvre la
 * tranche de ses voisines.
 */
fun DrawScope.drawTile(
    face: Rect,
    metrics: TileMetrics,
    symbol: Painter,
    selected: Boolean = false,
) {
    val d = metrics.depth
    val r = metrics.corner

    // Ombre : la silhouette de la tuile, prolongée et floutée sur le plan inférieur.
    val shadow = extrusion(face, r, d * 1.6f)
    drawIntoCanvas { canvas ->
        val paint = Paint().apply { color = ShadowColor }
        paint.asFrameworkPaint().maskFilter =
            BlurMaskFilter(metrics.thickness * 0.8f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawPath(shadow, paint)
    }

    // Tranche : dos rouge sur toute l'épaisseur, couche ivoire sous la face.
    val body = extrusion(face, r, d)
    drawPath(body, BackColor)
    drawPath(extrusion(face, r, d * 0.55f), SlabColor)

    // La tranche du bas, moins éclairée que celle de gauche.
    clipPath(belowDiagonal(Offset(face.left, face.bottom), d)) {
        drawPath(body, Color.Black.copy(alpha = 0.18f))
    }
    drawPath(body, Outline, style = Stroke(width = 1.5f))

    // Face.
    val faceShape = RoundRect(face, CornerRadius(r))
    val facePath = Path().apply { addRoundRect(faceShape) }
    drawPath(
        facePath,
        Brush.linearGradient(listOf(FaceLight, FaceDark), face.topRight, face.bottomLeft),
    )
    drawPath(facePath, FaceEdge, style = Stroke(width = 1.5f))

    // Symbole, au format 300 × 400 comme la face, légèrement réduit pour la marge.
    val inset = 0.06f
    translate(face.left + face.width * inset, face.top + face.height * inset) {
        with(symbol) {
            draw(Size(face.width * (1 - 2 * inset), face.height * (1 - 2 * inset)))
        }
    }

    if (selected) drawPath(facePath, Highlight)
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
