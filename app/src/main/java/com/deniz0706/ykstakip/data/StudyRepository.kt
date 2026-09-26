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
                    studyMinutes = obj.getInt("studyMinutes")
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
                }
            )
        }

        prefs.edit()
            .putString(KEY_STUDY_DAYS, array.toString())
            .apply()
    }

    companion object {
        private const val KEY_STUDY_DAYS = "study_days"
    }
}
