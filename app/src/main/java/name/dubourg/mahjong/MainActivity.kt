package name.dubourg.mahjong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Agrandissement maximal de la police du système : au-delà, la colonne de commandes ne
 * tient plus dans la hauteur d'un téléphone en paysage (voir 0015).
 */
private const val MAX_FONT_SCALE = 1.3f

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tapis sombre : icônes claires dans les barres système.
        val bars = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        // Plein écran : le plateau est limité par la hauteur. Un glissement depuis le bord
        // fait réapparaître les barres un instant.
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        setContent {
            // Sous le plafond, la densité du système est gardée telle quelle, avec sa
            // conversion non linéaire des grandes polices (Android 14).
            val density = LocalDensity.current
            val capped = if (density.fontScale <= MAX_FONT_SCALE) density else Density(density.density, MAX_FONT_SCALE)
            CompositionLocalProvider(LocalDensity provides capped) { GameScreen() }
        }
    }
}
