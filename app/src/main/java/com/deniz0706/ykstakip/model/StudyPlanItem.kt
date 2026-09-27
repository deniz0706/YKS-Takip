package com.deniz0706.ykstakip.model

data class StudyPlanItem(
    val id: Long,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val topic: String,
    val completed: Boolean
)
