package com.deniz0706.ykstakip

import android.app.Application
import android.util.Log
import com.deniz0706.ykstakip.data.LegacyDataMigrator
import com.deniz0706.ykstakip.data.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class YksApp : Application() {
    /** Requests must outlive individual screens; the OS may still stop the process. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        // Appearance is non-critical: a bad preference or delegate failure must
        // not prevent the launcher Activity from starting with the system theme.
        try {
            ThemeManager.applySavedTheme(this)
        } catch (error: Exception) {
            Log.e("YksApp", "Saved theme could not be applied; using system appearance.", error)
        }

        // Migration is also non-critical for rendering the first screen. The
        // migration marker is committed only after every step succeeds, allowing
        // a later launch to retry without silently marking partial data as done.
        try {
            LegacyDataMigrator.migrateIfNeeded(this)
        } catch (error: Exception) {
            Log.e("YksApp", "Legacy data migration failed; app startup will continue.", error)
        }
    }
}
