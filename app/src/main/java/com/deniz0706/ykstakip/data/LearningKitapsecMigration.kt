package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.model.QuestionCard
import java.text.Normalizer
import java.util.Locale

/**
 * Offline migration to Kitapsec's hierarchy.
 *
 * Uncertain matches are ARCHIVED under 'Eski Kayıtlar' with their original ID,
 * never silently attached to an unrelated topic. Question IDs/photo paths survive.
 */
object LearningKitapsecMigration {
    data class Result(
        val outcomes: List<LearningOutcome>,
        val questions: List<QuestionCard>
    )

    private data class Destination(val id: String, val child: String? = null)
    private val units by lazy { LearningKitapsecCatalog.starter }
    private val unitsById by lazy { units.associateBy { it.id } }
    private val previousById by lazy {
        (LearningSeedExpanded.starter + LearningSeed.legacyStarter).associateBy { it.id }
    }

    private fun norm(s: String): String = Normalizer.normalize(
        s.lowercase(Locale.forLanguageTag("tr")), Normalizer.Form.NFKC
    ).replace(Regex("[^\\p{L}\\p{N}]"), "")

    // Map only unambiguous older titles. Any uncertain mapping stays in archive.
    private val aliases = mapOf(
        "TYT|Türkçe|Noktalama" to "Noktalama İşaretleri",
        "TYT|Türkçe|Sözcükte Yapı" to "Sözcükte Yapı/Ekler",
        "TYT|Türkçe|Zamir Zarf Edat" to "Sözcük Türleri",
        "TYT|Türkçe|Fiil ve Kip" to "Fiiller",
        "TYT|Türkçe|Fiilimsiler" to "Fiiller",
        "TYT|Türkçe|Ek Fiil" to "Fiiller",
        "TYT|Türkçe|Fiilde Çatı" to "Fiiller",
        "TYT|Türkçe|Anlatım Bozuklukları" to "Anlatım Bozukluğu",
        "TYT|Türkçe|İsim Sıfat Tamlamaları" to "Sözcük Türleri",
        "TYT|Matematik|Sayı Kümeleri" to "Temel Kavramlar",
        "TYT|Matematik|Bölme Bölünebilme" to "Bölme ve Bölünebilme",
        "TYT|Matematik|EBOB EKOK" to "EBOB – EKOK",
        "TYT|Matematik|Oran Orantı" to "Oran Orantı",
        "TYT|Matematik|Denklemler" to "Denklem Çözme",
        "TYT|Matematik|Fonksiyon Temelleri" to "Fonksiyonlar",
        "TYT|Matematik|Sayma ve Olasılık" to "Permütasyon ve Kombinasyon",
        "TYT|Matematik|İstatistik" to "Veri – İstatistik",
        "TYT|Geometri|Dörtgenler ve Çokgenler" to "Özel Dörtgenler",
        "TYT|Fizik|Fiziğin Doğası" to "Fizik Bilimine Giriş",
        "TYT|Fizik|İş Güç Enerji" to "İş, Güç ve Enerji",
        "TYT|Fizik|Isı Sıcaklık" to "Isı, Sıcaklık ve Genleşme",
        "TYT|Fizik|Basınç ve Kaldırma" to "Basınç",
        "TYT|Fizik|Elektrostatik" to "Elektrik",
        "TYT|Kimya|Kimyasal Türler" to "Kimyasal Türler Arası Etkileşimler",
        "TYT|Kimya|Mol Hesapları" to "Kimyasal Hesaplamalar",
        "TYT|Kimya|Asit Baz Tuz" to "Asit, Baz ve Tuz",
        "TYT|Biyoloji|Hücre" to "Hücre ve Organelleri",
        "TYT|Biyoloji|Sınıflandırma" to "Canlıların Sınıflandırılması",
        "TYT|Biyoloji|Ekoloji" to "Ekosistem Ekolojisi",
        "TYT|Tarih|Tarih Bilimi" to "Tarih ve Zaman",
        "TYT|Tarih|Millî Mücadele" to "Milli Mücadele",
        "TYT|Coğrafya|Dünya Hareketleri" to "Dünya’nın Şekli ve Hareketleri",
        "TYT|Coğrafya|İklim Bilgisi" to "İklimler",
        "TYT|Coğrafya|Yer Şekilleri" to "İç Kuvvetler / Dış Kuvvetler",
        "TYT|Coğrafya|Su ve Toprak" to "Su – Toprak ve Bitkiler",
        "TYT|Coğrafya|Nüfus ve Göç" to "Nüfus",
        "TYT|Coğrafya|Bölgeler ve Afetler" to "Bölgeler",
        "TYT|Felsefe|Felsefeye Giriş" to "Felsefe’nin Konusu",
        "TYT|Din Kültürü|İnanç" to "Bilgi ve İnanç",
        "TYT|Din Kültürü|İbadet" to "İslam ve İbadet",
        "TYT|Din Kültürü|Hz. Muhammed" to "Hz. Muhammed (S.A.V.)",
        "TYT|Din Kültürü|Din ve Hayat" to "Din, Kültür ve Medeniyet",
        "AYT|Matematik|İkinci Dereceden Denklemler ve Parabol" to "İkinci Dereceden Denklem ve Fonksiyonlar",
        "AYT|Matematik|Sayma ve Binom" to "Sayma",
        "AYT|Matematik|Limit ve Süreklilik" to "Türev",
        "AYT|Matematik|Eşitsizlik Sistemleri" to "Denklem ve Eşitsizlik Sistemleri",
        "AYT|Matematik|Analitik Geometri" to "Analitik Geometri",
        "AYT|Geometri|Dönüşüm Geometrisi" to "Dönüşümler",
        "AYT|Geometri|Çemberin Analitiği" to "Dönüşümler",
        "AYT|Fizik|Dalga Mekaniği" to "Dalga Mekaniği",
        "AYT|Fizik|Modern Fizik" to "Modern Fizik",
        "AYT|Kimya|Organik Kimya" to "Organik Bileşikler",
        "AYT|Edebiyat|Söz Sanatları" to "Edebi Sanatlar",
        "AYT|Edebiyat|İslamiyet Öncesi Edebiyat" to
            "İslamiyet Öncesi Türk Edebiyatı ve Geçiş Dönemi",
        "AYT|Edebiyat|Geçiş Dönemi" to
            "İslamiyet Öncesi Türk Edebiyatı ve Geçiş Dönemi",
        "AYT|Edebiyat|Tanzimat" to "Tanzimat Edebiyatı",
        "AYT|Edebiyat|Servetifünun Fecriati" to
            "Servet-i Fünun ve Fecr-i Âti Edebiyatı",
        "AYT|Edebiyat|Millî Edebiyat" to "Milli Edebiyat",
        "AYT|Edebiyat|Edebî Akımlar" to "Batı Edebiyat Akımları",
        "AYT|Biyoloji|Komünite Ekolojisi" to "Komünite ve Popülasyon Ekolojisi",
        "AYT|Biyoloji|Enerji Dönüşümleri" to "Canlılarda Enerji Dönüşümleri",
        "AYT|Biyoloji|Evrim ve Adaptasyon" to "Canlılar ve Çevre",
        "AYT|Coğrafya-1|Nüfus Yerleşme" to "Beşeri Sistemler",
        "AYT|Coğrafya-2|Beşerî Sistemler" to "Beşeri Sistemler",
        "AYT|Coğrafya-2|Küresel Sistem" to "Küresel Ortam: Bölgeler ve Ülkeler"
    )

