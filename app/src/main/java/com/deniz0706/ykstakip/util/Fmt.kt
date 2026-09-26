package com.deniz0706.ykstakip.util

import java.util.Locale
import kotlin.math.roundToInt

object Fmt {
    fun net(value: Double?): String {
        if (value == null) return "—"
        return String.format(Locale.US, "%.2f", value).replace('.', ',')
    }

    fun netWithSign(value: Double): String {
        val sign = if (value > 0) "+" else if (value < 0) "-" else "±"
        return sign + String.format(Locale.US, "%.2f", kotlin.math.abs(value)).replace('.', ',')
    }

    fun minutesPerQuestion(value: Double?): String {
        if (value == null) return "—"
        return String.format(Locale.US, "%.2f", value).replace('.', ',')
    }

    fun round2(v: Double): Double = (v * 100).roundToInt() / 100.0
}
