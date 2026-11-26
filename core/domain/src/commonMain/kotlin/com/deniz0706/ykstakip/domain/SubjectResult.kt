package com.deniz0706.ykstakip.domain

import kotlinx.serialization.Serializable

import kotlin.math.roundToInt

/** Shared, platform-independent result for one YKS subject/test. */
@Serializable
data class SubjectResult(
    val subject: String,
    val questionCount: Int,
    val correct: Int,
    val wrong: Int,
    val timeMinutes: Int,
    val weakTopics: List<String> = emptyList()
) {
    val blank: Int get() = (questionCount - correct - wrong).coerceAtLeast(0)
    val net: Double get() = NetScore.calculate(correct, wrong)
    val minutesPerQuestion: Double?
        get() = if (timeMinutes > 0 && questionCount > 0)
            round2(timeMinutes.toDouble() / questionCount) else null

    companion object {}

    private fun round2(value: Double): Double = (value * 100).roundToInt() / 100.0
}
