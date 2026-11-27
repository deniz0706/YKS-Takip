package com.deniz0706.ykstakip.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.flow
import androidx.lifecycle.viewModelScope
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.data.FocusSessionRepository
import com.deniz0706.ykstakip.data.LearningRepository
import com.deniz0706.ykstakip.data.StudyRepository
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.util.Fmt
import com.deniz0706.ykstakip.util.HomeInsights
import com.deniz0706.ykstakip.util.ReviewAdvisor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class HomeUiState(
    val exams: List<Exam> = emptyList(),
    val selectedType: ExamType = ExamType.TYT,
    val todayQuestions: Int = 0,
    val dailyGoal: Int? = null,
    val daysLeft: Long? = null,
    val hasFocusSession: Boolean = false,
    val learningAlertTitle: String? = null,
    val reviewAlertCount: Int = 0,
    val learningDoneCount: Int = 0,
    val learningTotalCount: Int = 0,
    val aiCoachAdvice: String = "",
    val aiCoachDate: String = "",
    val targetTyt: Float? = null,
    val targetAyt: Float? = null,
    val latestNet: Double? = null,
    val netChange: Double? = null,
    val loading: Boolean = false
)

class HomeViewModel(
    application: Application,
    private val examRepository: ExamRepository,
    private val studyRepository: StudyRepository,
    private val focusRepository: FocusSessionRepository,
    private val learningRepository: LearningRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var selectedType = ExamType.TYT

    init {
        viewModelScope.launch {
            combine(
                examRepository.getAllExams().stateIn(viewModelScope, /*start=*/ null, HomeUiState().exams),
                studyRepository.getAll().stateIn(viewModelScope, /*start=*/ null, emptyList()),
                focusRepository.snapshot().let { /* placeholder */ },
                learningRepository.getOutcomes().stateIn(viewModelScope, /*start=*/ null, emptyList())
            ) { exams, days, _, outcomes ->
                val filtered = exams.filter { it.type == selectedType }.sortedBy { it.date }
                val latest = filtered.lastOrNull()?.totalNet
                val previous = filtered.getOrNull(filtered.lastIndex - 1)?.totalNet
                val change = if (latest != null && previous != null) latest - previous else null
                val today = LocalDate.now().toString()
                val todayCount = days.firstOrNull { it.date == today }?.questionCount ?: 0

                _uiState.value = _uiState.value.copy(
                    exams = filtered,
                    selectedType = selectedType,
                    todayQuestions = todayCount,
                    latestNet = latest,
                    netChange = change,
                    learningDoneCount = outcomes.count { it.completed },
                    learningTotalCount = outcomes.size,
                    loading = false
                )
            }
        }
    }

    fun selectExamType(type: ExamType) {
        selectedType = type
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(loading = true)
    }
}
