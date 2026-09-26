package com.deniz0706.ykstakip.model

import org.json.JSONObject
import kotlin.math.roundToInt

data class SubjectResult(
    val subject: String,
    val questionCount: Int,
    val correct: Int,
    val wrong: Int,
    val timeMinutes: Int
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
    }

    companion object {
        fun fromJson(json: JSONObject): SubjectResult = SubjectResult(
            subject = json.getString("subject"),
            questionCount = json.getInt("questionCount"),
            correct = json.getInt("correct"),
            wrong = json.getInt("wrong"),
            timeMinutes = json.optInt("timeMinutes", 0)
        )
        private fun round2(value: Double): Double = (value * 100).roundToInt() / 100.0
    }
}
