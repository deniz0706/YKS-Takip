package com.deniz0706.ykstakip.viewmodel

import com.deniz0706.ykstakip.data.repository.SqlExamRepository
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
class HomeViewModelTest {

    @Test
    fun empty_exams_should_show_zero_stats() = runBlocking {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val database = com.deniz0706.ykstakip.data.local.DatabaseHolder.get(context)
        database.examQueriesQueries.replaceAllExams()
        val repo = SqlExamRepository(context.applicationContext)
        val exams = repo.getAllExams().first()
        assert(exams.isEmpty())
    }

    @Test
    fun exam_with_all_subjects_should_calculate_net() {
        val exam = Exam(
            id = 1,
            title = "TYT Deneme 1",
            date = LocalDate.now().toString(),
            type = ExamType.TYT,
            subjects = listOf(
                SubjectResult("Türkçe", 30, 5, 5, 30),
                SubjectResult("Matematik", 25, 8, 7, 40),
                SubjectResult("Sosyal Bilgiler", 20, 3, 7, 30),
                SubjectResult("Fen Bilimleri", 18, 4, 8, 30)
            )
        )
        assert(exam.totalCorrect == 93)
        assert(exam.totalWrong == 20)
        assert(exam.totalBlank == 27)
        assert(exam.totalQuestions == 140)
    }
}
