package com.deniz0706.ykstakip.domain

data class StudyDay(
    val date: String,
    val questionCount: Int,
    val studyMinutes: Int,
    val estimated: Boolean = false
)
