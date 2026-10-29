package cl.biosur.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Gestor de preferencia de tema (Claro / Oscuro / Sistema).
 */
object ThemeManager {

    private const val PREFS_NAME = "biosur_theme_prefs"
    private const val KEY_THEME = "app_theme_mode"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        val theme = getSavedTheme(context)
        applyTheme(theme)
    }

    fun getSavedTheme(context: Context): String {
        return getPrefs(context).getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setSavedTheme(context: Context, themeMode: String) {
        getPrefs(context).edit().putString(KEY_THEME, themeMode).apply()
        applyTheme(themeMode)
    }

    fun applyTheme(themeMode: String) {
        val mode = when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
