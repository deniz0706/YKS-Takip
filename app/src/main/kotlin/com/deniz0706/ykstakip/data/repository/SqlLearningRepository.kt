package com.deniz0706.ykstakip.data.repository

import android.content.Context
import com.deniz0706.ykstakip.data.local.DatabaseHolder
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.model.QuestionStatus
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.deniz0706.ykstakip.data.LegacyDataWriteGuard
import org.json.JSONArray
import org.json.JSONObject

class SqlLearningRepository(private val context: Context) : LearningRepository {
    private val writeGuard = LegacyDataWriteGuard("Öğrenme kayıtları")
    private val database = DatabaseHolder.get(context)

    override fun getOutcomes(): Flow<List<LearningOutcome>> = flow {
        withContext(Dispatchers.IO) {
            val queries = database.learningQueriesQueries
            emit(queries.selectAllLearningOutcomes().executeAsList().map { row ->
                LearningOutcome(
                    id = row.id,
                    examType = row.exam_type,
                    subject = row.subject,
                    topic = row.topic,
                    title = row.title,
                    completed = row.completed == 1L,
                    reviewRequested = row.review_requested == 1L,
                    wrongCount = row.wrong_count.toInt(),
                    acknowledgedWrongCount = row.acknowledged_wrong_count.toInt(),
                    nextReviewDate = row.next_review_date ?: "",
                    reviewLevel = row.review_level.toInt(),
                    lastReviewedDate = row.last_reviewed_date ?: "",
                    custom = row.custom == 1L,
                    completedSubtopics = row.completed_subtopics?.let { json ->
                        try {
                            (0 until JSONArray(json).length()).mapNotNull { index ->
                                JSONArray(json).optString(index).takeIf { it.isNotBlank() }
                            }.distinct()
                        } catch (_: Exception) {
                            emptyList()
                        }
                    } ?: emptyList(),
                    completedDate = row.completed_date ?: ""
                )
            })
        }
    }

    override fun getQuestions(): Flow<List<QuestionCard>> = flow {
        withContext(Dispatchers.IO) {
            val queries = database.learningQueriesQueries
            emit(queries.selectAllQuestionCards().executeAsList().map { row ->
                QuestionCard(
                    id = row.id,
                    outcomeId = row.outcome_id,
                    title = row.title,
                    notes = row.notes,
                    status = QuestionStatus.valueOf(row.status),
                    createdAt = row.created_at,
                    dueDate = row.due_date,
                    reviewCount = row.review_count.toInt(),
                    photos = row.photos?.let { json ->
                        try {
                            (0 until JSONArray(json).length()).mapNotNull { index ->
                                JSONArray(json).optString(index).takeIf { it.isNotBlank() }
                            }.distinct()
                        } catch (_: Exception) {
                            emptyList()
                        }
                    } ?: emptyList()
                )
            })
        }
    }

    override suspend fun saveOutcome(outcome: LearningOutcome) = withContext(Dispatchers.IO) {
        writeGuard.requireWritable()
        val queries = database.learningQueriesQueries
        queries.insertLearningOutcome(
            outcome.id, outcome.examType, outcome.subject, outcome.topic, outcome.title,
            if (outcome.completed) 1L else 0L,
            if (outcome.reviewRequested) 1L else 0L,
            outcome.wrongCount.toLong(), outcome.acknowledgedWrongCount.toLong(),
            outcome.nextReviewDate, outcome.reviewLevel.toLong(), outcome.lastReviewedDate,
            if (outcome.custom) 1L else 0L, outcome.completedDate,
            JSONArray(outcome.completedSubtopics.distinct()).toString()
        )
    }

    override suspend fun deleteOutcome(id: String) = withContext(Dispatchers.IO) {
        writeGuard.requireWritable()
        val queries = database.learningQueriesQueries
        queries.deleteLearningOutcome(id)
    }

    override suspend fun saveQuestion(question: QuestionCard) = withContext(Dispatchers.IO) {
        writeGuard.requireWritable()
        val queries = database.learningQueriesQueries
        queries.insertQuestionCard(
            question.id, question.outcomeId, question.title, question.notes,
            question.status.name, question.createdAt, question.dueDate,
            question.reviewCount.toLong(),
            JSONArray(question.photos.distinct()).toString()
        )
    }

    override suspend fun deleteQuestion(id: String) = withContext(Dispatchers.IO) {
        writeGuard.requireWritable()
        val queries = database.learningQueriesQueries
        queries.deleteQuestionCard(id)
    }
}
