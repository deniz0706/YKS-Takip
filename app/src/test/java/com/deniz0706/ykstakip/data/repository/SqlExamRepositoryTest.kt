package com.deniz0706.ykstakip.data.repository

import com.deniz0706.ykstakip.data.local.YksDatabase
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.SubjectResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class SqlExamRepositoryTest {

    @Test
    fun save_and_retrieve_exam() = runBlocking {
        val repo = SqlExamRepository.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>())

        val exam = Exam(
            id = 42,
            title = "TYT Test",
            date = LocalDate.now().toString(),
            type = ExamType.TYT,
            subjects = listOf(
                SubjectResult("Türkçe", 30, 5, 5, 30)
            )
        )

        repo.saveExam(exam)
        val all = repo.getAllExams().first()
        val saved = all.find { it.id == 42L }
        assert(saved != null)
        assert(saved!!.title == "TYT Test")
        assert(saved!!.type == ExamType.TYT)
        assert(saved!!.totalCorrect == 30)
    }
}
