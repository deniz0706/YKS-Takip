package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.domain.model.StudyPlanItem
import org.json.JSONArray
import org.json.JSONObject

/** Persists and restores weekly study plan items so they survive backup/restore. */
object StudyPlanBackupManager {
    const val SCHEMA = "yks_takip_study_plan_backup"

    data class Parsed(val items: List<StudyPlanItem>)

    /** Exports all study plan items to JSON. */
    fun exportJson(context: Context): String {
        val repository = StudyPlanRepository(context)
        val items = repository.getAll()
        val rows = JSONArray()
        items.forEach { item ->
            rows.put(JSONObject().apply {
                put("id", item.id)
                put("dayOfWeek", item.dayOfWeek)
                put("startTime", item.startTime)
                put("endTime", item.endTime)
                put("subject", item.subject)
                put("topic", item.topic)
                put("completed", item.completed)
                put("completedWeek", item.completedWeek ?: "")
            })
        }
        return JSONObject().apply {
            put("schema", SCHEMA)
            put("version", 1)
            put("items", rows)
        }.toString(2)
    }

    /** Imports study plan items from JSON, merging with existing items. */
    fun importJson(context: Context, raw: String): Parsed {
        val obj = JSONObject(raw)
        require(obj.optString("schema") == SCHEMA) { "Bu çalışma planı yedeği değil." }
        require(obj.optInt("version") == 1) { "Bilinmeyen çalışma planı yedek sürümü." }

        val repository = StudyPlanRepository(context)
        val array = obj.getJSONArray("items")
        val items = (0 until array.length()).map { index ->
            val entry = array.getJSONObject(index)
            StudyPlanItem(
                id = entry.getLong("id"),
                dayOfWeek = entry.getInt("dayOfWeek"),
                startTime = entry.getString("startTime"),
                endTime = entry.getString("endTime"),
                subject = entry.getString("subject"),
                topic = entry.optString("topic", ""),
                completed = entry.optBoolean("completed", false),
                completedWeek = entry.optString("completedWeek", "").takeIf { it.isNotEmpty() }
            )
        }

        // Merge: existing items are kept; new items from backup are added.
        val existingIds = repository.getAll().map { it.id }.toSet()
        items.filterNot { it.id in existingIds }.forEach { repository.save(it) }

        return Parsed(items)
    }
}
