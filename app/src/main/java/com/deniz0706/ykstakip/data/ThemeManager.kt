package com.deniz0706.ykstakip.data

import android.content.Context
import android.content.res.Configuration
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateOf

/**
 * Four selectable appearances. Legacy "dark" meant the navy/mint palette;
 * migrate it ONCE to Aurora before "dark" is reused for the graphite palette.
 */
object ThemeManager {
    private const val PREFS = "yks_takip_prefs"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_PALETTE_V2 = "theme_palette_v2"
    private val modes = setOf("light", "dark", "aurora", "system")

    // Read by Compose themes: switching between Aurora and Dark (both uiMode night)
    // must repaint all retained pages without recreating the Activity.
    val selectedMode = mutableStateOf("system")

    private fun read(prefs: SharedPreferences): String {
        if (!prefs.getBoolean(KEY_PALETTE_V2, false)) {
            val prior = prefs.getString(KEY_THEME, "system") ?: "system"
            val migrated = if (prior == "dark") "aurora" else prior.takeIf { it in modes } ?: "system"
            prefs.edit().putString(KEY_THEME, migrated).putBoolean(KEY_PALETTE_V2, true).apply()
            return migrated
        }
        return prefs.getString(KEY_THEME, "system")?.takeIf { it in modes } ?: "system"
    }

    fun currentTheme(context: Context): String =
        read(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE))

    private fun desiredNightMode(mode: String): Int = when (mode) {
        "aurora", "dark" -> AppCompatDelegate.MODE_NIGHT_YES
        "light" -> AppCompatDelegate.MODE_NIGHT_NO
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    fun applySavedTheme(context: Context) {
        val mode = currentTheme(context)
        selectedMode.value = mode
        val desired = desiredNightMode(mode)
        if (AppCompatDelegate.getDefaultNightMode() != desired) {
            AppCompatDelegate.setDefaultNightMode(desired)
        }
    }

    fun setTheme(context: Context, mode: String) {
        require(mode in modes) { "Unknown theme: $mode" }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_THEME, mode)
            .putBoolean(KEY_PALETTE_V2, true)
            .apply()
        selectedMode.value = mode
        val desired = desiredNightMode(mode)
        if (AppCompatDelegate.getDefaultNightMode() != desired) {
            AppCompatDelegate.setDefaultNightMode(desired)
        }
    }

    data class NativePalette(
        val background: Int,
        val surface: Int,
        val elevated: Int,
        val onSurface: Int,
        val muted: Int,
        val accent: Int,
        val border: Int,
        val selected: Int
    )

    private fun palette(
        bg: String, surface: String, elevated: String, on: String,
        muted: String, accent: String, border: String, selected: String
    ) = NativePalette(
        Color.parseColor(bg), Color.parseColor(surface), Color.parseColor(elevated),
        Color.parseColor(on), Color.parseColor(muted), Color.parseColor(accent),
        Color.parseColor(border), Color.parseColor(selected)
    )

    private val lightPalette = palette(
        "#F5F7FC", "#FFFFFF", "#E9EDF5", "#152238",
        "#53657D", "#7055A5", "#CAD3DF", "#EEE8FA"
    )
    private val auroraPalette = palette(
        "#0B1220", "#172337", "#22334A", "#F3F8FF",
        "#9AAFC5", "#B9A4F5", "#30445C", "#2D344F"
    )
    private val darkPalette = palette(
        "#090B10", "#15181F", "#242831", "#F5F6FA",
        "#A8ADBC", "#CBB7FF", "#343744", "#332B45"
    )

    fun nativePalette(context: Context): NativePalette = when (currentTheme(context)) {
        "light" -> lightPalette
        "aurora" -> auroraPalette
        "dark" -> darkPalette
        else -> if ((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES) darkPalette else lightPalette
    }
}
