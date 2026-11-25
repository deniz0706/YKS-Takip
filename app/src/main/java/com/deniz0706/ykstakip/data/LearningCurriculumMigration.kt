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

    private fun sourceOf(record: LearningOutcome): Source? {
        val parts = record.id.split('|')
        val source = when {
            parts.size >= 4 && parts[0] == "unit" ->
                Source(parts[1], parts[2], parts.drop(3).joinToString("|"))
            parts.size >= 5 && parts[0] == "expanded" ->
                Source(parts[1], parts[2], parts[3])
            parts.size >= 4 && parts[0] in setOf("TYT", "AYT") ->
                Source(parts[0], parts[1], parts[2])
            record.subject != "Eski Kayıtlar" ->
                Source(record.examType, record.subject, record.title)
            else -> null
        } ?: return null

        val subject = subjectAliases[source.subject] ?: source.subject
        return source.copy(subject = subject)
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
            val direct = defaults[record.id]
            val aliasedTitle = source?.let {
                titleAliases["${it.exam}|${it.subject}|${it.title}"]
            }
            val target = direct ?: source?.let {
                val title = aliasedTitle ?: it.title
                byCourseAndTitle["${it.exam}|${it.subject}|${normalize(title)}"]
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
            val previous = saved[target.id] ?: target
            val dueDates = listOf(previous.nextReviewDate, record.nextReviewDate)
                .filter { it.isNotBlank() }
            saved[target.id] = target.copy(
                completed = previous.completed || record.completed,
                reviewRequested = previous.reviewRequested || record.reviewRequested,
                wrongCount = previous.wrongCount + record.wrongCount,
                acknowledgedWrongCount = previous.acknowledgedWrongCount +
                    record.acknowledgedWrongCount,
                nextReviewDate = dueDates.minOrNull().orEmpty(),
                reviewLevel = minOf(previous.reviewLevel, record.reviewLevel),
                lastReviewedDate = listOf(previous.lastReviewedDate, record.lastReviewedDate)
                    .filter { it.isNotBlank() }.maxOrNull().orEmpty(),
                // The new syllabus intentionally has no deeper checklist layer.
                completedSubtopics = emptyList(),
                custom = false
            )
        }

        val questions = linkedQuestions.map { card ->
            card.copy(outcomeId = remappedIds[card.outcomeId] ?: card.outcomeId)
        }
        return Result(saved.values.toList(), questions)
    }
}
