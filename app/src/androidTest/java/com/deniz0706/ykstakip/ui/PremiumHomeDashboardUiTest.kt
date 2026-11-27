package com.deniz0706.ykstakip.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.ui.home.PremiumHomeDashboard
import com.deniz0706.ykstakip.ui.theme.YksTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PremiumHomeDashboardUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun home_dashboard_renders_without_crashing() {
        composeRule.setContent {
            YksTheme {
                PremiumHomeDashboard(
                    exams = emptyList(),
                    onNewExam = {},
                    onOpenStudy = {},
                    onOpenAiCoach = {},
                    onOpenLearning = { _, _ -> },
                    aiCoachAdvice = "",
                    aiCoachDate = "",
                    reviewAlertCount = 0,
                    learningDoneCount = 0,
                    learningTotalCount = 0,
                    hasFocusSession = false,
                    daysLeft = null,
                    todayQuestions = 0,
                    dailyGoal = null,
                    targetTyt = null,
                    targetAyt = null,
                    daysLeftAyt = null
                )
            }
        }

        composeRule.onNodeWithText("Bugün").assertExists()
        composeRule.onNodeWithText("AI Koç").assertExists()
    }

    @Test
    fun home_dashboard_shows_exam_count() {
        val exams = listOf(
            Exam(id = 1, title = "TYT Deneme", date = "2024-01-01", type = ExamType.TYT, subjects = emptyList())
        )
        composeRule.setContent {
            YksTheme {
                PremiumHomeDashboard(
                    exams = exams,
                    onNewExam = {},
                    onOpenStudy = {},
                    onOpenAiCoach = {},
                    onOpenLearning = { _, _ -> },
                    aiCoachAdvice = "",
                    aiCoachDate = "",
                    reviewAlertCount = 0,
                    learningDoneCount = 0,
                    learningTotalCount = 0,
                    hasFocusSession = false,
                    daysLeft = null,
                    todayQuestions = 5,
                    dailyGoal = 10,
                    targetTyt = 90f,
                    targetAyt = null,
                    daysLeftAyt = null
                )
            }
        }

        composeRule.onNodeWithText("5 soru").assertExists()
    }

    @Test
    fun home_dashboard_navigates_to_study_on_action() {
        var studyOpened = false
        composeRule.setContent {
            YksTheme {
                PremiumHomeDashboard(
                    exams = emptyList(),
                    onNewExam = {},
                    onOpenStudy = { studyOpened = true },
                    onOpenAiCoach = {},
                    onOpenLearning = { _, _ -> },
                    aiCoachAdvice = "",
                    aiCoachDate = "",
                    reviewAlertCount = 0,
                    learningDoneCount = 0,
                    learningTotalCount = 0,
                    hasFocusSession = false,
                    daysLeft = null,
                    todayQuestions = 0,
                    dailyGoal = null,
                    targetTyt = null,
                    targetAyt = null,
                    daysLeftAyt = null
                )
            }
        }

        composeRule.onNodeWithText("Çalışmaya Başla").performClick()
        assert(studyOpened)
    }
}
