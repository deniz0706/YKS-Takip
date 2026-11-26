package com.deniz0706.ykstakip

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.deniz0706.ykstakip.data.ThemeManager

class YksApp : Application() {
    /** Requests must outlive individual screens; the OS may still stop the process. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        ThemeManager.applySavedTheme(this)
    }
}
