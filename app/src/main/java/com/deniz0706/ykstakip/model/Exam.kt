package com.deniz0706.ykstakip.model

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt
import com.deniz0706.ykstakip.model.AytField

data class Exam(
    val id: Long,
    val title: String,
    val date: String,
    val type: ExamType,
    val aytField: AytField? = null,
    val subjects: List<SubjectResult>,
    val notes: String = ""
) {
    val totalCorrect: Int get() = subjects.sumOf { it.correct }
    val totalWrong: Int get() = subjects.sumOf { it.wrong }
    val totalBlank: Int get() = subjects.sumOf { it.blank }
    val totalQuestions: Int get() = subjects.sumOf { it.questionCount }
    val totalTimeMinutes: Int get() = subjects.sumOf { it.timeMinutes }
    val totalNet: Double get() = round2(subjects.sumOf { it.net })
    val overallMinutesPerQuestion: Double?
        get() = if (totalQuestions > 0 && totalTimeMinutes > 0)
            round2(totalTimeMinutes.toDouble() / totalQuestions) else null

    fun subject(name: String): SubjectResult? = subjects.find { it.subject == name }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("date", date)
        put("type", type.name)
        put("aytField", aytField?.name)
        put("subjects", JSONArray(subjects.map { it.toJson() }))
    }

    companion object {
        fun fromJson(json: JSONObject): Exam {
            val arr = json.getJSONArray("subjects")
            val subjects = (0 until arr.length()).map { SubjectResult.fromJson(arr.getJSONObject(it)) }
            return Exam(
                id = json.getLong("id"),
                title = json.getString("title"),
                date = json.getString("date"),
                type = ExamType.valueOf(json.getString("type")),
                aytField = json.optString("aytField", "")
    .takeIf { it.isNotEmpty() }
    ?.let { AytField.valueOf(it) },
                subjects = subjects
            )
        }
        private fun round2(value: Double): Double = (value * 100).roundToInt() / 100.0
    }
}
