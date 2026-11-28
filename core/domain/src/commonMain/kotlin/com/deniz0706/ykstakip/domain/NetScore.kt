package com.deniz0706.ykstakip.domain

import kotlin.math.roundToInt

/**
 * Platform-independent YKS net arithmetic.
 *
 * Keep this as the single source of truth for shared Android/iOS domain code:
 * one wrong answer deducts one quarter of a net.
 */
object NetScore {
    fun calculate(correct: Int, wrong: Int): Double =
        ((correct - wrong / 4.0) * 100.0).roundToInt() / 100.0
}
