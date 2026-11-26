package com.deniz0706.ykstakip.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class NetScoreTest {
    @Test
    fun calculatesQuarterNetPenalty() {
        assertEquals(34.25, NetScore.calculate(35, 3))
        assertEquals(9.75, NetScore.calculate(11, 5))
        assertEquals(37.75, NetScore.calculate(38, 1))
    }

    @Test
    fun roundsToTwoDecimalPlaces() {
        assertEquals(0.5, NetScore.calculate(1, 2))
        assertEquals(-0.25, NetScore.calculate(0, 1))
    }

    @Test
    fun preservesNegativeNetWhenWrongAnswersExceedCorrectAnswers() {
        assertEquals(-1.0, NetScore.calculate(0, 4))
    }
}
