package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.Exam

object WeakTopicAnalyzer {

    data class WeakSpot(val subject: String, val topic: String, val count: Int, val outOf: Int)

    /**
     * Son [sampleSize] denemedeki "zayıf konu" etiketlerini ders+konu bazında sayar,
     * en sık tekrar edenleri döndürür. outOf = kaç denemede o ders hiç girildi (yüzde hesaplamak için).
     */
    fun findWeakSpots(exams: List<Exam>, sampleSize: Int = 5, top: Int = 3): List<WeakSpot> {
        val recent = exams.sortedBy { it.date }.takeLast(sampleSize)
        if (recent.isEmpty()) return emptyList()

        val counts = mutableMapOf<Pair<String, String>, Int>()
        val subjectAppearances = mutableMapOf<String, Int>()

        for (exam in recent) {
            for (subject in exam.subjects) {
                subjectAppearances[subject.subject] = (subjectAppearances[subject.subject] ?: 0) + 1
                for (rawTopic in subject.weakTopics) {
                    val topic = rawTopic.trim()
                    if (topic.isEmpty()) continue
                    val key = subject.subject to topic
                    counts[key] = (counts[key] ?: 0) + 1
                }
            }
        }

        return counts.entries
            .sortedByDescending { it.value }
            .take(top)
            .map { (key, count) ->
                val (subject, topic) = key
                WeakSpot(subject, topic, count, subjectAppearances[subject] ?: recent.size)
            }
    }
}
