package com.deniz0706.ykstakip.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.deniz0706.ykstakip.data.repository.SqlExamRepository
import com.deniz0706.ykstakip.domain.model.Exam
import kotlinx.coroutines.flow.first
import com.deniz0706.ykstakip.domain.repository.ExamRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class ExamViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExamRepository = SqlExamRepository(application.applicationContext)
    private val _exams = MutableLiveData<List<Exam>>(emptyList())
    val exams: MutableLiveData<List<Exam>> = _exams

    init {
        viewModelScope.launch {
            repository.getAllExams().collect { _exams.value = it }
        }
    }

    suspend fun saveExam(exam: Exam) {
        repository.saveExam(exam)
        _exams.value = repository.getAllExams().first()
    }

    fun deleteExam(id: Long) {
        viewModelScope.launch {
            repository.deleteExam(id)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.getAllExams().collect { _exams.value = it }
        }
    }
}
