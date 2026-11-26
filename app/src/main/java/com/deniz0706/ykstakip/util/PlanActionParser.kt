package com.deniz0706.ykstakip.util

import org.json.JSONObject

/** Parses a narrowly-scoped, non-executable study-plan proposal from AI text. */
object PlanActionParser {
    private const val OPEN = "[[YKS_PLAN_ACTION]]"
    private const val CLOSE = "[[/YKS_PLAN_ACTION]]"

    data class Draft(
        val dayOfWeek: Int,
        val startTime: String,
        val endTime: String,
        val subject: String,
        val topic: String
    )

    fun visibleText(response: String): String {
        val start = response.indexOf(OPEN)
        if (start < 0) return response.trim()
        val end = response.indexOf(CLOSE, start + OPEN.length)
        if (end < 0) return response.trim()
        return (response.substring(0, start) + response.substring(end + CLOSE.length)).trim()
    }

    fun parse(response: String): Draft? {
        val start = response.indexOf(OPEN)
        if (start < 0) return null
        val end = response.indexOf(CLOSE, start + OPEN.length)
        if (end < 0) return null
        val raw = response.substring(start + OPEN.length, end).trim()
        return runCatching {
            val json = JSONObject(raw)
            if (json.optString("type") != "add_plan") return null
            val day = json.optInt("dayOfWeek", -1)
            val startTime = json.optString("startTime")
            val endTime = json.optString("endTime")
            val subject = json.optString("subject").trim()
            val topic = json.optString("topic").trim()
            if (day !in 1..7 || !validTime(startTime) || !validTime(endTime)) return null
            if (toMinutes(endTime) <= toMinutes(startTime)) return null
            if (subject.length !in 2..40 || topic.length > 100) return null
            if (subject.any { it.isISOControl() } || topic.any { it.isISOControl() }) return null
            Draft(day, startTime, endTime, subject, topic)
        }.getOrNull()
    }

    private fun validTime(value: String): Boolean =
        Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$").matches(value)

    private fun toMinutes(value: String): Int =
        value.substring(0, 2).toInt() * 60 + value.substring(3, 5).toInt()
}
