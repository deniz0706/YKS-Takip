package com.deniz0706.ykstakip.data

/**
 * Prevents a failed legacy JSON read from being mistaken for an empty store.
 * The original SharedPreferences value is deliberately left untouched.
 */
internal class LegacyDataWriteGuard(private val dataLabel: String) {
    @Volatile
    private var blocked = false

    val isBlocked: Boolean
        get() = blocked

    fun markUnreadable() {
        blocked = true
    }

    fun requireWritable() {
        check(!blocked) {
            "$dataLabel verileri okunamadı. Veri kaybını önlemek için kayıt durduruldu. " +
                "Önce geçerli bir yedekten geri yükleyin."
        }
    }
}
