package com.deniz0706.ykstakip.data

import android.content.Context
import org.json.JSONObject

/** Persists and restores appearance/theme settings so they survive backup/restore. */
object ThemeBackupManager {
    const val SCHEMA = "yks_takip_theme_backup"

    data class Parsed(
        val themeMode: String?,
        val paletteV2: Boolean?
    )

    /** Exports theme settings from SharedPreferences to JSON. */
    fun exportJson(context: Context): String {
        val prefs = context.getSharedPreferences("yks_takip_prefs", Context.MODE_PRIVATE)
        return JSONObject().apply {
            put("schema", SCHEMA)
            put("version", 1)
            put("theme_mode", prefs.getString("theme_mode", null))
            put("theme_palette_v2", prefs.getBoolean("theme_palette_v2", false))
        }.toString(2)
    }

    /** Imports theme settings from JSON into SharedPreferences. */
    fun importJson(context: Context, raw: String): Parsed {
        val obj = JSONObject(raw)
        require(obj.optString("schema") == SCHEMA) { "Bu tema yedeği değil." }
        require(obj.optInt("version") == 1) { "Bilinmeyen tema yedek sürümü." }

        val prefs = context.getSharedPreferences("yks_takip_prefs", Context.MODE_PRIVATE)
        val themeModeRaw = obj.optString("theme_mode", "")
        val themeMode = themeModeRaw.takeIf { it.isNotEmpty() }
        val paletteV2 = obj.optBoolean("theme_palette_v2", false)

        prefs.edit().apply {
            themeMode?.let { putString("theme_mode", it) }
            putBoolean("theme_palette_v2", paletteV2)
        }.apply()

        return Parsed(themeMode, paletteV2)
    }
}
