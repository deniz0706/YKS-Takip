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
import com.deniz0706.ykstakip.data.ThemeManager
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.ui.theme.YksTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var repository: ExamRepository
    private lateinit var studyRepository: StudyRepository
    private val exams = mutableStateOf<List<Exam>>(emptyList())
    private val dashboardRevision = mutableStateOf(0)
    private val refreshListener: () -> Unit = { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())
        studyRepository = StudyRepository(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val dark = when (ThemeManager.currentTheme(context)) {
                    "dark" -> true
                    "light" -> false
                    else -> isSystemInDarkTheme()
                }
                YksTheme(darkTheme = dark) {
                    val revision = dashboardRevision.value
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    HomeScreenV2(
                        exams = exams.value,
                        onNewExam = { (activity as? MainActivity)?.openNewExam(it) },
                        onOpenStudy = { (activity as? MainActivity)?.openStudy() },
                        daysLeft = daysUntilExam(19),
                        daysLeftAyt = daysUntilExam(20),
                        targetTyt = AppSettings.getTargetNet(context, ExamType.TYT),
                        targetAyt = AppSettings.getTargetNet(context, ExamType.AYT),
                        todayQuestions = studyRepository.get(today)?.questionCount ?: 0,
                        dailyGoal = AppSettings.getDailyGoal(context)
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
        dashboardRevision.value += 1
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
