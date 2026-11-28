package com.deniz0706.ykstakip.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.ui.history.PremiumExamsScreen
import com.deniz0706.ykstakip.ui.theme.YksTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PremiumExamsScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun exam_history_type_filter_limits_visible_results() {
        val exams = listOf(
            Exam(id = 1, title = "TYT Deneme", date = "2026-11-20", type = ExamType.TYT),
            Exam(id = 2, title = "AYT Deneme", date = "2026-11-21", type = ExamType.AYT)
        )

        composeRule.setContent {
            YksTheme {
                PremiumExamsScreen(
                    exams = exams,
                    onExamClick = {},
                    onNewExam = {},
                    colorScheme = MaterialTheme.colorScheme
                )
            }
        }

        composeRule.onNodeWithText("AYT").performClick()
        composeRule.onNodeWithText("AYT Deneme").assertExists()
        composeRule.onNodeWithText("TYT Deneme").assertDoesNotExist()
    }
}
