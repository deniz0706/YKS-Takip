package com.deniz0706.ykstakip.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.repository.ExamRepository
import com.deniz0706.ykstakip.domain.repository.LearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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
            try {
                combine(
                    examRepository.getAllExams(),
                    learningRepository.getOutcomes()
                ) { exams, outcomes ->
                    exams to outcomes
                }.collect { (exams, outcomes) ->
                    val filtered = exams
                        .filter { it.type == selectedType }
                        .sortedBy { it.date }
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
            } catch (error: Exception) {
                // A repository failure should leave the dashboard usable rather
                // than escaping viewModelScope and terminating the process.
                Log.e("HomeViewModel", "Dashboard data could not be loaded.", error)
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    fun selectExamType(type: ExamType) {
        selectedType = type
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(loading = true)
    }
}