    private val aliasesToChild = mapOf(
        "TYT|Fizik|Elektrostatik" to "Elektrik Yükleri",
        "AYT|Matematik|Limit ve Süreklilik" to "Limit ve Süreklilik",
        "TYT|Türkçe|Ek Fiil" to "Ek Fiil",
        "TYT|Türkçe|Fiilde Çatı" to "Fiilde Çatı",
        "TYT|Türkçe|Fiilimsiler" to "Fiilimsi"
    )

    private fun destination(record: LearningOutcome): Destination? {
        if (record.custom) return null
        if (record.id in unitsById) return Destination(record.id)

        val subject = when (record.subject) {
            "Fen Bilimleri", "Sosyal Bilimler" -> record.topic
            else -> record.subject
        }
        val exam = record.examType
        val exact = units.filter { it.examType == exam && it.subject == subject }
            .singleOrNull { norm(it.title) == norm(record.title) }
        if (exact != null) return Destination(exact.id)

        val key = "$exam|$subject|${record.title}"
        val name = aliases[key] ?: return null
        val id = "unit|$exam|$subject|$name"
        if (id !in unitsById) return null
        val child = aliasesToChild[key]?.takeIf {
            it in LearningKitapsecCatalog.forOutcome(unitsById.getValue(id))
        }
        return Destination(id, child)
    }

