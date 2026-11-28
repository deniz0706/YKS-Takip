package com.deniz0706.ykstakip.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.ui.history.ExamEntryScreen
import com.deniz0706.ykstakip.ui.theme.YksTheme
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExamEntryScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun valid_tyt_exam_can_be_submitted() {
        val saved = AtomicReference<Exam?>(null)

        composeRule.setContent {
            YksTheme {
                ExamEntryScreen(
                    onBack = {},
                    onSave = { saved.set(it) }
                )
            }
        }

        composeRule.onNodeWithText("Deneme adı *").performTextInput("Deneme Kontrol")
        composeRule.onNodeWithText("Denemeyi Kaydet").performClick()

        composeRule.runOnIdle {
            val exam = saved.get()
            assertNotNull(exam)
            assertEquals("Deneme Kontrol", exam!!.title)
            assertEquals(ExamType.TYT, exam.type)
            assertEquals(4, exam.subjects.size)
            assertEquals(120, exam.totalQuestions)
        }
    }
}
