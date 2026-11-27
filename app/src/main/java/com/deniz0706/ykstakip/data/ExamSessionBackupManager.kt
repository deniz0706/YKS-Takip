package com.deniz0706.ykstakip.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Persists and restores exam session configuration and splits so they survive backup/restore. */
object ExamSessionBackupManager {
    const val SCHEMA = "yks_takip_exam_session_backup"

    data class Parsed(
        val examType: String?,
        val durationMinutes: Int?,
        val splits: List<SplitData>
    ) {
        data class SplitData(val label: String, val elapsedMs: Long)
    }

    /** Exports exam session state from SharedPreferences to JSON. */
    fun exportJson(context: Context): String {
        val prefs = context.getSharedPreferences("yks_exam_session_v1", Context.MODE_PRIVATE)
        val splitsRaw = prefs.getString("splits", "[]")
        val splits = mutableListOf<ExamSessionBackupManager.Parsed.SplitData>()
        try {
            val array = JSONArray(splitsRaw)
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                splits.add(ExamSessionBackupManager.Parsed.SplitData(
                    item.optString("label", "Ders"), item.optLong("elapsedMs", 0L)
                ))
            }
        } catch (_: Exception) { }

        return JSONObject().apply {
            put("schema", SCHEMA)
            put("version", 1)
            put("exam_type", prefs.getString("exam_type", null))
            put("duration", prefs.getInt("duration", -1))
            put("shared_timer_migrated", prefs.getBoolean("shared_timer_migrated", false))
            val array = JSONArray()
            splits.forEach { s ->
                array.put(JSONObject().put("label", s.label).put("elapsedMs", s.elapsedMs))
            }
            put("splits", array)
        }.toString(2)
    }

    /** Imports exam session state from JSON into SharedPreferences. */
    fun importJson(context: Context, raw: String): Parsed {
        val obj = JSONObject(raw)
        require(obj.optString("schema") == SCHEMA) { "Bu sınav oturumu yedeği değil." }
        require(obj.optInt("version") == 1) { "Bilinmeyen sınav oturumu yedek sürümü." }

        val prefs = context.getSharedPreferences("yks_exam_session_v1", Context.MODE_PRIVATE)
        val examTypeRaw = obj.optString("exam_type", "")
        val examType = examTypeRaw.takeIf { it.isNotEmpty() }
        val durationRaw = obj.optInt("duration", -1)
        val duration = if (durationRaw > 0) durationRaw else null
        val sharedTimerMigrated = obj.optBoolean("shared_timer_migrated", false)

        val splitsArray = obj.optJSONArray("splits") ?: JSONArray()
        val splits = mutableListOf<ExamSessionBackupManager.Parsed.SplitData>()
        for (i in 0 until splitsArray.length()) {
            val item = splitsArray.optJSONObject(i) ?: continue
            splits.add(ExamSessionBackupManager.Parsed.SplitData(
                item.optString("label", "Ders"), item.optLong("elapsedMs", 0L)
            ))
        }

        prefs.edit().apply {
            examType?.let { putString("exam_type", it) }
            duration?.let { putInt("duration", it) }
            putBoolean("shared_timer_migrated", sharedTimerMigrated)
            val array = JSONArray()
            splits.forEach { s ->
                array.put(JSONObject().put("label", s.label).put("elapsedMs", s.elapsedMs))
            }
            putString("splits", array.toString())
        }.apply()

        return Parsed(examType, duration, splits)
    }
}
