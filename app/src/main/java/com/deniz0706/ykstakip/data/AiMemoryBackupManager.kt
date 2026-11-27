package com.deniz0706.ykstakip.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Persists and restores consented AI coach memory so it survives backup/restore.
 * Only explicitly user-consented memories are exported; never auto-captured data. */
object AiMemoryBackupManager {
    const val SCHEMA = "yks_takip_ai_memory_backup"

    data class Parsed(
        val memories: List<MemoryData>
    ) {
        data class MemoryData(
            val id: String,
            val category: String,
            val content: String,
            val createdAt: Long,
            val updatedAt: Long,
            val source: String
        )
    }

    /** Exports consented AI memories from SharedPreferences to JSON. */
    fun exportJson(context: Context): String {
        val prefs = context.getSharedPreferences("ai_coach_long_term_memory", Context.MODE_PRIVATE)
        val raw = prefs.getString("memories_v1", null)
        val array = raw?.let { try { JSONArray(it) } catch (_: Exception) { JSONArray() } } ?: JSONArray()
        val exported = JSONArray()
        (0 until array.length()).forEach { index ->
            val item = array.optJSONObject(index) ?: return@forEach
            // Only export explicitly consented memories (source != auto_capture / explicit_self_description)
            val source = item.optString("source", "legacy")
            if (source == "auto_capture" || source == "explicit_self_description") return@forEach
            exported.put(JSONObject().apply {
                put("id", item.optString("id", ""))
                put("category", item.optString("category", "Not"))
                put("content", item.optString("content", ""))
                put("createdAt", item.optLong("createdAt", 0L))
                put("updatedAt", item.optLong("updatedAt", 0L))
                put("source", source)
            })
        }
        return JSONObject().apply {
            put("schema", SCHEMA)
            put("version", 1)
            put("memories", exported)
        }.toString(2)
    }

    /** Imports consented AI memories from JSON into SharedPreferences, merging with existing. */
    fun importJson(context: Context, raw: String): Parsed {
        val obj = JSONObject(raw)
        require(obj.optString("schema") == SCHEMA) { "Bu AI hafıza yedeği değil." }
        require(obj.optInt("version") == 1) { "Bilinmeyen AI hafıza yedek sürümü." }

        val prefs = context.getSharedPreferences("ai_coach_long_term_memory", Context.MODE_PRIVATE)
        val existingRaw = prefs.getString("memories_v1", "[]")
        val existing = try {
            JSONArray(existingRaw)
        } catch (_: Exception) { JSONArray() }
        val existingIds = (0 until existing.length()).mapNotNull { index ->
            existing.optJSONObject(index)?.optString("id", null)
        }.toSet()

        val incoming = obj.getJSONArray("memories")
        val added = mutableListOf<Parsed.MemoryData>()
        for (i in 0 until incoming.length()) {
            val item = incoming.optJSONObject(i) ?: continue
            val id = item.optString("id", "")
            if (id.isEmpty() || id in existingIds) continue
            added.add(Parsed.MemoryData(
                id = id,
                category = item.optString("category", "Not"),
                content = item.optString("content", ""),
                createdAt = item.optLong("createdAt", 0L),
                updatedAt = item.optLong("updatedAt", 0L),
                source = item.optString("source", "restored").takeIf { it.isNotEmpty() } ?: "restored"
            ))
        }

        if (added.isNotEmpty()) {
            val merged = JSONArray()
            for (j in 0 until existing.length()) {
                merged.put(existing.getJSONObject(j))
            }
            added.forEach { m ->
                merged.put(JSONObject().apply {
                    put("id", m.id)
                    put("category", m.category)
                    put("content", m.content)
                    put("createdAt", m.createdAt)
                    put("updatedAt", m.updatedAt)
                    put("source", m.source)
                })
            }
            prefs.edit().putString("memories_v1", merged.toString()).apply()
        }

        return Parsed(added)
    }
}
