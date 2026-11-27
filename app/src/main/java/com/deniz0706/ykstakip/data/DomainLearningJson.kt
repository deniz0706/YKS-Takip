package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.model.QuestionStatus
import com.deniz0706.ykstakip.domain.model.StudyDay
import org.json.JSONArray
import org.json.JSONObject

/** JSON serialization for domain LearningOutcome, preserving the legacy on-disk format exactly. */
fun LearningOutcome.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("examType", examType)
    put("subject", subject)
    put("topic", topic)
    put("title", title)
    put("completed", completed)
    put("completedDate", completedDate)
    put("reviewRequested", reviewRequested)
    put("wrongCount", wrongCount)
    put("acknowledgedWrongCount", acknowledgedWrongCount)
    put("nextReviewDate", nextReviewDate)
    put("reviewLevel", reviewLevel)
    put("lastReviewedDate", lastReviewedDate)
    put("custom", custom)
    put("completedSubtopics", JSONArray(completedSubtopics))
}

fun LearningOutcome.Companion.fromJson(json: JSONObject): LearningOutcome = LearningOutcome(
    id = json.getString("id"),
    examType = json.optString("examType", "TYT"),
    subject = json.optString("subject", ""),
    topic = json.optString("topic", ""),
    title = json.optString("title", ""),
    completed = json.optBoolean("completed"),
    reviewRequested = json.optBoolean("reviewRequested"),
    wrongCount = json.optInt("wrongCount"),
    acknowledgedWrongCount = json.optInt("acknowledgedWrongCount"),
    nextReviewDate = json.optString("nextReviewDate", ""),
    reviewLevel = json.optInt("reviewLevel"),
    lastReviewedDate = json.optString("lastReviewedDate", ""),
    custom = json.optBoolean("custom"),
    completedSubtopics = (json.optJSONArray("completedSubtopics") ?: JSONArray()).let { a ->
        (0 until a.length()).map { a.optString(it) }.filter { it.isNotBlank() }.distinct()
    },
    completedDate = json.optString("completedDate", "")
)

/** JSON serialization for domain QuestionCard, preserving the legacy on-disk format exactly. */
fun QuestionCard.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("outcomeId", outcomeId)
    put("title", title)
    put("notes", notes)
    put("status", status.name)
    put("createdAt", createdAt)
    put("dueDate", dueDate)
    put("reviewCount", reviewCount)
    put("photos", JSONArray(photos))
}

fun QuestionCard.Companion.fromJson(json: JSONObject): QuestionCard {
    val p = json.optJSONArray("photos") ?: JSONArray()
    return QuestionCard(
        id = json.getString("id"),
        outcomeId = json.optString("outcomeId", ""),
        title = json.optString("title", ""),
        notes = json.optString("notes", ""),
        status = runCatching { QuestionStatus.valueOf(json.optString("status")) }
            .getOrDefault(QuestionStatus.GERI_DON),
        createdAt = json.optString("createdAt", ""),
        dueDate = json.optString("dueDate", ""),
        reviewCount = json.optInt("reviewCount"),
        photos = (0 until p.length()).map { p.getString(it) }
    )
}

/** JSON serialization for domain StudyDay, preserving the legacy on-disk format exactly. */
fun StudyDay.toJson(): JSONObject = JSONObject().apply {
    put("date", date)
    put("questionCount", questionCount)
    put("studyMinutes", studyMinutes)
    put("estimated", estimated)
}

fun StudyDay.Companion.fromJson(json: JSONObject): StudyDay = StudyDay(
    date = json.getString("date"),
    questionCount = json.getInt("questionCount"),
    studyMinutes = json.getInt("studyMinutes"),
    estimated = json.optBoolean("estimated", false)
)
