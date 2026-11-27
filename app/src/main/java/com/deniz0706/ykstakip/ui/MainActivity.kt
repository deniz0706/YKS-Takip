package com.deniz0706.ykstakip.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.deniz0706.ykstakip.data.repository.SqlExamRepository
import com.deniz0706.ykstakip.data.repository.SqlLearningRepository
import com.deniz0706.ykstakip.data.repository.SqlStudyRepository
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.repository.ExamRepository
import com.deniz0706.ykstakip.domain.repository.LearningRepository
import com.deniz0706.ykstakip.ui.ai.NewAiCoachScreen
import com.deniz0706.ykstakip.ui.ai.CoachLine
import com.deniz0706.ykstakip.ui.history.NewExamsScreen
import com.deniz0706.ykstakip.ui.home.NewHomeScreen
import com.deniz0706.ykstakip.ui.navigation.Screen
import com.deniz0706.ykstakip.ui.navigation.YksNavigation
import com.deniz0706.ykstakip.ui.settings.NewSettingsScreen
import com.deniz0706.ykstakip.ui.statistics.NewStatisticsScreen
import com.deniz0706.ykstakip.ui.study.NewStudyScreen
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.domain.model.Exam

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YksTheme {
                YksApp()
            }
        }
    }
}

@Composable
fun YksApp() {
    val screen by remember { mutableStateOf<Screen>(Screen.Home) }

    val examRepository: ExamRepository by remember {
        SqlExamRepository(context.applicationContext)
    }
    val studyRepository: StudyRepository by remember {
        SqlStudyRepository(context.applicationContext)
    }
    val learningRepository: LearningRepository by remember {
        SqlLearningRepository(context.applicationContext)
    }

    YksNavigation(
        currentScreen = screen,
        onScreenChange = { screen = it },
        content = { currentScreen ->
            when (currentScreen) {
                Screen.Home -> NewHomeScreen(
                    exams = emptyList(),
                    onNewExam = { type -> screen = Screen.Exams },
                    onOpenStudy = { screen = Screen.Study },
                    onOpenAiCoach = { screen = Screen.AiCoach },
                    onOpenLearning = { type, subject -> screen = Screen.Study },
                    aiCoachAdvice = "",
                    aiCoachDate = ""
                )
                Screen.Exams -> NewExamsScreen(
                    exams = emptyList(),
                    onExamClick = { examId -> /* navigate to detail */ },
                    onNewExam = { /* open new exam */ }
                )
                Screen.Study -> NewStudyScreen(
                    days = emptyList(),
                    plan = emptyList(),
                    goal = null,
                    exams = emptyList(),
                    learningOutcomes = emptyList(),
                    learningQuestions = emptyList(),
                    learningField = AytField.SAYISAL,
                    onLearningFieldChange = {},
                    onLearningComplete = { _, _ -> },
                    onLearningSubtopicComplete = { _, _, _ -> },
                    onLearningWrong = {},
                    onLearningRequestReview = {},
                    onLearningReviewed = {},
                    onLearningPostpone = {},
                    onLearningCreate = { _, _, _, _ -> },
                    onLearningDelete = {},
                    onQuestionCreate = { _, _, _, _ -> "" },
                    onQuestionDelete = {},
                    onQuestionReview = { _, _ -> },
                    onQuestionUnsolved = {},
                    onQuestionCapture = {},
                    onQuestionGallery = {},
                    onQuestionPhotoRemove = { _, _ -> },
                    onRequestReviewNotifications = {},
                    onEditToday = {},
                    onFocusSaved = {},
                    onEditDay = {},
                    onPickDay = {},
                    onAddPlan = {},
                    onAddSuggestedPlan = { _, _, _ -> },
                    onEditPlan = {},
                    onTogglePlan = {},
                    onOpenLearningFromPlan = {},
                    onDeletePlan = {}
                )
                Screen.AiCoach -> NewAiCoachScreen(
                    lines = listOf(CoachLine("assistant", "Merhaba! Size nasıl yardımcı olabilir?")),
                    draft = "",
                    onDraftChange = {},
                    onSend = {},
                    sending = false,
                    hasApiKey = false,
                    onApiKeyMissing = { screen = Screen.Settings }
                )
                Screen.Settings -> NewSettingsScreen(
                    selectedTheme = "light",
                    onThemeChange = {},
                    tyt = "",
                    ayt = "",
                    obp = "",
                    daily = "",
                    onSave = { _, _, _, _ -> null },
                    aiBaseUrl = "",
                    aiModel = "",
                    hasAiKey = false,
                    onSaveAi = { _, _, _ -> null },
                    onTestAi = {},
                    onExport = {},
                    onImport = {},
                    onImportStudy = {},
                    onExportStudy = {},
                    onUndoStudyImport = {},
                    learningZipBusy = false,
                    onExportLearningZip = {},
                    onImportLearningZip = {},
                    fullBackupBusy = false,
                    onExportFullBackup = {},
                    onImportFullBackup = {}
                )
            }
        }
    )
}
