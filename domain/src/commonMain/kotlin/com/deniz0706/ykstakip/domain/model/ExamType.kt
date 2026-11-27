package com.deniz0706.ykstakip.domain.model

/** Protected: YKS exam types and their official durations. */
enum class ExamType(val label: String, val totalTimeMinutes: Int) {
    TYT("TYT", 165),
    AYT("AYT", 180)
}
