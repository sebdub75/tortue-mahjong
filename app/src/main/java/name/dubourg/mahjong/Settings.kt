package name.dubourg.mahjong

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Options du joueur, conservées d'un lancement à l'autre. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private var hardState by mutableStateOf(prefs.getBoolean(KEY_HARD, false))

    /** Mode difficile. Pas encore d'effet sur le jeu. */
    var hard: Boolean
        get() = hardState
        set(value) {
            hardState = value
            prefs.edit().putBoolean(KEY_HARD, value).apply()
        }

    private companion object {
        const val KEY_HARD = "hard"
    }
}
