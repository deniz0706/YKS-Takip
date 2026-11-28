package com.deniz0706.ykstakip.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyDataWriteGuardTest {
    @Test
    fun readableLegacyStoreAllowsWrites() {
        val guard = LegacyDataWriteGuard("Sınav")
        assertFalse(guard.isBlocked)
        guard.requireWritable()
    }

    @Test
    fun unreadableLegacyStoreBlocksWrites() {
        val guard = LegacyDataWriteGuard("Sınav")
        guard.markUnreadable()

        assertTrue(guard.isBlocked)
        val error = assertThrows(IllegalStateException::class.java) {
            guard.requireWritable()
        }
        assertTrue(error.message.orEmpty().contains("Veri kaybını önlemek"))
    }

    @Test
    fun repeatedUnreadableReadsKeepWritesBlocked() {
        val guard = LegacyDataWriteGuard("Çalışma geçmişi")
        guard.markUnreadable()
        guard.markUnreadable()

        assertThrows(IllegalStateException::class.java) {
            guard.requireWritable()
        }
    }
}
