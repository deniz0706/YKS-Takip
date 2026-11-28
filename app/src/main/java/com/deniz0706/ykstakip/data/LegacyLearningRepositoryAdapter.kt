package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.repository.LearningRepository as SharedLearningRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Keeps learning progress on its existing JSON/photo store until the entire
 * learning editor, archive, and photo lifecycle can migrate atomically.
 */
class LegacyLearningRepositoryAdapter(context: Context) : SharedLearningRepository {
    private val repository = LearningRepository.getInstance(context.applicationContext)

    override fun getOutcomes(): Flow<List<LearningOutcome>> = flow {
        emit(repository.outcomes())
    }

    override fun getQuestions(): Flow<List<QuestionCard>> = flow {
        emit(repository.questions())
    }

    override suspend fun saveOutcome(outcome: LearningOutcome) = withContext(Dispatchers.IO) {
        repository.updateOutcome(outcome.id) { outcome }
    }

    override suspend fun deleteOutcome(id: String) = withContext(Dispatchers.IO) {
        repository.deleteCustomOutcome(id)
    }

    override suspend fun saveQuestion(question: QuestionCard) = withContext(Dispatchers.IO) {
        repository.updateQuestion(question.id) { question }
    }

    override suspend fun deleteQuestion(id: String) = withContext(Dispatchers.IO) {
        repository.deleteQuestion(id)
    }
}
