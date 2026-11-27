package com.deniz0706.ykstakip.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.deniz0706.ykstakip.data.ThemeManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _theme = MutableLiveData<String>(ThemeManager.currentTheme(application.applicationContext))
    val theme: MutableLiveData<String> = _theme

    fun setTheme(mode: String) {
        ThemeManager.setTheme(application.applicationContext, mode)
        _theme.value = mode
    }

    val dailyGoal: MutableLiveData<Int?> = MutableLiveData(null)
}
