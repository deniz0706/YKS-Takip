package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.domain.StudyTimeAddition as SharedStudyTimeAddition
import com.deniz0706.ykstakip.model.StudyDay

/** Android compatibility adapter; daily time arithmetic is shared in commonMain. */
internal object StudyTimeAddition {
    const val DAILY_LIMIT = SharedStudyTimeAddition.DAILY_LIMIT

    fun add(previous: StudyDay?, date: String, minutes: Int): StudyDay? =
        SharedStudyTimeAddition.add(previous, date, minutes)
}
