package com.deniz0706.ykstakip.data

import androidx.test.platform.app.InstrumentationRegistry
import com.deniz0706.ykstakip.data.local.DatabaseHolder
import com.deniz0706.ykstakip.data.repository.SqlExamRepository
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabasePersistenceSmokeTest {

    @Test
    fun repositoriesShareOneOnDiskDatabase() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val firstDatabase = DatabaseHolder.get(context)
        val secondDatabase = DatabaseHolder.get(context)

        val learningRepository = SqlLearningRepository(context)
        learningRepository.getOutcomes().first()
        learningRepository.getQuestions().first()

        assertSame(firstDatabase, secondDatabase)
        assertTrue(context.getDatabasePath("yks_takip.db").exists())

        val id = System.currentTimeMillis()
        val exam = Exam(
            id = id,
            title = "CI database persistence check",
            date = "2026-11-27",
            type = ExamType.TYT,
            subjects = emptyList()
        )

        val writer = SqlExamRepository(context)
        val reader = SqlExamRepository(context)
        try {
            writer.saveExam(exam)
            assertTrue(reader.getAllExams().first().any { it.id == id })
        } finally {
            writer.deleteExam(id)
        }
    }
}
