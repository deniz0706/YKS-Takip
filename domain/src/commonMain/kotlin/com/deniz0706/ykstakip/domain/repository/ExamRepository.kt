package com.deniz0706.ykstakip.domain.repository

import com.deniz0706.ykstakip.domain.model.Exam
import kotlinx.coroutines.flow.Flow

interface ExamRepository {
    fun getAllExams(): Flow<List<Exam>>
    fun getExamById(id: Long): Flow<Exam?>
    suspend fun saveExam(exam: Exam)
    suspend fun deleteExam(id: Long)
    suspend fun replaceAll(exams: List<Exam>)
    suspend fun nextId(): Long
}
