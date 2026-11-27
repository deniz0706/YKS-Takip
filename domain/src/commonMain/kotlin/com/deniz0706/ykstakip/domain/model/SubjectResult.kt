package com.deniz0706.ykstakip.domain.model

import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

/** Protected: per-subject exam result model. */
@Serializable
data class SubjectResult(
    val subject: String,
    val correct: Int,
    val wrong: Int,
    val blank: Int,
    val timeMinutes: Int,
    val weakTopics: List<String> = emptyList()
) {
    val questionCount: Int get() = correct + wrong + blank
    val net: Double get() = correct - wrong * 0.25
    val minutesPerQuestion: Double?
        get() = if (timeMinutes > 0 && questionCount > 0)
            (timeMinutes.toDouble() / questionCount).roundToInt().toDouble() / 100.0 * 100.0 else null
}
