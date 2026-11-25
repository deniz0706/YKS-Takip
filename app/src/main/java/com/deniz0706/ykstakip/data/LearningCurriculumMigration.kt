package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.model.QuestionCard
import java.text.Normalizer
import java.util.Locale

/**
 * Moves saved learning progress onto the supplied curriculum.
 * Unmatched old entries remain in "Eski Kayıtlar"; question/photo links are retained.
 */
object LearningCurriculumMigration {
    data class Result(
        val outcomes: List<LearningOutcome>,
        val questions: List<QuestionCard>
    )

    private val subjectAliases = mapOf(
        "Din Kültürü" to "Din Kültürü ve Ahlak Bilgisi"
    )

    private val titleAliases = mapOf(
        "TYT|Türkçe|Paragraf" to "Paragrafta Anlam ve Yapı",
        "TYT|Türkçe|Sözcükte Yapı/Ekler" to "Sözcükte Yapı ve Ekler",
        "TYT|Türkçe|Fiiller" to "Fiiller (Anlam, Kip, Ek Fiil, Çatı)",
        "TYT|Türkçe|Anlatım Bozukluğu" to "Anlatım Bozuklukları",
        "TYT|Matematik|Oran Orantı" to "Oran – Orantı",
        "TYT|Matematik|Denklem Çözme" to "Birinci Dereceden Denklemler",
        "TYT|Matematik|Kümeler – Kartezyen Çarpım" to "Kümeler ve Kartezyen Çarpım",
        "TYT|Matematik|Veri – İstatistik" to "Veri ve İstatistik",
        "TYT|Matematik|Fonksiyonlar" to "Fonksiyonlar (Temel Kavramlar ve Grafikler)",
        "TYT|Matematik|Polinomlar" to "Polinomlar (Temel İşlemler)",
        "TYT|Matematik|2. Dereceden Denklemler" to "İkinci Dereceden Denklemler (Temel)",
        "TYT|Fizik|Dinamik" to "Newton'un Hareket Yasaları (Temel)",
        "TYT|Fizik|Basınç" to "Katı, Sıvı ve Gaz Basıncı",
        "TYT|Fizik|Elektrik" to "Elektrik Akımı ve Devreler",
        "TYT|Fizik|Manyetizma" to "Mıknatıslar ve Manyetizma",
        "TYT|Kimya|Kimyasal Hesaplamalar" to "Kimyasal Tepkimeler ve Hesaplamalar",
        "TYT|Kimya|Karışımlar" to "Karışımlar ve Ayırma Yöntemleri",
        "TYT|Kimya|Asit, Baz ve Tuz" to "Asitler, Bazlar ve Tuzlar",
        "AYT|Kimya|Karbon Kimyasına Giriş" to "Karbon Kimyasına Giriş ve Hibritleşme"
    )

    private fun normalize(value: String): String = Normalizer.normalize(
        value.lowercase(Locale.forLanguageTag("tr")),
        Normalizer.Form.NFKC
    ).replace(Regex("[^\\p{L}\\p{N}]"), "")

    private data class Source(
        val exam: String,
        val subject: String,
        val title: String
    )

    private fun sourceFromId(id: String): Source? {
        val parts = id.split('|')
        val source = when {
            parts.size >= 4 && parts[0] == "unit" ->
                Source(parts[1], parts[2], parts.drop(3).joinToString("|"))
            parts.size >= 5 && parts[0] == "expanded" ->
                Source(parts[1], parts[2], parts[3])
            parts.size >= 4 && parts[0] in setOf("TYT", "AYT") ->
                Source(parts[0], parts[1], parts[2])
            else -> null
        } ?: return null
        return source.copy(subject = subjectAliases[source.subject] ?: source.subject)
    }

    private fun sourceOf(record: LearningOutcome): Source? =
        sourceFromId(record.id) ?: if (record.subject != "Eski Kayıtlar") {
            val subject = subjectAliases[record.subject] ?: record.subject
            Source(record.examType, subject, record.title)
        } else null

