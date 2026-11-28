package com.deniz0706.ykstakip.data.repository

import android.content.Context
import com.deniz0706.ykstakip.data.local.DatabaseHolder
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.SubjectResult
import com.deniz0706.ykstakip.domain.repository.ExamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class SqlExamRepository(private val context: Context) : ExamRepository {
    private val db = DatabaseHolder.get(context)

    override fun getAllExams(): Flow<List<Exam>> = flow {
        val exams = withContext(Dispatchers.IO) {
            val queries = db.examQueriesQueries
            val rows = queries.selectAllExams().executeAsList()
            rows.map { row ->
                val subjects = queries.selectSubjectsForExam(row.id).executeAsList().map {
                    SubjectResult(it.subject, it.correct.toInt(), it.wrong.toInt(), it.blank.toInt(), it.time_minutes.toInt())
                }
                Exam(
                    id = row.id,
                    title = row.title,
                    date = row.date,
                    type = ExamType.valueOf(row.type),
                    aytField = row.ayt_field?.let { AytField.valueOf(it) },
                    subjects = subjects,
                    notes = row.notes ?: "",
                    publisher = row.publisher ?: ""
                )
            }
        }
        emit(exams)
    }

    override fun getExamById(id: Long): Flow<Exam?> = flow {
        val exam = withContext(Dispatchers.IO) {
            val queries = db.examQueriesQueries
            val row = queries.selectExamById(id).executeAsOneOrNull()
            row?.let {
                val subjects = queries.selectSubjectsForExam(it.id).executeAsList().map {
                    SubjectResult(it.subject, it.correct.toInt(), it.wrong.toInt(), it.blank.toInt(), it.time_minutes.toInt())
                }
                Exam(
                    id = it.id,
                    title = it.title,
                    date = it.date,
                    type = ExamType.valueOf(it.type),
                    aytField = it.ayt_field?.let { AytField.valueOf(it) },
                    subjects = subjects,
                    notes = it.notes ?: "",
                    publisher = it.publisher ?: ""
                )
            }
        }
        emit(exam)
    }

    override suspend fun saveExam(exam: Exam) = withContext(Dispatchers.IO) {
        val queries = db.examQueriesQueries
        queries.insertExam(
            exam.id, exam.title, exam.date, exam.type.name,
            exam.aytField?.name, exam.notes, exam.publisher
        )
        queries.deleteExamSubjects(exam.id)
        exam.subjects.forEach { subject ->
            queries.insertExamSubjects(exam.id, subject.subject, subject.correct.toLong(), subject.wrong.toLong(), subject.blank.toLong(), subject.timeMinutes.toLong())
        }
    }

    override suspend fun deleteExam(id: Long) = withContext(Dispatchers.IO) {
        val queries = db.examQueriesQueries
        queries.deleteExam(id)
    }

    override suspend fun replaceAll(exams: List<Exam>) = withContext(Dispatchers.IO) {
        val queries = db.examQueriesQueries
        queries.replaceAllExams()
        exams.forEach { exam ->
            queries.insertExam(
                exam.id, exam.title, exam.date, exam.type.name,
                exam.aytField?.name, exam.notes, exam.publisher
            )
            exam.subjects.forEach { subject ->
                queries.insertExamSubjects(exam.id, subject.subject, subject.correct.toLong(), subject.wrong.toLong(), subject.blank.toLong(), subject.timeMinutes.toLong())
            }
        }
    }

    override suspend fun nextId(): Long = withContext(Dispatchers.IO) {
        System.currentTimeMillis()
    }

    companion object {
        @Volatile private var instance: SqlExamRepository? = null
        fun getInstance(context: Context): SqlExamRepository =
            instance ?: synchronized(this) {
                instance ?: SqlExamRepository(context.applicationContext).also { instance = it }
            }
    }
}
