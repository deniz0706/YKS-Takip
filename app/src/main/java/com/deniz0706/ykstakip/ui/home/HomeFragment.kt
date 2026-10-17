package com.deniz0706.ykstakip.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.data.StudyRepository
import com.deniz0706.ykstakip.data.FocusSessionRepository
import com.deniz0706.ykstakip.data.LearningRepository
import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.util.ReviewAdvisor
import com.deniz0706.ykstakip.data.ThemeManager
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.theme.activeDarkTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var repository: ExamRepository
    private lateinit var studyRepository: StudyRepository
    private lateinit var focusRepository: FocusSessionRepository
    private lateinit var learningRepository: LearningRepository
    private val exams = mutableStateOf<List<Exam>>(emptyList())
    private data class Summary(
        val daysTyt: Long? = null,
        val daysAyt: Long? = null,
        val goalTyt: Float? = null,
        val goalAyt: Float? = null,
        val todayQuestions: Int = 0,
        val dailyGoal: Int? = null,
        val hasFocusSession: Boolean = false,
        val learningAlertTitle: String? = null,
        val learningAlertType: String = "TYT",
        val learningAlertSubject: String = "",
        val reviewAlertCount: Int = 0,
        val learningDoneCount: Int = 0,
        val learningTotalCount: Int = 0
    )
    private val dashboardSummary = mutableStateOf(Summary())
    private val refreshListener: () -> Unit = { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())
        studyRepository = StudyRepository(requireContext())
        focusRepository = FocusSessionRepository(requireContext())
        learningRepository = LearningRepository(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val dark = activeDarkTheme(context)
                YksTheme(darkTheme = dark) {
                    val summary = dashboardSummary.value
                    HomeScreenV2(
                        exams = exams.value,
                        onNewExam = { (activity as? MainActivity)?.openNewExam(it) },
                        onOpenStudy = { (activity as? MainActivity)?.openStudy() },
                        onOpenLearning = { type, subject ->
                            (activity as? MainActivity)?.openLearning(type, subject)
                        },
                        learningAlertTitle = summary.learningAlertTitle,
                        learningAlertType = summary.learningAlertType,
                        learningAlertSubject = summary.learningAlertSubject,
                        reviewAlertCount = summary.reviewAlertCount,
                        learningDoneCount = summary.learningDoneCount,
                        learningTotalCount = summary.learningTotalCount,
                        daysLeft = summary.daysTyt,
                        daysLeftAyt = summary.daysAyt,
                        targetTyt = summary.goalTyt,
                        targetAyt = summary.goalAyt,
                        todayQuestions = summary.todayQuestions,
                        dailyGoal = summary.dailyGoal,
                        hasFocusSession = summary.hasFocusSession
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        repository.addChangeListener(refreshListener)
        refresh()
    }

    override fun onPause() {
        repository.removeChangeListener(refreshListener)
        super.onPause()
    }

    private fun refresh() {
        exams.value = repository.getAllExams()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val session = focusRepository.snapshot()
        val learningOutcomes = learningRepository.outcomes()
        val alerts = ReviewAdvisor.recommendations(
            learningOutcomes, learningRepository.questions(), exams.value
        )
        val mainAlert = alerts.firstOrNull { it.urgent || it.due }
            ?: alerts.firstOrNull()
        dashboardSummary.value = Summary(
            daysTyt = daysUntilExam(19),
            daysAyt = daysUntilExam(20),
            goalTyt = AppSettings.getTargetNet(requireContext(), ExamType.TYT),
            goalAyt = AppSettings.getTargetNet(requireContext(), ExamType.AYT),
            todayQuestions = studyRepository.get(today)?.questionCount ?: 0,
            dailyGoal = AppSettings.getDailyGoal(requireContext()),
            hasFocusSession = session.running || session.elapsedMs > 0L,
            learningAlertTitle = mainAlert?.outcome?.let {
                it.subject + " · " + it.title
            },
            learningAlertType = mainAlert?.outcome?.examType ?: "TYT",
            learningAlertSubject = mainAlert?.outcome?.subject.orEmpty(),
            reviewAlertCount = alerts.size,
            learningDoneCount = learningOutcomes.count { it.completed },
            learningTotalCount = learningOutcomes.size
        )
    }

    private fun daysUntilExam(dayOfMonth: Int): Long {
        val exam = Calendar.getInstance().apply {
            set(2027, Calendar.JUNE, dayOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (exam.timeInMillis - today.timeInMillis) / 86_400_000L
    }
}
