package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.model.QuestionCard

/** AYT lists in the commercial outline include introductory grades 9-10 topics.
 * Do not mislabel old AYT progress as TYT progress; archive it in place instead.
 * Kept separate from the original Kitapsec migration to run on updated installs.
 */
object LearningScienceSeparation {
    val obsoleteAytTitles: Map<String, Set<String>> = mapOf(
        "Fizik" to setOf(
            "Fizik Bilimine Giriş", "Madde ve Özellikleri", "Enerji",
            "Isı ve Sıcaklık", "Basınç ve Kaldırma Kuvveti", "Dalgalar", "Optik"
        ),
        "Kimya" to setOf(
            "Kimya Bilimi", "Atom ve Periyodik Sistem",
            "Kimyasal Türler Arası Etkileşimler", "Maddenin Halleri",
            "Karışımlar", "Asitler, Bazlar ve Tuzlar", "Kimya Her Yerde"
        ),
        "Biyoloji" to setOf(
            "Yaşam Bilimi ve Biyoloji", "Canlılar Dünyası",
            "Güncel Çevre Sorunları", "Üreme",
            "Kalıtımın Genel İlkeleri", "Dünyamız"
        )
    )

    fun shouldArchive(unit: LearningOutcome): Boolean =
        unit.examType == "AYT" && unit.id.startsWith("unit|AYT|") &&
            unit.title in obsoleteAytTitles[unit.subject].orEmpty()

    fun archive(unit: LearningOutcome): LearningOutcome = unit.copy(
        subject = "Eski Kayıtlar",
        topic = "Önceki AYT Fen Konuları",
        title = "${unit.subject} · ${unit.title} (eski AYT kaydı)",
        custom = true
    )

    fun convert(
        saved: List<LearningOutcome>,
        linked: List<QuestionCard>
    ): List<LearningOutcome> {
        val activeIds = LearningKitapsecCatalog.starter.mapTo(hashSetOf()) { it.id }
        val older = saved.associateBy { it.id }.toMutableMap()
        val referenced = linked.mapTo(hashSetOf()) { it.outcomeId }
        val oldIds = (obsoleteAytTitles.flatMap { (subject, titles) ->
            titles.map { "unit|AYT|$subject|$it" }
        }).toSet()
        // Also restore an archive unit when a question references a previously
        // untouched default unit that will no longer be seeded.
        for (id in oldIds) {
            if (id in activeIds) continue
            if (id !in older && id !in referenced) continue
            val original = older[id] ?: run {
                val pieces = id.split("|", limit = 4)
                LearningOutcome(id = id, examType = "AYT",
                    subject = pieces[2], topic = pieces[2], title = pieces[3])
            }
            // A previous migration may already have archived this ID.
            older[id] = if (original.subject == "Eski Kayıtlar") original
                else archive(original)
        }
        return older.values.toList()
    }
}
