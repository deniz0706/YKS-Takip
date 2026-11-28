package com.deniz0706.ykstakip.shared

import com.deniz0706.ykstakip.domain.AytField
import com.deniz0706.ykstakip.domain.Exam
import com.deniz0706.ykstakip.domain.ExamType
import com.deniz0706.ykstakip.domain.SubjectResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DashboardUiStateTest {
    @Test
    fun emptyDashboardIsAValidInitialState() {
        val state = DashboardUiState()
        assertEquals(emptyList(), state.tytTrend)
        assertEquals(null, state.tytLatestNet)
    }

    @Test
    fun mapsPersistedTytAndAytExamHistoryIntoSharedDashboard() {
        val exams = listOf(
            exam(1, "2026-11-01", ExamType.TYT, 30),
            exam(2, "2026-11-02", ExamType.TYT, 35),
            exam(3, "2026-11-01", ExamType.AYT, 22),
            exam(4, "2026-11-02", ExamType.AYT, 26)
        )

        val state = DashboardUiState.fromExams(
            exams = exams,
            tytGoal = 100f,
            aytGoal = 70f,
            daysUntilExam = 205,
            todayQuestionCount = 85,
            dailyQuestionGoal = 120
        )

        assertEquals(35.0, state.tytLatestNet)
        assertEquals(30.0, state.tytPreviousNet)
        assertEquals(listOf(30.0, 35.0), state.tytTrend)
        assertEquals(26.0, state.aytLatestNet)
        assertEquals(22.0, state.aytPreviousNet)
        assertEquals(listOf(22.0, 26.0), state.aytTrend)
        assertEquals(100f, state.tytGoal)
        assertEquals(70f, state.aytGoal)
        assertEquals(205, state.daysUntilExam)
        assertEquals(85, state.todayQuestionCount)
        assertEquals(120, state.dailyQuestionGoal)
    }

    @Test
    fun rejectsNegativeQuestionCountsAndGoals() {
        assertFailsWith<IllegalArgumentException> { DashboardUiState(todayQuestionCount = -1) }
        assertFailsWith<IllegalArgumentException> { DashboardUiState(dailyQuestionGoal = -1) }
    }

    @Test
    fun acceptsNegativeNetButRejectsNonFiniteValues() {
        val state = DashboardUiState(tytTrend = listOf(-2.5, 0.0, 12.0))
        assertEquals(listOf(-2.5, 0.0, 12.0), state.tytTrend)
        assertFailsWith<IllegalArgumentException> {
            DashboardUiState(tytTrend = listOf(12.0, Double.NaN))
        }
        assertFailsWith<IllegalArgumentException> {
            DashboardUiState(aytTrend = listOf(Double.POSITIVE_INFINITY))
        }
        assertFailsWith<IllegalArgumentException> {
            DashboardUiState(tytGoal = Float.POSITIVE_INFINITY)
        }
    }

    private fun exam(id: Long, date: String, type: ExamType, correct: Int) = Exam(
        id = id,
        title = "Deneme $id",
        date = date,
        type = type,
        aytField = if (type == ExamType.AYT) AytField.SAYISAL else null,
        subjects = listOf(SubjectResult("Matematik", 40, correct, 0, 30))
    )
}
