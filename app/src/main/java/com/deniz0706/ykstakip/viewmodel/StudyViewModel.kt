package com.deniz0706.ykstakip.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.deniz0706.ykstakip.data.repository.SqlStudyRepository
import com.deniz0706.ykstakip.domain.model.StudyDay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SqlStudyRepository(application.applicationContext)
    private val _days = MutableLiveData<List<StudyDay>>(emptyList())
    val days: MutableLiveData<List<StudyDay>> = _days

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _days.value = repository.getAll()
        }
    }
}
