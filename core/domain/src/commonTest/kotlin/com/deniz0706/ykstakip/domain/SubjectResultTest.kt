package com.deniz0706.ykstakip.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubjectResultTest {
    @Test
    fun calculatesBlankAndNetWithoutPlatformDependencies() {
        val result = SubjectResult("Türkçe", 40, 35, 3, 30)
        assertEquals(2, result.blank)
        assertEquals(34.25, result.net)
    }

    @Test
    fun blankNeverBecomesNegativeForLegacyOverfilledData() {
        val result = SubjectResult("Türkçe", 40, 35, 8, 30)
        assertEquals(0, result.blank)
        assertEquals(33.0, result.net)
    }

    @Test
    fun calculatesMinutesPerQuestionAndKeepsMissingValueNull() {
        assertEquals(0.75, SubjectResult("Türkçe", 40, 35, 3, 30).minutesPerQuestion)
        assertNull(SubjectResult("Türkçe", 40, 35, 3, 0).minutesPerQuestion)
    }
}
