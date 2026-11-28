package com.deniz0706.ykstakip.data.local

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver

/**
 * Process-wide SQLDelight database.
 *
 * A null AndroidSqliteDriver name creates an in-memory database. The app needs
 * one named on-disk database shared by every repository and migration step.
 */
object DatabaseHolder {
    private const val DATABASE_NAME = "yks_takip.db"

    @Volatile
    private var instance: YksDatabase? = null

    fun get(context: Context): YksDatabase =
        instance ?: synchronized(this) {
            instance ?: YksDatabase(
                AndroidSqliteDriver(
                    schema = YksDatabase.Schema,
                    context = context.applicationContext,
                    name = DATABASE_NAME
                )
            ).also { instance = it }
        }
}