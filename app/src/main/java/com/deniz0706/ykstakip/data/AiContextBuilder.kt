package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.domain.model.AytField as DomainAytField
import com.deniz0706.ykstakip.domain.model.Exam as DomainExam
import com.deniz0706.ykstakip.domain.model.ExamType as DomainExamType
import com.deniz0706.ykstakip.domain.model.SubjectResult
import com.deniz0706.ykstakip.model.Exam as AppExam
import com.deniz0706.ykstakip.model.AytField as AppAytField
import com.deniz0706.ykstakip.model.ExamType as AppExamType
import com.deniz0706.ykstakip.util.WeakTopicAnalyzer
import com.deniz0706.ykstakip.util.PerformanceIntelligence
import java.time.LocalDate

/** Builds a compact, read-only snapshot. It never triggers network requests. */
object AiContextBuilder {

    private fun toDomainExam(exam: AppExam): DomainExam {
        val domainType = when (exam.type) {
            AppExamType.TYT -> DomainExamType.TYT
            AppExamType.AYT -> DomainExamType.AYT
        }
        val domainField = exam.aytField?.let {
            when (it) {
                AppAytField.SAYISAL -> DomainAytField.SAYISAL
                AppAytField.ESIT_AGIRLIK -> DomainAytField.ESIT_AGIRLIK
                AppAytField.SOZEL -> DomainAytField.SOZEL
                else -> null
            }
        }
        return DomainExam(
            id = exam.id,
            title = exam.title,
            date = exam.date,
            type = domainType,
            aytField = domainField,
            subjects = exam.subjects.map { sr ->
                SubjectResult(
                    subject = sr.subject,
                    correct = sr.correct,
                    wrong = sr.wrong,
                    blank = sr.blank,
                    timeMinutes = sr.timeMinutes,
                    weakTopics = emptyList()
                )
            },
            notes = exam.notes,
            publisher = exam.publisher
        )
    }

    fun build(context: Context): String {
        val app = context.applicationContext
        val exams = ExamRepository.getInstance(app).getAllExams().sortedBy { it.date }
        val studyDays = StudyRepository(app).getAll().sortedBy { it.date }
        val outcomes = LearningRepository.getInstance(app).outcomes()
        val plan = StudyPlanRepository(app).getAll()
        val memorySnapshot = AiMemoryRepository(app).promptSnapshot()
        val today = LocalDate.now()
        val recent = exams.takeLast(5)
        val weak = WeakTopicAnalyzer.findWeakSpots(exams, sampleSize = 8, top = 5)
        val lines = mutableListOf<String>()
        lines += "Tarih: $today"
        lines += "Hedefler: TYT ${AppSettings.getTargetNet(app, AppExamType.TYT) ?: "belirtilmedi"}, AYT ${AppSettings.getTargetNet(app, AppExamType.AYT) ?: "belirtilmedi"}, OBP ${AppSettings.getObp(app) ?: "belirtilmedi"}"
        lines += "Deneme sayısı: ${exams.size}"
        recent.forEach { exam ->
            val breakdown = exam.subjects.joinToString(", ") { "${it.subject}: ${"%.2f".format(java.util.Locale.US, it.net)} net (${it.correct}D/${it.wrong}Y)" }
            lines += "${exam.date} | ${exam.type} | ${exam.title}: ${exam.totalNet} net | $breakdown"
        }
        if (weak.isNotEmpty()) lines += "Zayıf konu etiketleri: " + weak.joinToString("; ") { "${it.subject} / ${it.topic} (${it.count}/${it.outOf} deneme)" }
        val domainExams = exams.map { toDomainExam(it) }
        lines += PerformanceIntelligence.toCoachSummary(PerformanceIntelligence.analyze(domainExams, DomainExamType.TYT), "TYT")
        DomainAytField.values().forEach { field ->
            if (exams.any { it.type == AppExamType.AYT && it.aytField == AppAytField.valueOf(field.name) }) {
                lines += PerformanceIntelligence.toCoachSummary(
                    PerformanceIntelligence.analyze(domainExams, DomainExamType.AYT, field), "AYT ${field.name}"
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