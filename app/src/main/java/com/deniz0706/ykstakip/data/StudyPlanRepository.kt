package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.model.StudyPlanItem
import org.json.JSONArray
import org.json.JSONObject

class StudyPlanRepository(context: Context) {

    private val prefs =
        context.getSharedPreferences("yks_takip_study_plan", Context.MODE_PRIVATE)

    fun getAll(): List<StudyPlanItem> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()

        return try {
            val array = JSONArray(raw)

            (0 until array.length()).map { index ->
                val obj = array.getJSONObject(index)

                StudyPlanItem(
                    id = obj.getLong("id"),
                    dayOfWeek = obj.getInt("dayOfWeek"),
                    startTime = obj.getString("startTime"),
                    endTime = obj.getString("endTime"),
                    subject = obj.getString("subject"),
                    topic = obj.getString("topic"),
                    completed = obj.optBoolean("completed", false)
                )
            }.sortedWith(
                compareBy<StudyPlanItem> { it.dayOfWeek }
                    .thenBy { it.startTime }
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getForDay(dayOfWeek: Int): List<StudyPlanItem> {
        return getAll()
            .filter { it.dayOfWeek == dayOfWeek }
            .sortedBy { it.startTime }
    }

    fun save(item: StudyPlanItem) {
        val current = getAll().toMutableList()

        val index = current.indexOfFirst { it.id == item.id }

        if (index >= 0) {
            current[index] = item
        } else {
            current.add(item)
        }

        persist(current)
    }

    fun delete(id: Long) {
        persist(getAll().filterNot { it.id == id })
    }

    fun toggleCompleted(id: Long) {
        val item = getAll().find { it.id == id } ?: return

        save(
            item.copy(
                completed = !item.completed
            )
        )
    }

    private fun persist(items: List<StudyPlanItem>) {
        val array = JSONArray()

        items.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("dayOfWeek", item.dayOfWeek)
                    put("startTime", item.startTime)
                    put("endTime", item.endTime)
                    put("subject", item.subject)
                    put("topic", item.topic)
                    put("completed", item.completed)
                }
            )
        }

        prefs.edit()
            .putString(KEY_ITEMS, array.toString())
            .apply()
    }

    companion object {
        private const val KEY_ITEMS = "study_plan_items"
    }
}
