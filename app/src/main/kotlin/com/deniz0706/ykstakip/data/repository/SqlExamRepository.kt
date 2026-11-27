package com.deniz0706.ykstakip.data.repository

import android.content.Context
import com.deniz0706.ykstakip.data.local.YksDatabase
import com.deniz0706.ykstakip.data.local.YksDatabase.Companion.get
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.SubjectResult
import com.deniz0706.ykstakip.domain.repository.ExamRepository
import com.squareup.sqldelight.android.sqliteDriver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class SqlExamRepository(private val context: Context) : ExamRepository {
    private val db = get(context.applicationContext).database

    override fun getAllExams(): Flow<List<Exam>> = flow {
        withContext(Dispatchers.IO) {
            val queries = com.deniz0706.ykstakip.data.local.ExamQueries(Db)
            val rows = queries.selectAllExams().executeAsList()
            emit(rows.map { row ->
                val subjects = queries.selectSubjectsForExam(row.id).executeAsList().map {
                    SubjectResult(it.subject, it.correct, it.wrong, it.blank, it.timeMinutes)
                }
                Exam(
                    id = row.id,
                    title = row.title,
                    date = row.date,
                    type = ExamType.valueOf(row.type),
                    aytField = row.aytField?.let { AytField.valueOf(it) },
                    subjects = subjects,
                    notes = row.notes,
                    publisher = row.publisher
                )
            })
        }
    }

    override fun getExamById(id: Long): Flow<Exam?> = flow {
        withContext(Dispatchers.IO) {
            val queries = com.deniz0706.ykstakip.data.local.ExamQueries(Db)
            val row = queries.selectExamById(id).executeAsOneOrNull()
            row?.let {
                val subjects = queries.selectSubjectsForExam(it.id).executeAsList().map {
                    SubjectResult(it.subject, it.correct, it.wrong, it.blank, it.timeMinutes)
                }
                Exam(
                    id = it.id,
                    title = it.title,
                    date = it.date,
                    type = ExamType.valueOf(it.type),
                    aytField = it.aytField?.let { AytField.valueOf(it) },
                    subjects = subjects,
                    notes = it.notes,
                    publisher = it.publisher
                )
            }
        }
    }

    override suspend fun saveExam(exam: Exam) = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.ExamQueries(Db)
        queries.insertExam(
            exam.id, exam.title, exam.date, exam.type.name,
            exam.aytField?.name, exam.notes, exam.publisher
        )
        queries.deleteExamSubjects(exam.id)
        exam.subjects.forEach { subject ->
            queries.insertExamSubjects(exam.id, subject.subject, subject.correct, subject.wrong, subject.blank, subject.timeMinutes)
        }
    }

    override suspend fun deleteExam(id: Long) = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.ExamQueries(Db)
        queries.deleteExam(id)
    }

    override suspend fun replaceAll(exams: List<Exam>) = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.ExamQueries(Db)
        queries.replaceAllExams()
        exams.forEach { exam ->
            queries.insertExam(
                exam.id, exam.title, exam.date, exam.type.name,
                exam.aytField?.name, exam.notes, exam.publisher
            )
            exam.subjects.forEach { subject ->
                queries.insertExamSubjects(exam.id, subject.subject, subject.correct, subject.wrong, subject.blank, subject.timeMinutes)
            }
        }
    }

    override suspend fun nextId(): Long = withContext(Dispatchers.IO) {
        System.currentTimeMillis()
    }

    private val Db: com.squareup.sqldelight.SqlDatabase
        get() = get(context.applicationContext).database
}
