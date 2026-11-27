package com.deniz0706.ykstakip.data.repository

import android.content.Context
import com.deniz0706.ykstakip.data.local.YksDatabase
import com.deniz0706.ykstakip.data.local.YksDatabase.Companion.get
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.model.QuestionStatus
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class SqlLearningRepository(private val context: Context) : LearningRepository {
    private val db = get(context.applicationContext).database

    override fun getOutcomes(): Flow<List<LearningOutcome>> = flow {
        withContext(Dispatchers.IO) {
            val queries = com.deniz0706.ykstakip.data.local.LearningQueries(Db)
            emit(queries.selectAllLearningOutcomes().executeAsList().map { row ->
                LearningOutcome(
                    id = row.id,
                    examType = row.examType,
                    subject = row.subject,
                    topic = row.topic,
                    title = row.title,
                    completed = row.completed == 1,
                    reviewRequested = row.reviewRequested == 1,
                    wrongCount = row.wrongCount,
                    acknowledgedWrongCount = row.acknowledgedWrongCount,
                    nextReviewDate = row.nextReviewDate,
                    reviewLevel = row.reviewLevel,
                    lastReviewedDate = row.lastReviewedDate,
                    custom = row.custom == 1,
                    completedSubtopics = row.completedSubtopics,
                    completedDate = row.completedDate
                )
            })
        }
    }

    override fun getQuestions(): Flow<List<QuestionCard>> = flow {
        withContext(Dispatchers.IO) {
            val queries = com.deniz0706.ykstakip.data.local.LearningQueries(Db)
            emit(queries.selectAllQuestionCards().executeAsList().map { row ->
                QuestionCard(
                    id = row.id,
                    outcomeId = row.outcomeId,
                    title = row.title,
                    notes = row.notes,
                    status = QuestionStatus.valueOf(row.status),
                    createdAt = row.createdAt,
                    dueDate = row.dueDate,
                    reviewCount = row.reviewCount,
                    photos = row.photos
                )
            })
        }
    }

    override suspend fun saveOutcome(outcome: LearningOutcome) = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.LearningQueries(Db)
        queries.insertLearningOutcome(
            outcome.id, outcome.examType, outcome.subject, outcome.topic, outcome.title,
            if (outcome.completed) 1 else 0,
            if (outcome.reviewRequested) 1 else 0,
            outcome.wrongCount, outcome.acknowledgedWrongCount,
            outcome.nextReviewDate, outcome.reviewLevel, outcome.lastReviewedDate,
            if (outcome.custom) 1 else 0, outcome.completedDate, outcome.completedSubtopics
        )
    }

    override suspend fun deleteOutcome(id: String) = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.LearningQueries(Db)
        queries.deleteLearningOutcome(id)
    }

    override suspend fun saveQuestion(question: QuestionCard) = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.LearningQueries(Db)
        queries.insertQuestionCard(
            question.id, question.outcomeId, question.title, question.notes,
            question.status.name, question.createdAt, question.dueDate,
            question.reviewCount, question.photos
        )
    }

    override suspend fun deleteQuestion(id: String) = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.LearningQueries(Db)
        queries.deleteQuestionCard(id)
    }

    private val Db: com.squareup.sqldelight.SqlDatabase
        get() = get(context.applicationContext).database
}
