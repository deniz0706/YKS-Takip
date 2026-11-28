package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.data.local.DatabaseHolder
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.model.QuestionStatus
import com.deniz0706.ykstakip.domain.model.StudyDay
import com.deniz0706.ykstakip.domain.model.SubjectResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

/**
 * One-time migration from SharedPreferences to SQLDelight.
 * Every insert is safe to repeat so an interrupted migration can resume.
 */
object LegacyDataMigrator {
    private const val PREFS = "yks_takip_prefs"
    private const val KEY_MIGRATED_TO_SQL = "migrated_to_sqldelight_persistent_v2"

    fun migrateIfNeeded(context: Context) {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_MIGRATED_TO_SQL, false)) return

        runBlocking(Dispatchers.IO) {
            migrateExams(app, prefs)
            migrateStudyDays(app)
            migrateStudyPlan(app)
            migrateLearning(app)
        }

        // Set only after every migration step has completed successfully.
        check(prefs.edit().putBoolean(KEY_MIGRATED_TO_SQL, true).commit()) {
            "Veri aktarımı tamamlandı işareti kaydedilemedi; güvenli tekrar deneme yapılacak."
        }
    }

    /**
     * Parse every legacy row before writing any row from that collection.
     * A malformed record must never be silently skipped and then marked migrated.
     * Inserts remain idempotent, so a later retry can safely resume other collections.
     */
    private fun <T> parseLegacyArray(
        raw: String,
        label: String,
        parse: (JSONObject) -> T
    ): List<T> {
        val array = try {
            JSONArray(raw)
        } catch (error: Exception) {
            throw IllegalStateException("$label verisi okunamadı; özgün kayıtlar korunuyor.", error)
        }
        return (0 until array.length()).map { index ->
            val item = try {
                array.getJSONObject(index)
            } catch (error: Exception) {
                throw IllegalStateException("$label içindeki ${index + 1}. kayıt okunamadı.", error)
            }
            try {
                parse(item)
            } catch (error: Exception) {
                throw IllegalStateException("$label içindeki ${index + 1}. kayıt geçersiz.", error)
            }
        }
    }

    private fun migrateExams(app: Context, prefs: android.content.SharedPreferences) {
        val raw = prefs.getString("exams_json", null) ?: return
        val exams = parseLegacyArray(raw, "Deneme kayıtları", ::parseExam)
        if (exams.isEmpty()) return
        val queries = DatabaseHolder.get(app).examQueriesQueries
        exams.forEach { exam ->
            // Ignore existing rows so a retry cannot overwrite edits made after
            // an interrupted migration. A missing subject set is filled once.
            queries.insertExamBulk(
                exam.id, exam.title, exam.date, exam.type.name,
                exam.aytField?.name, exam.notes, exam.publisher
            )
            if (queries.selectSubjectsForExam(exam.id).executeAsList().isEmpty()) {
                exam.subjects.forEach { subject ->
                    queries.insertExamSubjects(
                        exam.id, subject.subject, subject.correct.toLong(),
                        subject.wrong.toLong(), subject.blank.toLong(), subject.timeMinutes.toLong()
                    )
                }
            }
        }
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
        val days = parseLegacyArray(raw, "Çalışma geçmişi") { obj ->
            StudyDay(
                date = obj.getString("date"),
                questionCount = obj.getInt("questionCount"),
                studyMinutes = obj.getInt("studyMinutes"),
                estimated = obj.optBoolean("estimated", false)
            )
        }
        if (days.isEmpty()) return
        val db = DatabaseHolder.get(app)
        days.forEach { day ->
            db.studyDayQueriesQueries.insertStudyDayBulk(
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
        val items = parseLegacyArray(raw, "Haftalık çalışma planı") { obj ->
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
        }
        if (items.isEmpty()) return
        val db = DatabaseHolder.get(app)
        items.forEach { item ->
            db.studyPlanQueriesQueries.insertStudyPlanItemBulk(
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

        val outcomes = parseLegacyArray(outcomesRaw, "Öğrenme kayıtları", ::parseLearningOutcome)
        val questions = parseLegacyArray(questionsRaw, "Soru kartları", ::parseQuestionCard)

        if (outcomes.isEmpty() && questions.isEmpty()) return

        val db = DatabaseHolder.get(app)
        val queries = db.learningQueriesQueries
        outcomes.forEach { outcome ->
            queries.insertLearningOutcomeBulk(
                outcome.id, outcome.examType, outcome.subject, outcome.topic, outcome.title,
                if (outcome.completed) 1L else 0L,
                if (outcome.reviewRequested) 1L else 0L,
                outcome.wrongCount.toLong(), outcome.acknowledgedWrongCount.toLong(),
                outcome.nextReviewDate, outcome.reviewLevel.toLong(), outcome.lastReviewedDate,
                if (outcome.custom) 1L else 0L, outcome.completedDate,
                JSONArray(outcome.completedSubtopics.distinct()).toString()
            )
        }
        questions.forEach { question ->
            queries.insertQuestionCardBulk(
                question.id, question.outcomeId, question.title, question.notes,
                question.status.name, question.createdAt, question.dueDate,
                question.reviewCount.toLong(), JSONArray(question.photos.distinct()).toString()
            )
        }
    }

    private data class StudyPlanItem(
        val id: Long, val dayOfWeek: Int, val startTime: String, val endTime: String,
        val subject: String, val topic: String, val completed: Boolean, val completedWeek: String
    )
}
