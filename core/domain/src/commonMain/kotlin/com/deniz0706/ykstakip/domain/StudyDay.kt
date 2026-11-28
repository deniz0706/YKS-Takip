package com.deniz0706.ykstakip.domain

import kotlinx.serialization.Serializable

@Serializable
data class StudyDay(
    val date: String,
    val questionCount: Int,
    val studyMinutes: Int,
    val estimated: Boolean = false
)
