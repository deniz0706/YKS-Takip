package com.deniz0706.ykstakip.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.deniz0706.ykstakip.data.repository.SqlExamRepository
import com.deniz0706.ykstakip.model.Exam

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SqlExamRepository = SqlExamRepository.getInstance(application.applicationContext)
    private val _exams = MutableLiveData<List<Exam>>(emptyList())
    val exams: MutableLiveData<List<Exam>> = _exams

    init {
        _exams.value = repository.getAllExams()
    }
}
