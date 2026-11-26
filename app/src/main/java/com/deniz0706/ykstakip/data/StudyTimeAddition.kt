package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.StudyDay

/** Add real minutes without resetting previous questions or study metadata. */
internal object StudyTimeAddition {
    const val DAILY_LIMIT = 1440

    fun add(previous: StudyDay?, date: String, minutes: Int): StudyDay? {
        if (minutes !in 1..DAILY_LIMIT) return null
        val day = previous ?: StudyDay(date, 0, 0)
        require(day.date == date)
        if (day.studyMinutes !in 0..DAILY_LIMIT - minutes) return null
        return day.copy(studyMinutes = day.studyMinutes + minutes)
    }
}
