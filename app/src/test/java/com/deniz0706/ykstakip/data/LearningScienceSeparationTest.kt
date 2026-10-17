package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.model.QuestionCard
import com.deniz0706.ykstakip.model.QuestionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningScienceSeparationTest {
    @Test fun tytScienceIsNotRepeatedAsAytUnit() {
        val units = LearningKitapsecCatalog.starter
        for ((subject, removed) in LearningScienceSeparation.obsoleteAytTitles) {
            val ayt = units.filter { it.examType == "AYT" && it.subject == subject }
                .map { it.title }.toSet()
            assertTrue("Wrong AYT $subject units: ${ayt.intersect(removed)}",
                ayt.intersect(removed).isEmpty())
        }
        assertEquals(8, units.count { it.examType == "AYT" && it.subject == "Fizik" })
        assertEquals(10, units.count { it.examType == "AYT" && it.subject == "Kimya" })
        assertEquals(6, units.count { it.examType == "AYT" && it.subject == "Biyoloji" })
    }

    @Test fun questionsForRemovedUnitKeepOriginalPhotosAndProgress() {
        val prior = LearningOutcome(
            "unit|AYT|Fizik|Optik", "AYT", "Fizik", "Fizik", "Optik",
            completed = true, wrongCount = 2)
        val card = QuestionCard("old-ayt-photo", prior.id, "Çözülemedi", "",
            QuestionStatus.COZULEMEDI, "2026-10-17", "2026-10-18",
            photos = listOf("original.png"))
        val converted = LearningScienceSeparation.convert(listOf(prior), listOf(card))
        val old = converted.single()
        assertEquals(prior.id, old.id)
        assertEquals("Eski Kayıtlar", old.subject)
        assertTrue(old.completed)
        assertEquals(2, old.wrongCount)
        assertEquals(prior.id, card.outcomeId)
        assertEquals(listOf("original.png"), card.photos)
    }

    @Test fun orphanedQuestionCreatesRecoverableArchiveUnit() {
        val oldId = "unit|AYT|Kimya|Karışımlar"
        val card = QuestionCard("orphan", oldId, "Foto", "",
            QuestionStatus.COZULEMEDI, "2026-10-17", "2026-10-18",
            photos = listOf("p.png"))
        val converted = LearningScienceSeparation.convert(emptyList(), listOf(card))
        assertEquals(oldId, converted.single().id)
        assertEquals("Eski Kayıtlar", converted.single().subject)
    }

    @Test fun regularAytTopicsRemainUntouched() {
        val unit = LearningOutcome("unit|AYT|Fizik|Çembersel Hareket",
            "AYT", "Fizik", "Fizik", "Çembersel Hareket", completed = true)
        val converted = LearningScienceSeparation.convert(listOf(unit), emptyList())
        assertEquals(unit, converted.single())
        assertFalse(LearningScienceSeparation.shouldArchive(unit))
    }
}
