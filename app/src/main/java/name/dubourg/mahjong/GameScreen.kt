package name.dubourg.mahjong

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.unit.sp
import name.dubourg.mahjong.core.Board
import name.dubourg.mahjong.core.Dealer
import name.dubourg.mahjong.core.Face
import name.dubourg.mahjong.core.Game
import name.dubourg.mahjong.core.Layouts
import name.dubourg.mahjong.core.Position
import kotlin.math.min
import kotlin.random.Random

/** Ordre de dessin : niveau par niveau, de droite à gauche puis de haut en bas. */
private val drawOrder = compareBy<Position> { it.z }.thenByDescending { it.x }.thenBy { it.y }

internal val TextColor = Color(0xFFF1ECE2)
internal val ButtonColor = Color(0x33FFFFFF)

/** Partie en cours, distribuée en mode difficile ou non, avec son historique et la tuile sélectionnée. */
private class GameState(board: Board, val hard: Boolean) {
    private var game by mutableStateOf(Game(board))
    var selected by mutableStateOf<Position?>(null)

    val board: Board get() = game.board
    val canUndo: Boolean get() = game.canUndo

    /** Toucher une tuile : la sélectionner, ou retirer la paire si elle s'apparie. */
    fun tap(p: Position) {
        val current = selected
        when {
            !board.isFree(p) -> Unit
            current == p -> selected = null
            current != null && board.canRemove(current, p) -> {
                game = game.play(current, p)
                selected = null
            }
            else -> selected = p
        }
    }

    /** Annuler le dernier coup ; la sélection en cours est abandonnée. */
    fun undo() {
        if (!canUndo) return
        game = game.undo()
        selected = null
    }
}

/** Nouvelle donne : soluble, ou tirée au hasard en mode difficile. */
private fun newGame(hard: Boolean) = GameState(
    if (hard) Dealer.dealRandom(Layouts.TURTLE, Random.Default)
    else Dealer.deal(Layouts.TURTLE, Random.Default).board,
    hard,
)

private fun modeName(hard: Boolean) = if (hard) "difficile" else "normal"

/** Taille des tuiles et position du plateau, pour qu'il tienne centré dans [canvas]. */
private class BoardLayout(positions: Collection<Position>, canvas: Size) {
    val metrics: TileMetrics
    val origin: Offset

    init {
        // Encombrement calculé pour une tuile de largeur 1 : ombre comprise à gauche, où
        // elle côtoie les commandes ; en bas, seule la tranche compte et l'ombre peut passer
        // sous le bord de l'écran, sans marge supplémentaire.
        val unit = TileMetrics(1f)
        val rects = positions.map { unit.faceRect(it.x, it.y, it.z) }
        val left = rects.minOf { it.left } - unit.thickness * 2.5f
        val right = rects.maxOf { it.right }
        val top = rects.minOf { it.top }
        val bottom = rects.maxOf { it.bottom } + unit.thickness
        val scale = 0.97f * min(canvas.width / (right - left), canvas.height / (bottom - top))
        metrics = TileMetrics(scale)
        origin = Offset(
            (canvas.width - (right - left) * scale) / 2f - left * scale,
            (canvas.height - (bottom - top) * scale) / 2f - top * scale,
        )
    }

    fun faceRect(p: Position) = metrics.faceRect(p.x, p.y, p.z).translate(origin)

