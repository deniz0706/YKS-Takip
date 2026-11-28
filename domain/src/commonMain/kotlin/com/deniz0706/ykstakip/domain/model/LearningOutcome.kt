package com.deniz0706.ykstakip.domain.model

import kotlinx.serialization.Serializable

/** Protected: learning outcome / curriculum progress model. */
@Serializable
data class LearningOutcome(
    val id: String,
    val examType: String,
    val subject: String,
    val topic: String,
    val title: String,
    val completed: Boolean = false,
    val reviewRequested: Boolean = false,
    val wrongCount: Int = 0,
    val acknowledgedWrongCount: Int = 0,
    val nextReviewDate: String = "",
    val reviewLevel: Int = 0,
    val lastReviewedDate: String = "",
    val custom: Boolean = false,
    val completedSubtopics: List<String> = emptyList(),
    val completedDate: String = ""
)
