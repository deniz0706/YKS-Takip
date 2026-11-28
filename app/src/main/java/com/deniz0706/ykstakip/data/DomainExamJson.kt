package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.SubjectResult
import org.json.JSONArray
import org.json.JSONObject

/** JSON serialization for domain Exam, preserving the legacy on-disk format exactly. */
fun Exam.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("date", date)
    put("type", type.name)
    put("aytField", aytField?.name)
    put("notes", notes)
    put("publisher", publisher)
    put("subjects", JSONArray(subjects.map { it.toJson() }))
}

fun Exam.Companion.fromJson(json: JSONObject): Exam {
    val arr = json.getJSONArray("subjects")
    val subjects = (0 until arr.length()).map { SubjectResult.Companion.fromJson(arr.getJSONObject(it)) }
    return Exam(
        id = json.getLong("id"),
        title = json.getString("title"),
        date = json.getString("date"),
        type = ExamType.valueOf(json.getString("type")),
        aytField = json.optString("aytField", "").takeIf { it.isNotEmpty() }?.let { AytField.valueOf(it) },
        subjects = subjects,
        notes = json.optString("notes", ""),
        publisher = json.optString("publisher", "")
    )
}

/** JSON serialization for domain SubjectResult, preserving the legacy on-disk format exactly. */
fun SubjectResult.toJson(): JSONObject = JSONObject().apply {
    put("subject", subject)
    put("questionCount", questionCount)
    put("correct", correct)
    put("wrong", wrong)
    put("timeMinutes", timeMinutes)
    put("weakTopics", JSONArray(weakTopics))
}

fun SubjectResult.Companion.fromJson(json: JSONObject): SubjectResult {
    val topics = mutableListOf<String>()
    json.optJSONArray("weakTopics")?.let { arr ->
        for (i in 0 until arr.length()) topics.add(arr.getString(i))
    }
    return SubjectResult(
        subject = json.getString("subject"),
        correct = json.getInt("correct"),
        wrong = json.getInt("wrong"),
        blank = (json.getInt("questionCount") - json.getInt("correct") - json.getInt("wrong")).coerceAtLeast(0),
        timeMinutes = json.optInt("timeMinutes", 0),
        weakTopics = topics
    )
}
