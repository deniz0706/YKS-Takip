package com.deniz0706.ykstakip.domain.model

import kotlinx.serialization.Serializable

/** Protected: study history day record. */
@Serializable
data class StudyDay(
    val date: String,
    val questionCount: Int,
    val studyMinutes: Int,
    val estimated: Boolean = false
)
