package com.deniz0706.ykstakip.model

import org.json.JSONArray
import org.json.JSONObject

/** Progress is tracked per exam type, course, topic and individual outcome. */
data class LearningOutcome(
    val id: String,
    val examType: String,
    val subject: String,
    val topic: String,
    val title: String,
    val completed: Boolean = false,
    val reviewRequested: Boolean = false,
    val wrongCount: Int = 0,
    val acknowledgedWrongCount: Int = 0,
    val nextReviewDate: String = "",
    val reviewLevel: Int = 0,
    val lastReviewedDate: String = "",
    val custom: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("examType", examType); put("subject", subject)
        put("topic", topic); put("title", title); put("completed", completed)
        put("reviewRequested", reviewRequested); put("wrongCount", wrongCount)
        put("acknowledgedWrongCount", acknowledgedWrongCount)
        put("nextReviewDate", nextReviewDate); put("reviewLevel", reviewLevel)
        put("lastReviewedDate", lastReviewedDate); put("custom", custom)
    }
    companion object {
        fun fromJson(j: JSONObject): LearningOutcome = LearningOutcome(
            id = j.getString("id"), examType = j.optString("examType", "TYT"),
            subject = j.optString("subject", ""), topic = j.optString("topic", ""),
            title = j.optString("title", ""), completed = j.optBoolean("completed"),
            reviewRequested = j.optBoolean("reviewRequested"),
            wrongCount = j.optInt("wrongCount"), acknowledgedWrongCount = j.optInt("acknowledgedWrongCount"),
            nextReviewDate = j.optString("nextReviewDate", ""),
            reviewLevel = j.optInt("reviewLevel"),
            lastReviewedDate = j.optString("lastReviewedDate", ""),
            custom = j.optBoolean("custom")
        )
    }
}

enum class QuestionStatus { COZULEMEDI, GERI_DON, COZULDU }

data class QuestionCard(
    val id: String,
    val outcomeId: String,
    val title: String,
    val notes: String,
    val status: QuestionStatus,
    val createdAt: String,
    val dueDate: String,
    val reviewCount: Int = 0,
    val photos: List<String> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("outcomeId", outcomeId); put("title", title)
        put("notes", notes); put("status", status.name); put("createdAt", createdAt)
        put("dueDate", dueDate); put("reviewCount", reviewCount)
        put("photos", JSONArray(photos))
    }
    companion object {
        fun fromJson(j: JSONObject): QuestionCard {
            val p = j.optJSONArray("photos") ?: JSONArray()
            return QuestionCard(
                j.getString("id"), j.optString("outcomeId", ""),
                j.optString("title", ""), j.optString("notes", ""),
                runCatching { QuestionStatus.valueOf(j.optString("status")) }
                    .getOrDefault(QuestionStatus.GERI_DON),
                j.optString("createdAt", ""), j.optString("dueDate", ""),
                j.optInt("reviewCount"),
                (0 until p.length()).map { p.getString(it) }
            )
        }
    }
}
