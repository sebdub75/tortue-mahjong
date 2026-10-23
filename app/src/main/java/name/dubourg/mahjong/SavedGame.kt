package name.dubourg.mahjong

import android.content.Context
import name.dubourg.mahjong.core.Game

/**
 * Partie en cours, enregistrée à chaque coup pour survivre à la recréation de l'activité
 * (écran éteint, rotation) comme à l'arrêt de l'appli.
 */
class SavedGame(context: Context) {
    private val prefs = context.getSharedPreferences("game", Context.MODE_PRIVATE)

    /** Partie enregistrée et son mode ; null s'il n'y en a pas ou si elle est illisible. */
    fun load(): Pair<Game, Boolean>? {
        val text = prefs.getString(KEY_GAME, null) ?: return null
        val game = try {
            Game.decode(text)
        } catch (e: IllegalArgumentException) {
            return null
        }
        return game to prefs.getBoolean(KEY_HARD, false)
    }

    fun save(game: Game, hard: Boolean) {
        prefs.edit().putString(KEY_GAME, game.encode()).putBoolean(KEY_HARD, hard).apply()
    }

    private companion object {
        const val KEY_GAME = "game"
        const val KEY_HARD = "hard"
    }
}
