package com.deniz0706.ykstakip.data

import android.content.Context
import org.json.JSONObject

/** Persists and restores focus session timer state so it survives backup/restore. */
object FocusSessionBackupManager {
    const val SCHEMA = "yks_takip_focus_session_backup"

    data class Parsed(
        val durationMinutes: Int?,
        val accumulatedMs: Long?,
        val running: Boolean?,
        val stopwatchMode: Boolean?,
        val startedAt: Long?,
        val carryDate: String?,
        val carryMs: Long?
    )

    /** Exports focus session state from SharedPreferences to JSON. */
    fun exportJson(context: Context): String {
        val prefs = context.getSharedPreferences("yks_focus_session", Context.MODE_PRIVATE)
        return JSONObject().apply {
            put("schema", SCHEMA)
            put("version", 1)
            put("duration", prefs.getInt("duration", 25))
            put("accumulated", prefs.getLong("accumulated", 0L))
            put("started_at", prefs.getLong("started_at", -1L))
            put("stopwatch_mode", prefs.getBoolean("stopwatch_mode", false))
            put("carry_date", prefs.getString("carry_date", null))
            put("carry_ms", prefs.getLong("carry_ms", -1L))
        }.toString(2)
    }

    /** Imports focus session state from JSON into SharedPreferences. */
    fun importJson(context: Context, raw: String): Parsed {
        val obj = JSONObject(raw)
        require(obj.optString("schema") == SCHEMA) { "Bu odak oturumu yedeği değil." }
        require(obj.optInt("version") == 1) { "Bilinmeyen odak oturumu yedek sürümü." }

        val prefs = context.getSharedPreferences("yks_focus_session", Context.MODE_PRIVATE)
        val durationRaw = obj.optInt("duration", -1)
        val duration = if (durationRaw > 0) durationRaw else null
        val accumulated = obj.optLong("accumulated", -1L).takeIf { it >= 0 }
        val startedAtRaw = obj.optLong("started_at", -1L)
        val startedAt = if (startedAtRaw < 0) null else startedAtRaw
        val stopwatchMode = obj.optBoolean("stopwatch_mode", false)
        val carryDateRaw = obj.optString("carry_date", "")
        val carryDate = carryDateRaw.takeIf { it.isNotEmpty() }
        val carryMsRaw = obj.optLong("carry_ms", -1L)
        val carryMs = if (carryMsRaw < 0) null else carryMsRaw

        prefs.edit().apply {
            duration?.let { putInt("duration", it) }
            accumulated?.let { putLong("accumulated", it) }
            startedAt?.let { putLong("started_at", it) }
            putBoolean("stopwatch_mode", stopwatchMode)
            carryDate?.let { putString("carry_date", it) }
            carryMs?.let { putLong("carry_ms", it) }
        }.apply()

        return Parsed(duration, accumulated, if (startedAtRaw > 0) true else null,
            stopwatchMode, startedAt, carryDate, carryMs)
    }
}
