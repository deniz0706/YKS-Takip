package com.deniz0706.ykstakip.domain.model

import kotlinx.serialization.Serializable

/** Protected: per-subject exam result model. */
@Serializable
data class SubjectResult(
    val subject: String,
    val correct: Int,
    val wrong: Int,
    val blank: Int,
    val timeMinutes: Int
) {
    val questionCount: Int get() = correct + wrong + blank
    val net: Double get() = correct - wrong * 0.25
}
