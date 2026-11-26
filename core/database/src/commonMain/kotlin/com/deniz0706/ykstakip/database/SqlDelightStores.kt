package com.deniz0706.ykstakip.database

import app.cash.sqldelight.db.SqlDriver
import com.deniz0706.ykstakip.domain.AytField
import com.deniz0706.ykstakip.domain.Exam
import com.deniz0706.ykstakip.domain.ExamType
import com.deniz0706.ykstakip.domain.StudyDay
import com.deniz0706.ykstakip.domain.SubjectResult
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Transactional SQLDelight adapter for the shared exam aggregate.
 *
 * Every parent + child replacement is one SQLite transaction. Callers must not
 * separately mutate SubjectResultRecord rows for an existing exam.
 */
class SqlDelightExamStore(
    private val database: YksDatabase,
    private val json: Json = Json
) {
    fun getAll(): List<Exam> = database.examQueries.selectAllExams()
        .executeAsList()
        .map { row ->
            val subjects = database.examQueries.selectSubjectsForExam(row.id)
                .executeAsList()
                .map { subject ->
                    SubjectResult(
                        subject = subject.subject,
                        questionCount = subject.questionCount.toInt(),
                        correct = subject.correct.toInt(),
                        wrong = subject.wrong.toInt(),
                        timeMinutes = subject.timeMinutes.toInt(),
                        weakTopics = json.decodeFromString(subject.weakTopicsJson)
                    )
                }
            Exam(
                id = row.id,
                title = row.title,
                date = row.examDate,
                type = ExamType.valueOf(row.examType),
                aytField = row.aytField?.let(AytField::valueOf),
                subjects = subjects,
                notes = row.notes,
                publisher = row.publisher
            )
        }

    fun get(id: Long): Exam? = database.examQueries.selectExamById(id)
        .executeAsOneOrNull()
        ?.let { row ->
            val subjects = database.examQueries.selectSubjectsForExam(id)
                .executeAsList()
                .map { subject ->
                    SubjectResult(
                        subject = subject.subject,
                        questionCount = subject.questionCount.toInt(),
                        correct = subject.correct.toInt(),
                        wrong = subject.wrong.toInt(),
                        timeMinutes = subject.timeMinutes.toInt(),
                        weakTopics = json.decodeFromString(subject.weakTopicsJson)
                    )
                }
            Exam(
                id = row.id,
                title = row.title,
                date = row.examDate,
                type = ExamType.valueOf(row.examType),
                aytField = row.aytField?.let(AytField::valueOf),
                subjects = subjects,
                notes = row.notes,
                publisher = row.publisher
            )
        }

    fun save(exam: Exam) {
        database.transaction {
            upsertParent(exam)
            database.examQueries.deleteSubjectsForExam(exam.id)
            insertSubjects(exam)
        }
    }

    fun replaceAll(exams: List<Exam>) {
        require(exams.map { it.id }.distinct().size == exams.size) {
            "Exam IDs must be unique."
        }
        database.transaction {
            database.examQueries.deleteAllExams()
            exams.forEach { exam ->
                upsertParent(exam)
                insertSubjects(exam)
            }
        }
    }

    fun delete(id: Long) {
        database.examQueries.deleteExamById(id)
    }

    private fun upsertParent(exam: Exam) {
        database.examQueries.upsertExam(
            id = exam.id,
            title = exam.title,
            examDate = exam.date,
            examType = exam.type.name,
            aytField = exam.aytField?.name,
            notes = exam.notes,
            publisher = exam.publisher
        )
    }

    private fun insertSubjects(exam: Exam) {
        exam.subjects.forEachIndexed { index, subject ->
            database.examQueries.insertSubjectResult(
                examId = exam.id,
                subjectOrder = index.toLong(),
                subject = subject.subject,
                questionCount = subject.questionCount.toLong(),
                correct = subject.correct.toLong(),
                wrong = subject.wrong.toLong(),
                timeMinutes = subject.timeMinutes.toLong(),
                weakTopicsJson = json.encodeToString(subject.weakTopics)
            )
        }
    }
}

/** SQLDelight-backed daily study history adapter. */
class SqlDelightStudyDayStore(private val database: YksDatabase) {
    fun getAll(): List<StudyDay> = database.studyDayQueries.selectAllStudyDays()
        .executeAsList()
        .map { StudyDay(it.studyDate, it.questionCount.toInt(), it.studyMinutes.toInt(), it.estimated != 0L) }

    fun get(date: String): StudyDay? = database.studyDayQueries.selectStudyDay(date)
        .executeAsOneOrNull()
        ?.let { StudyDay(it.studyDate, it.questionCount.toInt(), it.studyMinutes.toInt(), it.estimated != 0L) }

    fun save(day: StudyDay) {
        database.studyDayQueries.upsertStudyDay(
            studyDate = day.date,
            questionCount = day.questionCount.toLong(),
            studyMinutes = day.studyMinutes.toLong(),
            estimated = if (day.estimated) 1L else 0L
        )
    }

    fun delete(date: String) {
        database.studyDayQueries.deleteStudyDay(date)
    }
}
