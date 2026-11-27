package com.deniz0706.ykstakip.data.local

import android.content.Context
import com.squareup.sqldelight.android.SqlDriver
import com.squareup.sqldelight.db.SqlDatabase

/** Single database instance for the application. */
class YksDatabase private constructor(internal val database: SqlDatabase) {
    companion object {
        @Volatile private var instance: YksDatabase? = null

        fun get(context: Context): YksDatabase =
            instance ?: synchronized(this) {
                instance ?: YksDatabase(SqlDatabase(com.deniz0706.ykstakip.data.local.Schema, SqlDriver(context))).also { instance = it }
            }
    }
}
