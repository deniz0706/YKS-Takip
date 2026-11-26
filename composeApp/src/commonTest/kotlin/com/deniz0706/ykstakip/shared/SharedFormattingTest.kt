package com.deniz0706.ykstakip.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedFormattingTest {
    @Test
    fun netFormattingUsesTurkishDecimalSeparator() {
        assertEquals("37,25", formatNet(37.25))
        assertEquals("0", formatNet(0.0))
        assertEquals("-1,5", formatNet(-1.5))
    }
}
