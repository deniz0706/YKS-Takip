package com.deniz0706.ykstakip.ui.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.repository.ExamRepository
import com.deniz0706.ykstakip.domain.repository.LearningRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {
    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun changingExamTypeRefiltersAlreadyLoadedExamsWithoutRepositoryEmission() {
        val exams = MutableStateFlow(
            listOf(
                exam(1L, ExamType.TYT),
                exam(2L, ExamType.AYT, AytField.SAYISAL)
            )
        )
        val viewModel = HomeViewModel(
            ApplicationProvider.getApplicationContext<Application>(),
            FakeExamRepository(exams),
            FakeLearningRepository()
        )

        assertEquals(listOf(1L), viewModel.uiState.value.exams.map { it.id })

        // No repository flow emits here. Selection itself must trigger recomputation.
        viewModel.selectExamType(ExamType.AYT)

        assertEquals(ExamType.AYT, viewModel.uiState.value.selectedType)
        assertEquals(listOf(2L), viewModel.uiState.value.exams.map { it.id })
    }

    private fun exam(id: Long, type: ExamType, field: AytField? = null) = Exam(
        id = id,
        title = "Deneme $id",
        date = "2026-11-28",
        type = type,
        aytField = field,
        subjects = emptyList()
    )

    private class FakeExamRepository(
        private val exams: MutableStateFlow<List<Exam>>
    ) : ExamRepository {
        override fun getAllExams(): Flow<List<Exam>> = exams
        override fun getExamById(id: Long): Flow<Exam?> = flowOf(exams.value.firstOrNull { it.id == id })
        override suspend fun saveExam(exam: Exam) { exams.value = exams.value.filterNot { it.id == exam.id } + exam }
        override suspend fun deleteExam(id: Long) { exams.value = exams.value.filterNot { it.id == id } }
        override suspend fun replaceAll(exams: List<Exam>) { this.exams.value = exams }
        override suspend fun nextId(): Long = (exams.value.maxOfOrNull { it.id } ?: 0L) + 1L
    }

    private class FakeLearningRepository : LearningRepository {
        override fun getOutcomes(): Flow<List<LearningOutcome>> = flowOf(emptyList())
        override fun getQuestions(): Flow<List<QuestionCard>> = flowOf(emptyList())
        override suspend fun saveOutcome(outcome: LearningOutcome) = Unit
        override suspend fun deleteOutcome(id: String) = Unit
        override suspend fun saveQuestion(question: QuestionCard) = Unit
        override suspend fun deleteQuestion(id: String) = Unit
    }
}
