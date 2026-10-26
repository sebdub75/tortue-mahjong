package name.dubourg.mahjong

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Mode de distribution d'une partie. [label], court, tient dans la colonne de commandes ;
 * [description] précise le mode quand la place le permet. Ce sont des ressources texte,
 * traduites selon la langue du téléphone (voir 0017).
 */
enum class Mode(@StringRes val label: Int, @StringRes val description: Int = label) {
    /** Donne construite soluble par `Dealer.deal`, très tolérante. */
    NORMAL(R.string.mode_normal),

    /** Donne au hasard dont le solveur a trouvé une solution (`Dealer.dealSolvable`). */
    HARD(R.string.mode_hard),

    /** Donne au hasard sans vérification (`Dealer.dealRandom`) : peut être perdue d'avance. */
    HARD_UNCHECKED(R.string.mode_hard, R.string.mode_hard_unchecked),

    /** Donne soluble et très piégeuse, choisie à l'avance (`Dealer.dealExpert`). */
    EXPERT(R.string.mode_expert),
}

/** Options du joueur, conservées d'un lancement à l'autre. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private var hardState by mutableStateOf(prefs.getBoolean(KEY_HARD, false))
    private var guaranteedState by mutableStateOf(prefs.getBoolean(KEY_GUARANTEED, true))
    private var expertState by mutableStateOf(prefs.getBoolean(KEY_EXPERT, false))
    private var plainTableState by mutableStateOf(prefs.getBoolean(KEY_PLAIN_TABLE, false))

    /** Mode difficile : donne tirée au hasard. */
    var hard: Boolean
        get() = hardState
        set(value) {
            hardState = value
            prefs.edit().putBoolean(KEY_HARD, value).apply()
        }

    /** En mode difficile, ne garder que les donnes dont le solveur a trouvé une solution. */
    var guaranteed: Boolean
        get() = guaranteedState
        set(value) {
            guaranteedState = value
            prefs.edit().putBoolean(KEY_GUARANTEED, value).apply()
        }

    /** Avec la solution garantie, ne servir que des donnes très piégeuses (mode expert). */
    var expert: Boolean
        get() = expertState
        set(value) {
            expertState = value
            prefs.edit().putBoolean(KEY_EXPERT, value).apply()
        }

    /** Tapis d'une couleur unie au lieu du dégradé, qui fait des anneaux sur un écran e-ink. */
    var plainTable: Boolean
        get() = plainTableState
        set(value) {
            plainTableState = value
            prefs.edit().putBoolean(KEY_PLAIN_TABLE, value).apply()
        }

    /** Mode des prochaines donnes. */
    val mode: Mode
        get() = when {
            !hard -> Mode.NORMAL
            !guaranteed -> Mode.HARD_UNCHECKED
            expert -> Mode.EXPERT
            else -> Mode.HARD
        }

    private companion object {
        const val KEY_HARD = "hard"
        const val KEY_GUARANTEED = "guaranteed"
        const val KEY_EXPERT = "expert"
        const val KEY_PLAIN_TABLE = "plainTable"
    }
}
