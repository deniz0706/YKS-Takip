package com.deniz0706.ykstakip.model

import org.json.JSONArray
import org.json.JSONObject

/** Android JSON adapter. The exam value model itself is platform-independent. */
typealias Exam = com.deniz0706.ykstakip.domain.Exam

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
    val subjects = (0 until arr.length()).map { SubjectResult.fromJson(arr.getJSONObject(it)) }
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
