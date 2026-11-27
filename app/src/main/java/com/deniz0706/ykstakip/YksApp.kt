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
        ThemeManager.applySavedTheme(this)

        // A failed legacy migration must never prevent the app from opening.
        // The migration flag is written only after all steps succeed, so a later
        // launch can safely retry the idempotent migration.
        try {
            LegacyDataMigrator.migrateIfNeeded(this)
        } catch (error: Exception) {
            Log.e("YksApp", "Legacy data migration failed; app startup will continue.", error)
        }
    }
}
