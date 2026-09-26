package com.deniz0706.ykstakip

import android.app.Application
import com.deniz0706.ykstakip.data.ThemeManager

class YksApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.applySavedTheme(this)
    }
}
