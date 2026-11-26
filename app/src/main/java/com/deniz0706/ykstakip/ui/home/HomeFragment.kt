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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.AiCoachCache
import com.deniz0706.ykstakip.data.AiContextBuilder
import com.deniz0706.ykstakip.data.AiSecretStore
import com.deniz0706.ykstakip.data.NvidiaAiService
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
        val learningTotalCount: Int = 0,
        val aiCoachAdvice: String = "",
        val aiCoachDate: String = ""
    )
    private val dashboardSummary = mutableStateOf(Summary())
    private var refreshJob: Job? = null
    private var knownExamIds: Set<Long>? = null
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
                        onOpenAiCoach = { (activity as? MainActivity)?.openAiCoach() },
                        onOpenLearning = { type, subject ->
                            (activity as? MainActivity)?.openLearning(type, subject)
                        },
                        learningAlertTitle = summary.learningAlertTitle,
                        learningAlertType = summary.learningAlertType,
                        learningAlertSubject = summary.learningAlertSubject,
                        reviewAlertCount = summary.reviewAlertCount,
                        learningDoneCount = summary.learningDoneCount,
                        learningTotalCount = summary.learningTotalCount,
                        aiCoachAdvice = summary.aiCoachAdvice,
                        aiCoachDate = summary.aiCoachDate,
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
        if (!isAdded) return
        refreshJob?.cancel()
        val appContext = requireContext().applicationContext
        refreshJob = viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val savedExams = repository.getAllExams()
                val newExamId = knownExamIds?.let { previous -> savedExams.firstOrNull { it.id !in previous }?.id }
                knownExamIds = savedExams.map { it.id }.toSet()
                val cachedAdvice = AiCoachCache.getAdvice(appContext)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val session = focusRepository.snapshot()
                val learningOutcomes = learningRepository.outcomes()
                val alerts = ReviewAdvisor.recommendations(
                    learningOutcomes, learningRepository.questions(), savedExams
                )
                val mainAlert = alerts.firstOrNull { it.urgent || it.due }
                    ?: alerts.firstOrNull()
                Triple(savedExams, Summary(
                    daysTyt = daysUntilExam(19),
                    daysAyt = daysUntilExam(20),
                    goalTyt = AppSettings.getTargetNet(appContext, ExamType.TYT),
                    goalAyt = AppSettings.getTargetNet(appContext, ExamType.AYT),
                    todayQuestions = studyRepository.get(today)?.questionCount ?: 0,
                    dailyGoal = AppSettings.getDailyGoal(appContext),
                    hasFocusSession = session.running || session.elapsedMs > 0L,
                    learningAlertTitle = mainAlert?.outcome?.let {
                        it.subject + " · " + it.title
                    },
                    learningAlertType = mainAlert?.outcome?.examType ?: "TYT",
                    learningAlertSubject = mainAlert?.outcome?.subject.orEmpty(),
                    reviewAlertCount = alerts.size,
                    learningDoneCount = learningOutcomes.count { it.completed },
                    learningTotalCount = learningOutcomes.size,
                    aiCoachAdvice = cachedAdvice?.first.orEmpty(),
                    aiCoachDate = cachedAdvice?.second.orEmpty()
                ), newExamId)
            }
            if (isAdded) {
                exams.value = result.first
                dashboardSummary.value = result.second
                result.third?.let(::analyzeNewExam)
            }
        }
    }

    private fun analyzeNewExam(examId: Long) {
        val appContext = requireContext().applicationContext
        if (!AiSecretStore.hasApiKey(appContext) || !AiCoachCache.claimDailySlot(appContext, examId)) return
        viewLifecycleOwner.lifecycleScope.launch {
            val advice = runCatching {
                val snapshot = withContext(Dispatchers.IO) { AiContextBuilder.build(appContext) }
                NvidiaAiService(appContext).chat(listOf(
                    NvidiaAiService.Message("system",
                        "Sen YKS-Takip uygulamasının gerçekçi ve veri odaklı YKS koçusun. Türkçe yanıt ver. Öğrencinin verilerinde olmayan bilgi uydurma. Yeni denemesini, önceki sonuçlarını ve konu ilerlemesini birlikte değerlendir. En fazla 3 somut öneri ver. Öğrenci özeti:\n$snapshot"),
                    NvidiaAiService.Message("user",
                        "Yeni bir deneme kaydettim (kayıt kimliği: $examId). Sonuçlarıma göre kısa bir değerlendirme ve öncelikli çalışma önerisi hazırla.")
                ), 520)
            }.getOrNull()
            if (!advice.isNullOrBlank()) {
                AiCoachCache.save(appContext, advice)
                if (isAdded) dashboardSummary.value = dashboardSummary.value.copy(
                    aiCoachAdvice = advice, aiCoachDate = LocalDate.now().toString()
                )
            }
        }
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
