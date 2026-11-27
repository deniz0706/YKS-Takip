package com.deniz0706.ykstakip.data.repository

import com.deniz0706.ykstakip.data.local.YksDatabase
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.model.SubjectResult
import org.junit.Test
import java.time.LocalDate

class SqlExamRepositoryTest {

    @Test
    fun save_and_retrieve_exam() {
        val repo = SqlExamRepository.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>())

        val exam = com.deniz0706.ykstakip.model.Exam(
            id = 42,
            title = "TYT Test",
            date = LocalDate.now().toString(),
            type = ExamType.TYT,
            subjects = listOf(
                SubjectResult("Türkçe", 30, 5, 5, 30)
            )
        )

        repo.saveExam(exam)
        val all = repo.getAllExams()
        val saved = all.find { it.id == 42 }
        assert(saved != null)
        assert(saved!!.title == "TYT Test")
        assert(saved!!.type == ExamType.TYT)
        assert(saved!!.totalCorrect == 30)
    }
}
