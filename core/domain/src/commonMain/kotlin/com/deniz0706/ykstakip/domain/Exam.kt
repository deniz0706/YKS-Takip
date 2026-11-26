package com.deniz0706.ykstakip.domain

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/** Shared, serializable YKS exam aggregate used by Android and iOS domain layers. */
@Serializable
data class Exam(
    val id: Long,
    val title: String,
    val date: String,
    val type: ExamType,
    val aytField: AytField? = null,
    val subjects: List<SubjectResult>,
    val notes: String = "",
    val publisher: String = ""
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

    companion object {}

    private fun round2(value: Double): Double = (value * 100).roundToInt() / 100.0
}
