package com.deniz0706.ykstakip.domain.repository

import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import kotlinx.coroutines.flow.Flow

interface LearningRepository {
    fun getOutcomes(): Flow<List<LearningOutcome>>
    fun getQuestions(): Flow<List<QuestionCard>>
    suspend fun saveOutcome(outcome: LearningOutcome)
    suspend fun deleteOutcome(id: String)
    suspend fun saveQuestion(question: QuestionCard)
    suspend fun deleteQuestion(id: String)
}
