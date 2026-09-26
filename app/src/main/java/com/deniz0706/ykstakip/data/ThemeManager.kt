package com.deniz0706.ykstakip.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREFS = "yks_takip_prefs"
    private const val KEY_THEME = "theme_mode" // "dark" | "light" | "system"

    fun applySavedTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        when (prefs.getString(KEY_THEME, "system")) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun setTheme(context: Context, mode: String) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putString(KEY_THEME, mode).apply()

    val activity = context as? android.app.Activity
    activity?.overridePendingTransition(
        android.R.anim.fade_in,
        android.R.anim.fade_out
    )

    applySavedTheme(context)
}

    fun currentTheme(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_THEME, "system") ?: "system"
}
