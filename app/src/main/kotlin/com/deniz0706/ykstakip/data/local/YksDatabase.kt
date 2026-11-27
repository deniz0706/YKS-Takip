package com.deniz0706.ykstakip.data.local

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

/** Single database instance for the application (wraps raw SqlDatabase for legacy access). */
class DatabaseHolder private constructor(internal val driver: SqlDriver) {
    companion object {
        @Volatile private var instance: DatabaseHolder? = null

        fun get(context: Context): DatabaseHolder =
            instance ?: synchronized(this) {
                instance ?: DatabaseHolder(AndroidSqliteDriver(YksDatabase.Schema, context)).also { instance = it }
            }
    }
}
