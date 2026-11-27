package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.data.local.YksDatabase
import com.deniz0706.ykstakip.domain.model.AytField
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.model.QuestionStatus
import com.deniz0706.ykstakip.domain.model.StudyDay
import com.deniz0706.ykstakip.domain.model.SubjectResult
import com.deniz0706.ykstakip.data.repository.SqlExamRepository
import com.deniz0706.ykstakip.data.repository.SqlLearningRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

/**
 * One-time migration from SharedPreferences to SQLDelight.
 * Preserves all existing user data. Runs only when the migration flag is not set.
 */
object LegacyDataMigrator {
    private const val PREFS = "yks_takip_prefs"
    private const val KEY_MIGRATED_TO_SQL = "migrated_to_sqldelight_v1"

    fun migrateIfNeeded(context: Context) {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_MIGRATED_TO_SQL, false)) return

        runBlocking {
            migrateExams(app, prefs)
            migrateStudyDays(app)
            migrateStudyPlan(app)
            migrateLearning(app)
        }

        prefs.edit().putBoolean(KEY_MIGRATED_TO_SQL, true).apply()
    }

    private fun migrateExams(app: Context, prefs: android.content.SharedPreferences) {
        val raw = prefs.getString("exams_json", null) ?: return
        val exams = try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                try {
                    parseExam(array.getJSONObject(index))
                } catch (_: Exception) {
                    null
                }
            }
        } catch (_: Exception) {
            return
        }
        if (exams.isEmpty()) return
        val repository = SqlExamRepository(app)
        runBlocking { repository.replaceAll(exams) }
    }

    private fun parseExam(json: JSONObject): Exam {
        val arr = json.getJSONArray("subjects")
        val subjects = (0 until arr.length()).map { parseSubjectResult(arr.getJSONObject(it)) }
        return Exam(
            id = json.getLong("id"),
            title = json.getString("title"),
            date = json.getString("date"),
            type = ExamType.valueOf(json.getString("type")),
            aytField = json.optString("aytField", "").takeIf { it.isNotEmpty() }?.let { AytField.valueOf(it) },
            subjects = subjects,
            notes = json.optString("notes", ""),
            publisher = json.optString("publisher", "")
        )
    }

    private fun parseSubjectResult(json: JSONObject): SubjectResult =
        SubjectResult(
            subject = json.getString("subject"),
            correct = json.getInt("correct"),
            wrong = json.getInt("wrong"),
            blank = json.getInt("blank"),
            timeMinutes = json.getInt("timeMinutes")
        )

    private fun parseLearningOutcome(json: JSONObject): LearningOutcome =
        LearningOutcome(
            id = json.getString("id"),
            examType = json.optString("examType", "TYT"),
            subject = json.optString("subject", ""),
            topic = json.optString("topic", ""),
            title = json.optString("title", ""),
            completed = json.optBoolean("completed"),
            reviewRequested = json.optBoolean("reviewRequested"),
            wrongCount = json.optInt("wrongCount"),
            acknowledgedWrongCount = json.optInt("acknowledgedWrongCount"),
            nextReviewDate = json.optString("nextReviewDate", ""),
            reviewLevel = json.optInt("reviewLevel"),
            lastReviewedDate = json.optString("lastReviewedDate", ""),
            custom = json.optBoolean("custom"),
            completedSubtopics = (json.optJSONArray("completedSubtopics") ?: JSONArray()).let { a ->
                (0 until a.length()).map { a.optString(it) }.filter { it.isNotBlank() }.distinct()
            },
            completedDate = json.optString("completedDate", "")
        )

    private fun parseQuestionCard(json: JSONObject): QuestionCard {
        val p = json.optJSONArray("photos") ?: JSONArray()
        return QuestionCard(
            id = json.getString("id"),
            outcomeId = json.optString("outcomeId", ""),
            title = json.optString("title", ""),
            notes = json.optString("notes", ""),
            status = runCatching { QuestionStatus.valueOf(json.optString("status")) }.getOrDefault(QuestionStatus.GERI_DON),
            createdAt = json.optString("createdAt", ""),
            dueDate = json.optString("dueDate", ""),
            reviewCount = json.optInt("reviewCount"),
            photos = (0 until p.length()).map { p.getString(it) }
        )
    }

    private fun migrateStudyDays(app: Context) {
        val prefs = app.getSharedPreferences("yks_takip_study", Context.MODE_PRIVATE)
        val raw = prefs.getString("study_days", null) ?: return
        val days = try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                try {
                    val obj = array.getJSONObject(index)
                    StudyDay(
                        date = obj.getString("date"),
                        questionCount = obj.getInt("questionCount"),
                        studyMinutes = obj.getInt("studyMinutes"),
                        estimated = obj.optBoolean("estimated", false)
                    )
                } catch (_: Exception) {
                    null
                }
            }
        } catch (_: Exception) {
            return
        }
        if (days.isEmpty()) return
        val db = YksDatabase(AndroidSqliteDriver(YksDatabase.Schema, app))
        days.forEach { day ->
            db.studyDayQueriesQueries.insertStudyDay(
                date = day.date,
                question_count = day.questionCount.toLong(),
                study_minutes = day.studyMinutes.toLong(),
                estimated = if (day.estimated) 1 else 0
            )
        }
    }

    private fun migrateStudyPlan(app: Context) {
        val prefs = app.getSharedPreferences("yks_takip_study_plan", Context.MODE_PRIVATE)
        val raw = prefs.getString("study_plan_items", null) ?: return
        val items = try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                try {
                    val obj = array.getJSONObject(index)
                    StudyPlanItem(
                        id = obj.getLong("id"),
                        dayOfWeek = obj.getInt("dayOfWeek"),
                        startTime = obj.getString("startTime"),
                        endTime = obj.getString("endTime"),
                        subject = obj.getString("subject"),
                        topic = obj.optString("topic", ""),
                        completed = obj.optBoolean("completed", false),
                        completedWeek = obj.optString("completedWeek", "")
                    )
                } catch (_: Exception) {
                    null
                }
            }
        } catch (_: Exception) {
            return
        }
        if (items.isEmpty()) return
        val db = YksDatabase(AndroidSqliteDriver(YksDatabase.Schema, app))
        items.forEach { item ->
            db.studyPlanQueriesQueries.insertStudyPlanItem(
                item.id,
                item.dayOfWeek.toLong(),
                item.startTime,
                item.endTime,
                item.subject,
                item.topic,
                if (item.completed) 1L else 0L,
                item.completedWeek
            )
        }
    }

    private fun migrateLearning(app: Context) {
        val prefs = app.getSharedPreferences("yks_learning_journal", Context.MODE_PRIVATE)
        val outcomesRaw = prefs.getString("outcomes", "[]") ?: "[]"
        val questionsRaw = prefs.getString("questions", "[]") ?: "[]"

        val outcomes = try {
            val array = JSONArray(outcomesRaw)
            (0 until array.length()).mapNotNull { index ->
                try {
                    parseLearningOutcome(array.getJSONObject(index))
                } catch (_: Exception) {
                    null
                }
            }
        } catch (_: Exception) {
            emptyList()
        }

        val questions = try {
            val array = JSONArray(questionsRaw)
            (0 until array.length()).mapNotNull { index ->
                try {
                    parseQuestionCard(array.getJSONObject(index))
                } catch (_: Exception) {
                    null
                }
            }
        } catch (_: Exception) {
            emptyList()
        }

        if (outcomes.isEmpty() && questions.isEmpty()) return

        val repository = SqlLearningRepository(app)
        runBlocking {
            outcomes.forEach { repository.saveOutcome(it) }
            questions.forEach { repository.saveQuestion(it) }
        }
    }

    private data class StudyPlanItem(
        val id: Long, val dayOfWeek: Int, val startTime: String, val endTime: String,
        val subject: String, val topic: String, val completed: Boolean, val completedWeek: String
    )
}
