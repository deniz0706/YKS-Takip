package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.util.WeakTopicAnalyzer
import com.deniz0706.ykstakip.util.PerformanceIntelligence
import java.time.LocalDate

/** Builds a compact, read-only snapshot. It never triggers network requests. */
object AiContextBuilder {
    fun build(context: Context): String {
        val app = context.applicationContext
        val exams = ExamRepository.getInstance(app).getAllExams().sortedBy { it.date }
        val studyDays = StudyRepository(app).getAll().sortedBy { it.date }
        val outcomes = LearningRepository(app).outcomes()
        val plan = StudyPlanRepository(app).getAll()
        val memorySnapshot = AiMemoryRepository(app).promptSnapshot()
        val today = LocalDate.now()
        val recent = exams.takeLast(5)
        val weak = WeakTopicAnalyzer.findWeakSpots(exams, sampleSize = 8, top = 5)
        val lines = mutableListOf<String>()
        lines += "Tarih: $today"
        lines += "Hedefler: TYT ${AppSettings.getTargetNet(app, ExamType.TYT) ?: "belirtilmedi"}, AYT ${AppSettings.getTargetNet(app, ExamType.AYT) ?: "belirtilmedi"}, OBP ${AppSettings.getObp(app) ?: "belirtilmedi"}"
        lines += "Deneme sayısı: ${exams.size}"
        recent.forEach { exam ->
            val breakdown = exam.subjects.joinToString(", ") { "${it.subject}: ${"%.2f".format(java.util.Locale.US, it.net)} net (${it.correct}D/${it.wrong}Y)" }
            lines += "${exam.date} | ${exam.type} | ${exam.title}: ${exam.totalNet} net | $breakdown"
        }
        if (weak.isNotEmpty()) lines += "Zayıf konu etiketleri: " + weak.joinToString("; ") { "${it.subject} / ${it.topic} (${it.count}/${it.outOf} deneme)" }
        lines += PerformanceIntelligence.toCoachSummary(PerformanceIntelligence.analyze(exams, ExamType.TYT), "TYT")
        AytField.values().forEach { field ->
            if (exams.any { it.type == ExamType.AYT && it.aytField == field }) {
                lines += PerformanceIntelligence.toCoachSummary(
                    PerformanceIntelligence.analyze(exams, ExamType.AYT, field), "AYT ${field.name}"
                )
            }
        }
        val completed = outcomes.count { it.completed }
        lines += "Konu ilerlemesi: $completed / ${outcomes.size} kazanım tamamlandı; ${outcomes.count { it.reviewRequested }} tekrar isteniyor."
        val pending = outcomes.filter { !it.completed }.take(12)
        if (pending.isNotEmpty()) lines += "Tamamlanmamış kazanımlar: " + pending.joinToString("; ") { "${it.examType} ${it.subject}: ${it.title}" }
        val overdue = outcomes.filter { !it.completed && it.nextReviewDate.isNotBlank() && it.nextReviewDate <= today.toString() }
        if (overdue.isNotEmpty()) lines += "Öncelikli tekrar kuyruğu (${overdue.size}): " + overdue.take(15).joinToString("; ") { "${it.subject} / ${it.title} (yanlış: ${it.wrongCount}, tekrar: ${it.nextReviewDate})" }
        val reviewDue = outcomes.filter { it.nextReviewDate.isNotBlank() && it.nextReviewDate <= today.toString() }.take(10)
        if (reviewDue.isNotEmpty()) lines += "Tekrar tarihi gelenler: " + reviewDue.joinToString("; ") { "${it.subject}: ${it.title}" }
        val last7 = studyDays.filter { runCatching { LocalDate.parse(it.date).isAfter(today.minusDays(7)) }.getOrDefault(false) }
        lines += "Son 7 gün: ${last7.sumOf { it.studyMinutes }} dakika, ${last7.sumOf { it.questionCount }} soru (${last7.size} kayıt günü)."
        val previous7 = studyDays.filter { runCatching { val date = LocalDate.parse(it.date); date.isAfter(today.minusDays(14)) && !date.isAfter(today.minusDays(7)) }.getOrDefault(false) }
        if (last7.isNotEmpty() || previous7.isNotEmpty()) lines += "Çalışma ritmi karşılaştırması: önceki 7 gün ${previous7.sumOf { it.studyMinutes }} dk / ${previous7.sumOf { it.questionCount }} soru; son 7 gün ${last7.sumOf { it.studyMinutes }} dk / ${last7.sumOf { it.questionCount }} soru."
        val todayPlan = plan.filter { it.dayOfWeek == today.dayOfWeek.value % 7 || it.dayOfWeek == today.dayOfWeek.value }
        if (todayPlan.isNotEmpty()) lines += "Haftalık program (bugün): " + todayPlan.take(6).joinToString("; ") { "${it.startTime}-${it.endTime} ${it.subject} ${it.topic}" }
        val currentSnapshot = lines.take(20).joinToString("\n").take(4200)
        return buildString {
            if (memorySnapshot.isNotBlank()) {
                append("KALICI ÖĞRENCİ HAFIZASI (kullanıcının özellikle kaydettiği bilgiler):\n")
                append(memorySnapshot)
                append("\n\n")
            }
            append("GÜNCEL UYGULAMA VERİLERİ:\n")
            append(currentSnapshot)
        }.take(6800)
    }
}