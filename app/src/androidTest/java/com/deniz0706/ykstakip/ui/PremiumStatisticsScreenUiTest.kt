package com.deniz0706.ykstakip.ui

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.ui.statistics.PremiumStatisticsScreen
import com.deniz0706.ykstakip.ui.theme.YksTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PremiumStatisticsScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun recent_trend_filter_shows_only_selected_exam_type() {
        val exams = listOf(
            Exam(id = 1, title = "TYT Deneme", date = "2026-11-20", type = ExamType.TYT),
            Exam(id = 2, title = "AYT Deneme", date = "2026-11-21", type = ExamType.AYT)
        )

        composeRule.setContent {
            YksTheme {
                PremiumStatisticsScreen(
                    exams = exams,
                    obp = 88.2,
                    targetTyt = 115f,
                    targetAyt = 80f
                )
            }
        }

        composeRule.onNodeWithText("Son Denemeler").performScrollTo()
        composeRule.onNodeWithText("AYT").performClick()

        composeRule.onNodeWithText("AYT Deneme").assertExists()
        composeRule.onNodeWithText("TYT Deneme").assertDoesNotExist()
    }
}
