package com.deniz0706.ykstakip.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deniz0706.ykstakip.domain.repository.ExamRepository
import com.deniz0706.ykstakip.domain.repository.LearningRepository
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val exams: List<Exam> = emptyList(),
    val selectedType: ExamType = ExamType.TYT,
    val todayQuestions: Int = 0,
    val dailyGoal: Int? = null,
    val daysLeft: Long? = null,
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
    private val learningRepository: LearningRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var selectedType = ExamType.TYT

    init {
        viewModelScope.launch {
            val examsFlow = examRepository.getAllExams().stateIn(
                viewModelScope, /*start=*/ null, emptyList()
            )
            val outcomesFlow = learningRepository.getOutcomes().stateIn(
                viewModelScope, /*start=*/ null, emptyList()
            )
            examsFlow.collect { exams ->
                outcomesFlow.collect { outcomes ->
                    val filtered = exams.filter { it.type == selectedType }.sortedBy { it.date }
                    val latest = filtered.lastOrNull()?.totalNet
                    val previous = filtered.getOrNull(filtered.lastIndex - 1)?.totalNet
                    val change = if (latest != null && previous != null) latest - previous else null

                    _uiState.value = _uiState.value.copy(
                        exams = filtered,
                        selectedType = selectedType,
                        latestNet = latest,
                        netChange = change,
                        learningDoneCount = outcomes.count { it.completed },
                        learningTotalCount = outcomes.size,
                        loading = false
                    )
                }
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
