package com.deniz0706.ykstakip.model

import org.json.JSONArray
import org.json.JSONObject

/** Android JSON compatibility adapter around the shared exam-result value model. */
typealias SubjectResult = com.deniz0706.ykstakip.domain.SubjectResult

fun SubjectResult.toJson(): JSONObject = JSONObject().apply {
    put("subject", subject)
    put("questionCount", questionCount)
    put("correct", correct)
    put("wrong", wrong)
    put("timeMinutes", timeMinutes)
    put("weakTopics", JSONArray(weakTopics))
}

fun subjectResultFromJson(json: JSONObject): SubjectResult {
    val topics = mutableListOf<String>()
    json.optJSONArray("weakTopics")?.let { arr ->
        for (i in 0 until arr.length()) topics.add(arr.getString(i))
    }
    return SubjectResult(
        subject = json.getString("subject"),
        questionCount = json.getInt("questionCount"),
        correct = json.getInt("correct"),
        wrong = json.getInt("wrong"),
        timeMinutes = json.optInt("timeMinutes", 0),
        weakTopics = topics
    )
}
