package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome

/**
 * Editable starter checklist, not an official MEB/ÖSYM acquisition database.
 * Stable IDs allow catalog growth without resetting the user's progress.
 */
object LearningSeed {
    private data class Group(val exam: String, val subject: String, val topic: String,
                             val outcomes: List<String>)
    private fun group(exam: String, subject: String, topic: String, vararg items: String) =
        Group(exam, subject, topic, items.toList())

    val starter: List<LearningOutcome> by lazy { LearningSeedExpanded.starter }
    val starterIds: Set<String> by lazy { starter.map { it.id }.toSet() }

    /** Old catalog is kept for lossless ID migration, not shown in the UI. */
    val legacyStarter: List<LearningOutcome> by lazy {
        val groups = listOf(
            group("TYT","Türkçe","Paragraf","Ana düşünceyi belirler","Yardımcı düşünceyi bulur","Paragrafta yapı ve akışı yorumlar"),
            group("TYT","Türkçe","Dil bilgisi","Sözcük türlerini ayırt eder","Cümlenin ögelerini belirler","Yazım ve noktalama kurallarını uygular"),
            group("TYT","Matematik","Temel Kavramlar","Sayı kümelerini kullanır","Bölme ve bölünebilme kurallarını uygular","Üslü ve köklü ifadeleri düzenler"),
            group("TYT","Matematik","Denklemler","Birinci derece denklemleri çözer","Mutlak değerli ifadeleri yorumlar","Oran ve orantı kurar"),
            group("TYT","Matematik","Problemler","Yüzde ve kar-zarar problemlerini çözer","Hareket problemlerini modeller","İşçi-havuz problemlerini çözer"),
            group("TYT","Matematik","Geometri","Üçgende açı ve uzunluk ilişkilerini kullanır","Üçgende alan hesaplar","Çember ve daire problemlerini çözer"),
            group("TYT","Sosyal Bilimler","Tarih","Osmanlı yenileşme hareketlerini açıklar","Millî Mücadele kronolojisini yorumlar","Atatürk ilkelerini ayırt eder"),
            group("TYT","Sosyal Bilimler","Coğrafya","Harita ve koordinat yorumlar","İklim ve yer şekillerini ilişkilendirir","Türkiye'nin nüfus ve yerleşmesini değerlendirir"),
            group("TYT","Sosyal Bilimler","Felsefe","Felsefenin temel özelliklerini açıklar","Bilgi ve ahlak felsefesi problemlerini ayırt eder"),
            group("TYT","Fen Bilimleri","Fizik","Hareket grafiklerini yorumlar","Kuvvet ve denge hesapları yapar","Optik ve dalga olaylarını açıklar"),
            group("TYT","Fen Bilimleri","Kimya","Atom ve periyodik sistemi yorumlar","Kimyasal türler arası etkileşimleri açıklar","Karışım ve derişim hesapları yapar"),
            group("TYT","Fen Bilimleri","Biyoloji","Hücre organellerinin görevlerini açıklar","Madde geçişlerini karşılaştırır","Canlıların sınıflandırılmasını yorumlar"),
            group("AYT","Matematik","Fonksiyonlar","Fonksiyonun tanım ve görüntü kümesini bulur","Bileşke ve ters fonksiyon kurar"),
            group("AYT","Matematik","Polinomlar","Polinom bölme ve kalan bulur","Polinom denklemlerini çözer"),
            group("AYT","Matematik","Trigonometri","Birim çember ilişkilerini kullanır","Trigonometrik denklemleri çözer"),
            group("AYT","Matematik","Logaritma","Logaritmanın kurallarını uygular","Üstel-logaritmik denklemleri çözer"),
            group("AYT","Matematik","Limit - Türev - İntegral","Limit ve süreklilik hesaplar","Türevin geometrik yorumunu yapar","Belirli integral ve alan hesaplar"),
            group("AYT","Fizik","Kuvvet ve Hareket","Newton yasalarını uygular","Bağıl hareket problemlerini çözer","Atış hareketlerini yorumlar"),
            group("AYT","Fizik","Enerji ve Momentum","İş-enerji bağıntılarını uygular","İtme-momentum korunumunu kullanır"),
            group("AYT","Fizik","Elektrik ve Manyetizma","Elektrik alan ve potansiyeli ilişkilendirir","Manyetik kuvveti yorumlar","İndüksiyon olaylarını açıklar"),
            group("AYT","Fizik","Modern Fizik","Fotoelektrik olayı yorumlar","Atom modellerini karşılaştırır"),
            group("AYT","Kimya","Kimyasal Tepkimeler","Tepkime hızı hesaplar","Kimyasal dengeyi yorumlar"),
            group("AYT","Kimya","Asit Baz ve Çözünürlük","Asit-baz pH hesaplar","Çözünürlük dengesini yorumlar"),
            group("AYT","Kimya","Elektrokimya","Redoks tepkimelerini denkleştirir","Elektroliz ve pil hesapları yapar"),
            group("AYT","Kimya","Organik Kimya","Organik bileşikleri adlandırır","Fonksiyonel grupları ayırt eder"),
            group("AYT","Biyoloji","Hücre Bölünmeleri","Mitoz ve mayozu karşılaştırır","Kalıtım problemlerini çözer"),
            group("AYT","Biyoloji","İnsan Fizyolojisi","Sinir ve endokrin sistemi açıklar","Dolaşım ve solunumu ilişkilendirir","Sindirim ve boşaltım sistemlerini yorumlar"),
            group("AYT","Biyoloji","Fotosentez ve Solunum","Fotosentez basamaklarını açıklar","Hücresel solunum evrelerini karşılaştırır"),
            group("AYT","Biyoloji","Bitki Biyolojisi","Bitkilerde taşımayı açıklar","Bitki hormonlarını yorumlar"),
            group("AYT","Edebiyat","Edebiyat Dönemleri","Tanzimat edebiyatını ayırt eder","Servetifünun dönemini yorumlar","Cumhuriyet dönemi yazarlarını ilişkilendirir"),
            group("AYT","Tarih-1","Osmanlı ve Cumhuriyet","Osmanlı siyasi gelişmelerini yorumlar","Millî Mücadele olaylarını ilişkilendirir"),
            group("AYT","Coğrafya-1","Türkiye Coğrafyası","Türkiye'nin ekonomik faaliyetlerini yorumlar","Nüfus ve yerleşme verilerini değerlendirir"),
            group("AYT","Tarih-2","Dünya Tarihi","20. yüzyıl gelişmelerini değerlendirir"),
            group("AYT","Coğrafya-2","Küresel Sistem","Ülkeler arası ekonomik ilişkileri yorumlar"),
            group("AYT","Felsefe Grubu","Psikoloji ve Sosyoloji","Psikoloji yaklaşımlarını ayırt eder","Toplum ve kurum ilişkilerini yorumlar"),
            group("AYT","Din Kültürü / Ek Felsefe","İnanç ve Değer","Din ve ahlakla ilgili kavramları yorumlar")
        )
        val original = groups.flatMap { g ->
            g.outcomes.mapIndexed { index, title ->
                LearningOutcome(
                    id = g.exam + "|" + g.subject + "|" + g.topic + "|" + index,
                    examType = g.exam, subject = g.subject, topic = g.topic,
                    title = title
                )
            }
        }
        // Original IDs stay unchanged. Expanded identifiers use an isolated prefix
        // so historical ticks, reviews, wrong answers and photos remain linked.
        original + LearningSeedExpanded.legacyStarter
    }

