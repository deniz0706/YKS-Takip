package com.deniz0706.ykstakip.domain.model

import kotlinx.serialization.Serializable

/** Protected: a repeating weekly timetable item. Checkmarks expire at the start of each new week. */
@Serializable
data class StudyPlanItem(
    val id: Long,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val topic: String = "",
    val completed: Boolean = false,
    val completedWeek: String? = null
)