    private fun targetFor(
        source: Source,
        defaults: Map<String, LearningOutcome>,
        byCourseAndTitle: Map<String, LearningOutcome>
    ): LearningOutcome? {
        val directId = "unit|${source.exam}|${source.subject}|${source.title}"
        val alias = titleAliases["${source.exam}|${source.subject}|${source.title}"]
        val title = alias ?: source.title
        return defaults[directId] ?: byCourseAndTitle[
            "${source.exam}|${source.subject}|${normalize(title)}"
        ]
    }

    fun convert(
        recorded: List<LearningOutcome>,
        linkedQuestions: List<QuestionCard>
    ): Result {
        val defaults = LearningKitapsecCatalog.starter.associateBy { it.id }
        val byCourseAndTitle = LearningKitapsecCatalog.starter.associateBy {
            "${it.examType}|${it.subject}|${normalize(it.title)}"
        }
        val saved = linkedMapOf<String, LearningOutcome>()
        val remappedIds = mutableMapOf<String, String>()

        recorded.forEach { record ->
            // User-created topics are not curriculum entries and must remain untouched.
            if (record.custom &&
                !record.id.startsWith("unit|") &&
                !record.id.startsWith("expanded|") &&
                !record.id.startsWith("TYT|") &&
                !record.id.startsWith("AYT|")
            ) {
                saved[record.id] = record
                return@forEach
            }

            val source = sourceOf(record)
            val target = defaults[record.id] ?: source?.let {
                targetFor(it, defaults, byCourseAndTitle)
            }

            if (target == null) {
                // Keep removed/ambiguous topics recoverable, including any linked questions.
                val archived = if (record.subject == "Eski Kayıtlar") record else record.copy(
                    subject = "Eski Kayıtlar",
                    topic = "Önceki Müfredat",
                    title = "${source?.subject ?: record.subject}: ${source?.title ?: record.title}",
                    custom = true
                )
                saved[record.id] = archived
                remappedIds[record.id] = record.id
                return@forEach
            }

            remappedIds[record.id] = target.id
            val previous = saved[target.id]
            val base = previous ?: target
            val dueDates = listOf(base.nextReviewDate, record.nextReviewDate)
                .filter { it.isNotBlank() }
            saved[target.id] = target.copy(
                completed = base.completed || record.completed,
                reviewRequested = base.reviewRequested || record.reviewRequested,
                wrongCount = base.wrongCount + record.wrongCount,
                acknowledgedWrongCount = base.acknowledgedWrongCount +
                    record.acknowledgedWrongCount,
                nextReviewDate = dueDates.minOrNull().orEmpty(),
                reviewLevel = if (previous == null) record.reviewLevel
                    else minOf(previous.reviewLevel, record.reviewLevel),
                lastReviewedDate = listOf(base.lastReviewedDate, record.lastReviewedDate)
                    .filter { it.isNotBlank() }.maxOrNull().orEmpty(),
                // The new syllabus intentionally has no deeper checklist layer.
                completedSubtopics = emptyList(),
                custom = false
            )
        }

        val questions = linkedQuestions.map { card ->
            val mapped = remappedIds[card.outcomeId] ?: run {
                val source = sourceFromId(card.outcomeId)
                val target = source?.let { targetFor(it, defaults, byCourseAndTitle) }
                if (target != null) {
                    target.id
                } else {
                    // A question may be the only saved reference to a removed topic.
                    // Create an archive row so its journal entry and photo links remain reachable.
                    if (source != null && card.outcomeId !in saved) {
                        saved[card.outcomeId] = LearningOutcome(
                            id = card.outcomeId,
                            examType = source.exam,
                            subject = "Eski Kayıtlar",
                            topic = "Önceki Müfredat",
                            title = "${source.subject}: ${source.title}",
                            custom = true
                        )
                    }
                    card.outcomeId
                }
            }
            card.copy(outcomeId = mapped)
        }
        return Result(saved.values.toList(), questions)
    }
}
