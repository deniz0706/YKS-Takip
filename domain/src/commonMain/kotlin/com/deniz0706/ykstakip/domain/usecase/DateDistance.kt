package com.deniz0706.ykstakip.domain.usecase

/**
 * Platform-independent ISO date distance helper for commonMain.
 * Returns the absolute day distance; invalid dates return Long.MAX_VALUE.
 */
internal object DateDistance {
    fun daysBetween(first: String, second: String): Long {
        val firstDay = toEpochDay(first) ?: return Long.MAX_VALUE
        val secondDay = toEpochDay(second) ?: return Long.MAX_VALUE
        return kotlin.math.abs(firstDay - secondDay)
    }

    private fun toEpochDay(value: String): Long? {
        val parts = value.split('-')
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null
        if (year < 1 || month !in 1..12) return null

        val leap = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
        val monthLengths = intArrayOf(
            31, if (leap) 29 else 28, 31, 30, 31, 30,
            31, 31, 30, 31, 30, 31
        )
        if (day !in 1..monthLengths[month - 1]) return null

        var adjustedYear = year.toLong()
        if (month <= 2) adjustedYear -= 1
        val era = adjustedYear / 400
        val yearOfEra = adjustedYear - era * 400
        val adjustedMonth = month + if (month > 2) -3 else 9
        val dayOfYear = (153L * adjustedMonth + 2) / 5 + day - 1
        val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
        return era * 146097 + dayOfEra - 719468
    }
}
