package com.deniz0706.ykstakip.database

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/** Creates the app-private SQLDelight database without touching legacy SharedPreferences. */
fun createAndroidYksDatabase(context: Context): YksDatabase =
    YksDatabase(
        AndroidSqliteDriver(
            schema = YksDatabase.Schema,
            context = context.applicationContext,
            name = "yks_takip_v4.db"
        )
    )
