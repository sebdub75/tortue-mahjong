package name.dubourg.mahjong

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.random.Random

/** Ordre de dessin : niveau par niveau, de droite à gauche puis de haut en bas. */
private val drawOrder = compareBy<Position> { it.z }.thenByDescending { it.x }.thenBy { it.y }

internal val TextColor = Color(0xFFF1ECE2)
internal val ButtonColor = Color(0x33FFFFFF)

/**
 * Partie en cours, avec le mode dans lequel elle a été distribuée, son historique, la
 * tuile sélectionnée et le rang de la paire montrée par l'aide.
 */
private class GameState(game: Game, val mode: Mode) {
    var game by mutableStateOf(game)
        private set
    var selected by mutableStateOf<Position?>(null)

    /** Rang, dans [hintOrder], de la paire montrée par l'aide ; null sans aide. */
    var hint by mutableStateOf<Int?>(null)
        private set

    // Paires parcourues par l'aide, fixées au premier appui : toucher ensuite une tuile
    // suggérée la sélectionne sans rebattre l'ordre en cours de route.
    private var hintOrder = emptyList<Pair<Position, Position>>()

    val board: Board get() = game.board
    val canUndo: Boolean get() = game.canUndo
    val hintPair: Pair<Position, Position>? get() = hint?.let { hintOrder.getOrNull(it) }

    /** « rang/total » de l'aide : le total compte toutes les combinaisons parcourues. */
    val hintProgress: String? get() = hint?.let { "${it + 1}/${hintOrder.size}" }

    /**
     * Montrer la paire jouable suivante, en revenant à la première après la dernière. Au
     * premier appui, les paires de la tuile sélectionnée passent en tête.
     */
    fun nextHint() {
        if (hint == null) hintOrder = board.playablePairs(first = selected)
        if (hintOrder.isEmpty()) return
        hint = ((hint ?: -1) + 1) % hintOrder.size
    }

    /** Toucher le fond du plateau : abandonner la sélection et éteindre l'aide. */
    fun tapBackground() {
        selected = null
        hint = null
    }

    /**
     * Toucher une tuile : la sélectionner, ou retirer la paire si elle s'apparie.
     * Sélectionner une tuile hors de la paire suggérée éteint l'aide : au prochain appui,
     * elle repartira des paires de cette tuile.
     */
    fun tap(p: Position) {
        val current = selected
        when {
            !board.isFree(p) -> Unit
            current == p -> selected = null
            current != null && board.canRemove(current, p) -> {
                game = game.play(current, p)
                selected = null
                hint = null
            }
            else -> {
                selected = p
                val shown = hintPair
                if (shown != null && p != shown.first && p != shown.second) hint = null
            }
        }
    }

    /** Annuler le dernier coup ; la sélection en cours est abandonnée. */
    fun undo() {
        if (!canUndo) return
        game = game.undo()
        selected = null
        hint = null
    }
}

/**
 * Nouvelle donne : construite soluble en mode normal, tirée au hasard en mode difficile,
 * puis vérifiée par le solveur si la solution est garantie (plusieurs dizaines de
 * millisecondes), ou prise parmi les donnes expert calculées à l'avance.
 */
