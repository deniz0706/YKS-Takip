package com.deniz0706.ykstakip.domain.model

import kotlinx.serialization.Serializable

/** Protected: question/error notebook card model. */
@Serializable
data class QuestionCard(
    val id: String,
    val outcomeId: String,
    val title: String,
    val notes: String,
    val status: QuestionStatus,
    val createdAt: String,
    val dueDate: String,
    val reviewCount: Int = 0,
    val photos: List<String> = emptyList()
)

enum class QuestionStatus { COZULEMEDI, GERI_DON, COZULDU }
