package com.deniz0706.ykstakip.database

import app.cash.sqldelight.driver.native.NativeSqliteDriver

/** Creates the iOS app-private SQLDelight database; no legacy data is imported here. */
fun createIosYksDatabase(): YksDatabase =
    YksDatabase(
        NativeSqliteDriver(
            schema = YksDatabase.Schema,
            name = "yks_takip_v4.db"
        )
    )
