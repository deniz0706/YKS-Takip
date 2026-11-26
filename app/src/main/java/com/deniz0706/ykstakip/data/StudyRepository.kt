package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.model.StudyDay
import org.json.JSONArray
import org.json.JSONObject

class StudyRepository(context: Context) {

    private val prefs =
        context.getSharedPreferences("yks_takip_study", Context.MODE_PRIVATE)

    fun getAll(): List<StudyDay> {
        val raw = prefs.getString(KEY_STUDY_DAYS, null) ?: return emptyList()

        return try {
            val array = JSONArray(raw)

            (0 until array.length()).map {
                val obj = array.getJSONObject(it)

                StudyDay(
                    date = obj.getString("date"),
                    questionCount = obj.getInt("questionCount"),
                    studyMinutes = obj.getInt("studyMinutes"),
                    estimated = obj.optBoolean("estimated", false)
                )
            }.sortedBy { it.date }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun get(date: String): StudyDay? {
        return getAll().find { it.date == date }
    }

    fun save(studyDay: StudyDay) {
        val current = getAll().toMutableList()
        val index = current.indexOfFirst { it.date == studyDay.date }

        if (index >= 0) {
            current[index] = studyDay
        } else {
            current.add(studyDay)
        }

        persist(current)
    }

    fun delete(date: String) {
        persist(getAll().filterNot { it.date == date })
    }

    private fun persist(days: List<StudyDay>) {
        val array = JSONArray()

        days.sortedBy { it.date }.forEach { day ->
            array.put(
                JSONObject().apply {
                    put("date", day.date)
                    put("questionCount", day.questionCount)
                    put("studyMinutes", day.studyMinutes)
                    put("estimated", day.estimated)
                }
            )
        }

        prefs.edit()
            .putString(KEY_STUDY_DAYS, array.toString())
            // A manual edit invalidates the import rollback snapshot.
            .remove(KEY_BEFORE_IMPORT)
            .remove(KEY_AFTER_IMPORT)
            .apply()
    }

    /** Atomic merge. Existing days stay unchanged unless user explicitly allows overwrites. */
    fun importDays(incoming: List<StudyDay>, overwrite: Boolean = false): Int {
        val current = getAll()
        val merged = current.associateBy { it.date }.toMutableMap()
        var changes = 0
        for (day in incoming) {
            if (merged[day.date] == null || (overwrite && merged[day.date] != day)) {
                merged[day.date] = day
                changes++
            }
        }
        if (changes == 0) return 0
        val raw = prefs.getString(KEY_STUDY_DAYS, "[]") ?: "[]"
        val json = JSONArray()
        merged.values.sortedBy { it.date }.forEach { day ->
            json.put(JSONObject().apply {
                put("date", day.date)
                put("questionCount", day.questionCount)
                put("studyMinutes", day.studyMinutes)
                put("estimated", day.estimated)
            })
        }
        val updated = json.toString()
        require(prefs.edit().putString(KEY_BEFORE_IMPORT, raw)
            .putString(KEY_AFTER_IMPORT, updated)
            .putString(KEY_STUDY_DAYS, updated).commit()) {
            "Çalışma geçmişi kaydedilemedi."
        }
        return changes
    }

    fun canUndoLastImport(): Boolean = StudyBackupManager.canUndoImport(
        prefs.getString(KEY_BEFORE_IMPORT, null),
        prefs.getString(KEY_AFTER_IMPORT, null),
        prefs.getString(KEY_STUDY_DAYS, null)
    )

    fun undoLastImport(): Boolean {
        // Older backups lack an after-import snapshot: do not restore blindly.
        if (!canUndoLastImport()) return false
        val previous = prefs.getString(KEY_BEFORE_IMPORT, null) ?: return false
        return prefs.edit().putString(KEY_STUDY_DAYS, previous)
            .remove(KEY_BEFORE_IMPORT)
            .remove(KEY_AFTER_IMPORT).commit()
    }

    companion object {
        private const val KEY_STUDY_DAYS = "study_days"
        private const val KEY_BEFORE_IMPORT = "study_before_import"
        private const val KEY_AFTER_IMPORT = "study_after_import"
    }
}
