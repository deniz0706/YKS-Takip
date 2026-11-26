package com.deniz0706.ykstakip.domain

import kotlinx.serialization.Serializable

@Serializable
enum class ExamType(val label: String, val totalTimeMinutes: Int) {
    TYT("TYT", 165),
    AYT("AYT", 180)
}
