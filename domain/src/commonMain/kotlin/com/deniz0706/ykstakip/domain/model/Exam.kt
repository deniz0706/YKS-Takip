package com.deniz0706.ykstakip.domain.model

import kotlinx.serialization.Serializable

/** Protected: exam data contract shared across Android and any future platform. */
@Serializable
data class Exam(
    val id: Long,
    val title: String,
    val date: String,
    val type: ExamType,
    val aytField: AytField? = null,
    val subjects: List<SubjectResult> = emptyList(),
    val notes: String = "",
    val publisher: String = ""
) {
    val totalCorrect: Int get() = subjects.sumOf { it.correct }
    val totalWrong: Int get() = subjects.sumOf { it.wrong }
    val totalBlank: Int get() = subjects.sumOf { it.blank }
    val totalQuestions: Int get() = subjects.sumOf { it.questionCount }
    val totalTimeMinutes: Int get() = subjects.sumOf { it.timeMinutes }
    val totalNet: Double get() = subjects.sumOf { it.net }.roundToInt().toDouble() / 100.0 * 100.0
    val overallMinutesPerQuestion: Double?
        get() = if (totalQuestions > 0 && totalTimeMinutes > 0)
            (totalTimeMinutes.toDouble() / totalQuestions).roundToInt().toDouble() / 100.0 * 100.0 else null

    fun subject(name: String): SubjectResult? = subjects.find { it.subject == name }
}
