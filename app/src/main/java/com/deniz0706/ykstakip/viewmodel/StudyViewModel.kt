package com.deniz0706.ykstakip.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.deniz0706.ykstakip.data.StudyRepository
import com.deniz0706.ykstakip.domain.model.StudyDay

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyRepository(application.applicationContext)
    private val _days = MutableLiveData<List<StudyDay>>(emptyList())
    val days: MutableLiveData<List<StudyDay>> = _days

    init {
        refresh()
    }

    fun refresh() {
        _days.value = repository.getAll()
    }
}