    private fun archive(source: LearningOutcome): LearningOutcome = source.copy(
        subject = "Eski Kayıtlar",
        topic = "Önceki Konu Listesi",
        title = "${source.subject}: ${source.title}",
        custom = true
    )

    fun convert(recorded: List<LearningOutcome>, linked: List<QuestionCard>): Result {
        val saved = linkedMapOf<String, LearningOutcome>()
        val conversions = linkedMapOf<String, String>()
        val previous = (previousById + recorded.associateBy { it.id })

        // Process each persisted progress entry only once. Retain unknown entries.
        for (old in recorded) {
            if (old.custom) {
                saved[old.id] = old
                continue
            }
            val dest = destination(old)
            if (dest == null) {
                saved[old.id] = archive(old)
                continue
            }
            conversions[old.id] = dest.id
            val reference = unitsById.getValue(dest.id)
            val base = saved[dest.id] ?: reference
            val available = LearningKitapsecCatalog.forOutcome(reference)
            val oldSubtopics = old.completedSubtopics.filter { it in available }
            val selected = (base.completedSubtopics + oldSubtopics +
                if (old.completed) {
                    if (dest.child != null) listOf(dest.child)
                    else available
                } else emptyList()
            ).distinct()
            val updated = base.copy(
                completedSubtopics = selected,
                completed = if (available.isNotEmpty()) selected.size == available.size
                    else base.completed || old.completed,
                wrongCount = base.wrongCount + old.wrongCount,
                acknowledgedWrongCount = base.acknowledgedWrongCount + old.acknowledgedWrongCount,
                reviewRequested = base.reviewRequested || old.reviewRequested,
                nextReviewDate = (listOf(base.nextReviewDate, old.nextReviewDate))
                    .filter { it.isNotBlank() }.minOrNull().orEmpty(),
                reviewLevel = if (base == reference) old.reviewLevel
                    else minOf(base.reviewLevel, old.reviewLevel),
                lastReviewedDate = listOf(base.lastReviewedDate, old.lastReviewedDate)
                    .filter { it.isNotBlank() }.maxOrNull().orEmpty()
            )
            saved[dest.id] = updated
        }

        val convertedCards = linked.map { card ->
            val target = conversions[card.outcomeId] ?: if (card.outcomeId in unitsById)
                card.outcomeId else {
                val old = previous[card.outcomeId]
                val dest = old?.let { destination(it) }
                if (dest != null) dest.id else {
                    // Archive a question whose prior unit is no longer in the
                    // new curriculum; NEVER drop the image links or question.
                    if (card.outcomeId !in saved) {
                        val guessed = old ?: LearningOutcome(
                            id = card.outcomeId,
                            examType = card.outcomeId.split('|').getOrNull(1)
                                ?.takeIf { it in setOf("TYT", "AYT") } ?: "TYT",
                            subject = "Eski Konular",
                            topic = "Önceki Konu Listesi",
                            title = "Eski konu (${card.outcomeId.take(36)})"
                        )
                        saved[card.outcomeId] = archive(guessed)
                    }
                    card.outcomeId
                }
            }
            card.copy(outcomeId = target)
        }
        return Result(saved.values.toList(), convertedCards)
    }
}
