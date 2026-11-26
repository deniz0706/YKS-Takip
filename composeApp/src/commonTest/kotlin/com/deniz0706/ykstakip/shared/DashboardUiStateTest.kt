package com.deniz0706.ykstakip.shared

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
    fun rejectsNegativeQuestionCountsAndGoals() {
        assertFailsWith<IllegalArgumentException> { DashboardUiState(todayQuestionCount = -1) }
        assertFailsWith<IllegalArgumentException> { DashboardUiState(dailyQuestionGoal = -1) }
    }

    @Test
    fun rejectsInvalidExamTrendValues() {
        assertFailsWith<IllegalArgumentException> { DashboardUiState(tytTrend = listOf(12.0, Double.NaN)) }
        assertFailsWith<IllegalArgumentException> { DashboardUiState(aytTrend = listOf(-0.25)) }
    }
}
