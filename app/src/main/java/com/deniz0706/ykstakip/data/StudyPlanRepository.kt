package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.model.StudyPlanItem
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** A repeating weekly timetable. Checkmarks expire at the start of each new week. */
class StudyPlanRepository(context: Context) {

    private val prefs =
        context.getSharedPreferences("yks_takip_study_plan", Context.MODE_PRIVATE)

    private fun currentWeek(): String = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()

    fun getAll(): List<StudyPlanItem> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val week = currentWeek()
            var legacy = false
            val items = (0 until array.length()).map { index ->
                val obj = array.getJSONObject(index)
                val storedCompleted = obj.optBoolean("completed", false)
                if (!obj.has("completedWeek")) legacy = true
                // Existing records had a permanent completed flag; interpret it
                // as completed in this week's migrated schedule only once.
                val completedWeek = obj.optString("completedWeek", "")
                    .takeIf { it.isNotBlank() }
                    ?: if (storedCompleted && !obj.has("completedWeek")) week else null
                StudyPlanItem(
                    id = obj.getLong("id"),
                    dayOfWeek = obj.getInt("dayOfWeek"),
                    startTime = obj.getString("startTime"),
                    endTime = obj.getString("endTime"),
                    subject = obj.getString("subject"),
                    topic = obj.optString("topic", ""),
                    completed = storedCompleted && completedWeek == week,
                    completedWeek = completedWeek
                )
            }.sortedWith(
                compareBy<StudyPlanItem> { it.dayOfWeek }
                    .thenBy { it.startTime }
            )
            if (legacy) persist(items)
            items
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getForDay(dayOfWeek: Int): List<StudyPlanItem> =
        getAll().filter { it.dayOfWeek == dayOfWeek }.sortedBy { it.startTime }

    fun save(item: StudyPlanItem) {
        val current = getAll().toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) current[index] = item else current.add(item)
        persist(current)
    }

    fun delete(id: Long) {
        persist(getAll().filterNot { it.id == id })
    }

    fun toggleCompleted(id: Long) {
        val item = getAll().find { it.id == id } ?: return
        save(item.copy(
            completed = !item.completed,
            completedWeek = if (item.completed) null else currentWeek()
        ))
    }

    private fun persist(items: List<StudyPlanItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().apply {
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
        prefs.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    companion object {
        private const val KEY_ITEMS = "study_plan_items"
    }
}
