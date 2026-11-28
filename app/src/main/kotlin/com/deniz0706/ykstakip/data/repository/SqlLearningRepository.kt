package com.deniz0706.ykstakip.data.repository

import android.content.Context
import com.deniz0706.ykstakip.data.LegacyDataWriteGuard
import com.deniz0706.ykstakip.data.local.DatabaseHolder
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.model.QuestionStatus
import com.deniz0706.ykstakip.domain.repository.LearningRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray

class SqlLearningRepository(private val context: Context) : LearningRepository {
    private val writeGuard = LegacyDataWriteGuard("Öğrenme kayıtları")
    private val database = DatabaseHolder.get(context)

    override fun getOutcomes(): Flow<List<LearningOutcome>> = flow {
        // Do the blocking query on IO, then emit back in the collector context.
        // Emitting from inside withContext(IO) violates Flow's context invariant.
        val outcomes = withContext(Dispatchers.IO) {
            database.learningQueriesQueries.selectAllLearningOutcomes().executeAsList().map { row ->
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
            }
        }
        emit(outcomes)
    }

    override fun getQuestions(): Flow<List<QuestionCard>> = flow {
        // Keep Flow emission in the collector context for the same reason as above.
        val questions = withContext(Dispatchers.IO) {
            database.learningQueriesQueries.selectAllQuestionCards().executeAsList().map { row ->
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
            }
        }
        emit(questions)
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
        database.learningQueriesQueries.deleteLearningOutcome(id)
    }

    override suspend fun saveQuestion(question: QuestionCard) = withContext(Dispatchers.IO) {
        writeGuard.requireWritable()
        database.learningQueriesQueries.insertQuestionCard(
            question.id, question.outcomeId, question.title, question.notes,
            question.status.name, question.createdAt, question.dueDate,
            question.reviewCount.toLong(),
            JSONArray(question.photos.distinct()).toString()
        )
    }

    override suspend fun deleteQuestion(id: String) = withContext(Dispatchers.IO) {
        writeGuard.requireWritable()
        database.learningQueriesQueries.deleteQuestionCard(id)
    }
}