    private fun legacyTarget(outcome: LearningOutcome): String {
        val subject = when (outcome.subject) {
            "Sosyal Bilimler" -> outcome.topic
            "Fen Bilimleri" -> outcome.topic
            else -> if (outcome.examType == "TYT" && outcome.subject == "Matematik"
                && outcome.topic == "Geometri") "Geometri" else outcome.subject
        }
        val exam = outcome.examType
        val topic = when {
            outcome.subject == "Sosyal Bilimler" && subject == "Tarih" ->
                when {
                    outcome.title.contains("Mücadele") -> "Millî Mücadele"
                    outcome.title.contains("Atatürk") -> "Atatürk İlkeleri ve İnkılaplar"
                    else -> "Osmanlı Devleti"
                }
            outcome.subject == "Sosyal Bilimler" && subject == "Coğrafya" ->
                when {
                    outcome.title.contains("Harita") -> "Harita Bilgisi"
                    outcome.title.contains("İklim") -> "İklim Bilgisi"
                    else -> "Nüfus ve Göç"
                }
            outcome.subject == "Sosyal Bilimler" -> "Felsefeye Giriş"
            outcome.subject == "Fen Bilimleri" && subject == "Fizik" ->
                when {
                    outcome.title.contains("Optik") || outcome.title.contains("dalga") -> "Optik"
                    else -> "Hareket ve Kuvvet"
                }
            outcome.subject == "Fen Bilimleri" && subject == "Kimya" ->
                if (outcome.title.contains("Karışım")) "Karışımlar" else "Atom ve Periyodik Sistem"
            outcome.subject == "Fen Bilimleri" && subject == "Biyoloji" ->
                if (outcome.title.contains("sınıflandırma", true)) "Sınıflandırma" else "Hücre"
            exam == "TYT" && outcome.subject == "Matematik" && outcome.topic == "Geometri" ->
                if (outcome.title.contains("Çember")) "Çember ve Daire" else "Üçgenler"
            exam == "TYT" && outcome.subject == "Türkçe" && outcome.topic == "Paragraf" -> "Paragraf"
            exam == "TYT" && outcome.subject == "Türkçe" && outcome.topic == "Dil bilgisi" -> "Sözcükte Yapı"
            exam == "TYT" && outcome.subject == "Matematik" && outcome.topic == "Temel Kavramlar" -> "Sayı Kümeleri"
            exam == "TYT" && outcome.subject == "Matematik" && outcome.topic == "Problemler" -> "Problemler"
            exam == "AYT" && outcome.subject == "Matematik" && outcome.topic == "Limit - Türev - İntegral" ->
                when {
                    outcome.title.contains("İntegral") -> "İntegral"
                    outcome.title.contains("Türev") -> "Türev"
                    else -> "Limit ve Süreklilik"
                }
            else -> LearningSeedExpanded.normalizedTopic(exam, subject, outcome.topic)
        }
        val course = starter.filter { it.examType == exam && it.subject == subject }
        return course.find { it.title == topic }?.id
            ?: course.find { it.title.equals(outcome.topic, true) }?.id
            ?: course.firstOrNull()?.id
            ?: outcome.id
    }

