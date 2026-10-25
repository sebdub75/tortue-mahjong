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
    fun load(): Pair<Game, Mode>? {
        val text = prefs.getString(KEY_GAME, null) ?: return null
        val game = try {
            Game.decode(text)
        } catch (e: IllegalArgumentException) {
            return null
        }
        return game to mode()
    }

    fun save(game: Game, mode: Mode) {
        prefs.edit().putString(KEY_GAME, game.encode()).putString(KEY_MODE, mode.name).remove(KEY_HARD).apply()
    }

    // Les sauvegardes d'avant la solution garantie n'ont qu'un booléen « hard » : leurs
    // donnes difficiles étaient tirées sans vérification.
    private fun mode(): Mode {
        val name = prefs.getString(KEY_MODE, null)
            ?: return if (prefs.getBoolean(KEY_HARD, false)) Mode.HARD_UNCHECKED else Mode.NORMAL
        return Mode.entries.firstOrNull { it.name == name } ?: Mode.NORMAL
    }

    private companion object {
        const val KEY_GAME = "game"
        const val KEY_MODE = "mode"
        const val KEY_HARD = "hard"
    }
}
