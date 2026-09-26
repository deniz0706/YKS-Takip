package com.deniz0706.ykstakip.model

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

data class SubjectResult(
    val subject: String,
    val questionCount: Int,
    val correct: Int,
    val wrong: Int,
    val timeMinutes: Int,
    val weakTopics: List<String> = emptyList()
) {
    val blank: Int get() = (questionCount - correct - wrong).coerceAtLeast(0)
    val net: Double get() = round2(correct - wrong / 4.0)
    val minutesPerQuestion: Double?
        get() = if (timeMinutes > 0) round2(timeMinutes.toDouble() / questionCount) else null

    fun toJson(): JSONObject = JSONObject().apply {
        put("subject", subject)
        put("questionCount", questionCount)
        put("correct", correct)
        put("wrong", wrong)
        put("timeMinutes", timeMinutes)
        put("weakTopics", JSONArray(weakTopics))
    }

    companion object {
        fun fromJson(json: JSONObject): SubjectResult {
            val topics = mutableListOf<String>()
            json.optJSONArray("weakTopics")?.let { arr ->
                for (i in 0 until arr.length()) topics.add(arr.getString(i))
            }
            return SubjectResult(
                subject = json.getString("subject"),
                questionCount = json.getInt("questionCount"),
                correct = json.getInt("