    /** Bas visible de la rangée la plus basse : la face, plus l'épaisseur de la tuile. */
    fun bottom(positions: Collection<Position>) = positions.maxOf { faceRect(it).bottom } + metrics.thickness
}

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val settings = remember { Settings(context.applicationContext) }
    // Changer de mode ne touche pas la partie en cours : à la sortie des options, on
    // propose une nouvelle donne ; si le joueur la refuse, le mode vaut pour la suivante.
    var game by remember { mutableStateOf(newGame(settings.hard)) }
    var showOptions by remember { mutableStateOf(false) }
    var offerNewDeal by remember { mutableStateOf(false) }
    var boardSize by remember { mutableStateOf(Size.Zero) }
    // Marge sous les commandes pour aligner le bas du dernier bouton sur la rangée la plus
    // basse de la tortue (plateau et colonne partagent le même haut).
    val bottomGap = with(LocalDensity.current) {
        if (boardSize == Size.Zero) 12.dp
        else (boardSize.height - BoardLayout(Layouts.TURTLE.positions, boardSize)
            .bottom(Layouts.TURTLE.positions)).toDp().coerceAtLeast(0.dp)
    }
    val board = game.board
    val pairs = board.playablePairs().size

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF2B6E55), Color(0xFF0D2B21)), radius = 2600f)),
    ) {
        Row(Modifier.fillMaxSize().safeDrawingPadding()) {
            // Le plateau est limité par la hauteur : les commandes prennent place à gauche.
            Column(
                Modifier.fillMaxHeight().width(150.dp)
                    .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = bottomGap),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Label("Tuiles\n${board.tiles.size}")
                Label("Paires jouables\n$pairs")
                Label("Mode\n${modeName(game.hard).replaceFirstChar { it.uppercase() }}")
                ActionButton("Nouvelle donne") { game = newGame(settings.hard) }
                ActionButton("Annuler", enabled = game.canUndo) { game.undo() }
                Spacer(Modifier.weight(1f))
                ActionButton("Options") { showOptions = true }
            }
            BoardCanvas(game, Modifier.fillMaxSize().onSizeChanged { boardSize = it.toSize() })
        }

        val message = when {
            board.tiles.isEmpty() -> "Gagné !"
            pairs == 0 -> "Plus aucune paire jouable"
            else -> null
        }
        if (message != null) {
            Column(
                Modifier
                    .align(Alignment.Center)
                    .background(Color(0xCC0D2B21), RoundedCornerShape(16.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                BasicText(message, style = TextStyle(TextColor, 28.sp, FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Bloqué : revenir en arrière vaut souvent mieux que tout recommencer.
                    if (board.tiles.isNotEmpty()) ActionButton("Annuler") { game.undo() }
                    ActionButton("Nouvelle donne") { game = newGame(settings.hard) }
                }
            }
        }

        if (showOptions) OptionsPanel(settings) {
            showOptions = false
            offerNewDeal = settings.hard != game.hard
        }

        if (offerNewDeal) {
            ModalPanel(onDismiss = { offerNewDeal = false }) {
                BasicText(
                    "Vous êtes passé en mode ${modeName(settings.hard)}.\nSouhaitez-vous une nouvelle donne ?",
                    style = TextStyle(TextColor, 22.sp, FontWeight.Bold, textAlign = TextAlign.Center),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionButton("Continuer la partie") { offerNewDeal = false }
                    ActionButton("Nouvelle donne") {
                        game = newGame(settings.hard)
                        offerNewDeal = false
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCanvas(game: GameState, modifier: Modifier) {
    val painters = Face.fullSet().distinct().associateWith { painterResource(it.drawable) }
    // Images de la tuile nue, refaites seulement quand la taille des tuiles change.
    val spriteCache = remember { arrayOfNulls<TileSprites>(1) }
    Canvas(
        // Clé game : le détecteur repart à chaque nouvelle partie.
        modifier.pointerInput(game) {
            // Appui puis relâché, sans délai d'appui long : un rendu lent ne fait pas
            // perdre le toucher.
            awaitEachGesture {
                val down = awaitFirstDown()
                if (waitForUpOrCancellation() == null) return@awaitEachGesture
                val canvas = Size(size.width.toFloat(), size.height.toFloat())
                val layout = BoardLayout(Layouts.TURTLE.positions, canvas)
                // La tuile visée est la dernière dessinée sous le doigt, donc la plus haute.
                game.board.tiles.keys.sortedWith(drawOrder)
                    .lastOrNull { layout.faceRect(it).contains(down.position) }
                    ?.let(game::tap)
            }
        },
    ) {
        val board = game.board
        if (board.tiles.isEmpty()) return@Canvas
        // Cadrage calculé sur la disposition complète : le plateau ne bouge pas en cours de partie.
        val layout = BoardLayout(Layouts.TURTLE.positions, size)
        val sprites = spriteCache[0]?.takeIf { it.metrics == layout.metrics }
            ?: TileSprites(layout.metrics).also { spriteCache[0] = it }
        for (p in board.tiles.keys.sortedWith(drawOrder)) {
            drawTile(
                layout.faceRect(p),
                sprites,
                painters.getValue(board.tiles.getValue(p)),
                selected = p == game.selected,
                dimmed = !board.isFree(p),
            )
        }
    }
}

@Composable
private fun Label(text: String) {
    BasicText(text, style = TextStyle(TextColor, 16.sp))
}

@Composable
internal fun ActionButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    BasicText(
        text,
        Modifier
            .background(ButtonColor, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        style = TextStyle(if (enabled) TextColor else TextColor.copy(alpha = 0.35f), 16.sp, FontWeight.Medium),
    )
}