private fun newGame(mode: Mode) = GameState(
    Game(
        when (mode) {
            Mode.NORMAL -> Dealer.deal(Layouts.TURTLE, Random.Default).board
            Mode.HARD -> Dealer.dealSolvable(Layouts.TURTLE, Random.Default).board
            Mode.HARD_UNCHECKED -> Dealer.dealRandom(Layouts.TURTLE, Random.Default)
            Mode.EXPERT -> Dealer.dealExpert(Random.Default)
        },
    ),
    mode,
)

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
    val saved = remember { SavedGame(context.applicationContext) }
    // Changer de mode ne touche pas la partie en cours : à la sortie des options, on
    // propose une nouvelle donne ; si le joueur la refuse, le mode vaut pour la suivante.
    // Sans sauvegarde (premier lancement), la donne est distribuée sur place : le mode est
    // alors normal, sauf sauvegarde illisible.
    var game by remember {
        mutableStateOf(saved.load()?.let { (g, mode) -> GameState(g, mode) } ?: newGame(settings.mode))
    }
    // Enregistrement à chaque coup, annulation ou nouvelle donne : rien n'est perdu si
    // l'activité est recréée ou l'appli arrêtée.
    LaunchedEffect(game, game.game) { saved.save(game.game, game.mode) }
    // Le solveur tourne hors du thread UI ; un voile couvre l'écran le temps de la donne.
    val scope = rememberCoroutineScope()
    var dealing by remember { mutableStateOf(false) }
    fun deal() {
        if (dealing) return
        val mode = settings.mode
        dealing = true
        scope.launch {
            game = withContext(Dispatchers.Default) { newGame(mode) }
            dealing = false
        }
    }
    var showOptions by remember { mutableStateOf(false) }
    var offerNewDeal by remember { mutableStateOf(false) }
    var confirmNewDeal by remember { mutableStateOf(false) }
    var boardSize by remember { mutableStateOf(Size.Zero) }
    // Marge sous les commandes pour aligner le bas du dernier bouton sur la rangée la plus
    // basse de la tortue (plateau et colonne partagent le même haut).
    val bottomGap = with(LocalDensity.current) {
        if (boardSize == Size.Zero) 12.dp
        else (boardSize.height - BoardLayout(Layouts.TURTLE.positions, boardSize)
            .bottom(Layouts.TURTLE.positions)).toDp().coerceAtLeast(0.dp)
    }
    val board = game.board
    val pairs = board.playablePairCount()

    Box(
        Modifier
            .fillMaxSize()
            // Le dégradé fait des anneaux sur les écrans e-ink : option « Fond uni » (voir 0016).
            .background(
                if (settings.plainTable) SolidColor(Color(0xFF1F5443))
                else Brush.radialGradient(listOf(Color(0xFF2B6E55), Color(0xFF0D2B21)), radius = 2600f),
            ),
    ) {
        Row(Modifier.fillMaxSize().safeDrawingPadding()) {
            // Le plateau est limité par la hauteur : les commandes prennent place à gauche.
            // Compteurs sur une ligne chacun : avec une grande police, la colonne doit tenir
            // en hauteur (voir 0015).
            Column(
                Modifier.fillMaxHeight().width(150.dp)
                    .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = bottomGap),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Accord selon la langue : en français, singulier jusqu'à 1 inclus
                    // (« 0 paire »), en anglais au-delà de 1 seulement (« 0 pairs »).
                    Label(pluralStringResource(R.plurals.tiles_left, board.tiles.size, board.tiles.size))
                    Label(pluralStringResource(R.plurals.playable_pairs, pairs, pairs))
                    Label(stringResource(game.mode.label).replaceFirstChar { it.uppercase() })
                }
                // Tortue vide : rien à perdre, la nouvelle donne se passe de confirmation.
                ActionButton(stringResource(R.string.new_deal)) {
                    if (board.tiles.isEmpty()) deal() else confirmNewDeal = true
                }
                // Coups de la partie en bas, à l'écart de la nouvelle donne qui l'abandonne.
                Spacer(Modifier.weight(1f))
                IconButton(R.drawable.ic_undo, stringResource(R.string.undo), enabled = game.canUndo) { game.undo() }
                IconButton(R.drawable.ic_hint, stringResource(R.string.hint), game.hintProgress, enabled = pairs > 0) {
                    game.nextHint()
                }
            }
            Box(Modifier.fillMaxSize()) {
                BoardCanvas(game, Modifier.fillMaxSize().onSizeChanged { boardSize = it.toSize() })
                // Coin haut droit, toujours vide : la tortue n'y a que ses tuiles d'extrémité,
                // à mi-hauteur. Icône en dp, insensible à la police du système.
                IconButton(R.drawable.ic_options, stringResource(R.string.options), modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                    showOptions = true
                }
            }
        }

        val message = when {
            board.tiles.isEmpty() -> stringResource(R.string.won)
            pairs == 0 -> stringResource(R.string.no_more_pairs)
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
                    if (board.tiles.isNotEmpty()) ActionButton(stringResource(R.string.undo)) { game.undo() }
                    ActionButton(stringResource(R.string.new_deal)) { deal() }
                }
            }
        }

        if (confirmNewDeal) {
            ModalPanel(onDismiss = { confirmNewDeal = false }) {
                BasicText(
                    stringResource(R.string.confirm_new_deal),
                    style = TextStyle(TextColor, 22.sp, FontWeight.Bold, textAlign = TextAlign.Center),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionButton(stringResource(R.string.keep_playing)) { confirmNewDeal = false }
                    ActionButton(stringResource(R.string.new_deal)) {
                        deal()
                        confirmNewDeal = false
                    }
                }
            }
        }

        if (showOptions) OptionsPanel(settings) {
            showOptions = false
            offerNewDeal = settings.mode != game.mode
        }

        if (offerNewDeal) {
            ModalPanel(onDismiss = { offerNewDeal = false }) {
                BasicText(
                    stringResource(R.string.offer_new_deal, stringResource(settings.mode.description)),
                    style = TextStyle(TextColor, 22.sp, FontWeight.Bold, textAlign = TextAlign.Center),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionButton(stringResource(R.string.keep_playing)) { offerNewDeal = false }
                    ActionButton(stringResource(R.string.new_deal)) {
                        deal()
                        offerNewDeal = false
                    }
                }
            }
        }

        if (dealing) {
            ModalPanel(onDismiss = {}) {
                BasicText(stringResource(R.string.dealing),style = TextStyle(TextColor, 22.sp, FontWeight.Bold))
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
                // La tuile visée est la dernière dessinée sous le doigt, donc la plus haute ;
                // toucher le fond abandonne la sélection et éteint l'aide.
                val target = game.board.tiles.keys.sortedWith(drawOrder)
                    .lastOrNull { layout.faceRect(it).contains(down.position) }
                if (target != null) game.tap(target) else game.tapBackground()
            }
        },
    ) {
        val board = game.board
        if (board.tiles.isEmpty()) return@Canvas
        // Cadrage calculé sur la disposition complète : le plateau ne bouge pas en cours de partie.
        val layout = BoardLayout(Layouts.TURTLE.positions, size)
        val hint = game.hintPair
        val sprites = spriteCache[0]?.takeIf { it.metrics == layout.metrics }
            ?: TileSprites(layout.metrics).also { spriteCache[0] = it }
        for (p in board.tiles.keys.sortedWith(drawOrder)) {
            drawTile(
                layout.faceRect(p),
                sprites,
                painters.getValue(board.tiles.getValue(p)),
                selected = p == game.selected,
                hinted = hint != null && (p == hint.first || p == hint.second),
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

/**
 * Bouton à icône, dessinée en dp : insensible à la police du système. [badge], s'il est
 * donné, s'affiche à droite de l'icône (rang de la paire montrée par l'aide).
 */
@Composable
private fun IconButton(
    icon: Int,
    description: String,
    badge: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val alpha = if (enabled) 1f else 0.35f
    Row(
        modifier
            .background(ButtonColor, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(painterResource(icon), description, Modifier.size(28.dp), alpha = alpha)
        if (badge != null) BasicText(badge, style = TextStyle(TextColor.copy(alpha = alpha), 16.sp, FontWeight.Medium))
    }
}