    val legacyToTopicId: Map<String, String> by lazy {
        legacyStarter.associate { it.id to legacyTarget(it) }
    }

    /**
     * Units renamed or consolidated after cross-checking the MEB OGM unit
     * tables. IDs are mapped both for existing devices and imported ZIP files.
     * The old combined Gazlar/Çözeltiler record cannot be split accurately;
     * it is kept under Gazlar, while the solutions unit starts unmarked.
     */
    private val renamedUnitIds: Map<String, String> = mapOf(
        "unit|AYT|Kimya|Gazlar ve Çözeltiler" to "unit|AYT|Kimya|Gazlar",
        "unit|AYT|Kimya|Organik Kimya" to "unit|AYT|Kimya|Organik Bileşikler",
        "unit|AYT|Biyoloji|Komünite Ekolojisi" to
            "unit|AYT|Biyoloji|Komünite ve Popülasyon Ekolojisi",
        "unit|AYT|Biyoloji|Popülasyon Ekolojisi" to
            "unit|AYT|Biyoloji|Komünite ve Popülasyon Ekolojisi"
    )

    fun topicIdFor(id: String): String = renamedUnitIds[id] ?: legacyToTopicId[id] ?: id

    /** Converts saved old checkmarks and wrong counts into the new topic rows. */
    fun mergeLegacyProgress(recorded: List<LearningOutcome>): List<LearningOutcome> {
        val current = linkedMapOf<String, LearningOutcome>()
        recorded.filter { topicIdFor(it.id) == it.id }.forEach { current[it.id] = it }
        val groups = recorded.filter { topicIdFor(it.id) != it.id }.groupBy {
            topicIdFor(it.id)
        }
        for ((id, old) in groups) {
            val base = current[id] ?: starter.firstOrNull { it.id == id } ?: continue
            val dueDates = (old.map { it.nextReviewDate } + base.nextReviewDate)
                .filter { it.isNotBlank() }
            current[id] = base.copy(
                completed = base.completed || old.any { it.completed },
                reviewRequested = base.reviewRequested || old.any { it.reviewRequested },
                wrongCount = base.wrongCount + old.sumOf { it.wrongCount },
                acknowledgedWrongCount = base.acknowledgedWrongCount +
                    old.sumOf { it.acknowledgedWrongCount },
                nextReviewDate = dueDates.minOrNull().orEmpty(),
                reviewLevel = if (old.any { it.reviewLevel == 0 }) 0
                    else minOf(base.reviewLevel, old.minOf { it.reviewLevel }),
                lastReviewedDate = (old.map { it.lastReviewedDate } +
                    base.lastReviewedDate).filter { it.isNotBlank() }.maxOrNull().orEmpty()
            )
        }
        return current.values.toList()
    }
}
