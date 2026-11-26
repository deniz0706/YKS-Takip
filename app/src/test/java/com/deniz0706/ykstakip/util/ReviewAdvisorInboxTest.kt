package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.LearningOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReviewAdvisorInboxTest {
    @Test fun manuallyFlaggedTopicsFromDifferentExamsStayTogether() {
        val aytBiology = LearningOutcome(
            id = "unit|AYT|Biyoloji|Genden Proteine",
            examType = "AYT", subject = "Biyoloji", topic = "Biyoloji",
            title = "Genden Proteine", reviewRequested = true,
            nextReviewDate = "2026-10-17"
        )
        val tytHistory = LearningOutcome(
            id = "unit|TYT|Tarih|Milli Mücadele",
            examType = "TYT", subject = "Tarih", topic = "Tarih",
            title = "Milli Mücadele", reviewRequested = true,
            nextReviewDate = "2026-10-17"
        )
        val signals = ReviewAdvisor.recommendations(
            listOf(aytBiology, tytHistory), emptyList(), emptyList(),
            LocalDate.parse("2026-10-17")
        )
        assertEquals(setOf(aytBiology.id, tytHistory.id),
            signals.map { it.outcome.id }.toSet())
        assertTrue(signals.all { it.outcome.reviewRequested && it.due })
        assertTrue(signals.all { it.explanation.contains("Kendin tekrar için işaretledin") })
    }

    @Test fun manuallyFlaggedTopicAppearsBeforeAutomaticUrgentSuggestion() {
        val automatic = LearningOutcome("unit|TYT|Fizik|Optik",
            "TYT", "Fizik", "Fizik", "Optik", wrongCount = 3)
        val manual = LearningOutcome("unit|AYT|Kimya|Gazlar",
            "AYT", "Kimya", "Kimya", "Gazlar", reviewRequested = true)
        val signals = ReviewAdvisor.recommendations(
            listOf(automatic, manual), emptyList(), emptyList(),
            LocalDate.parse("2026-10-17")
        )
        assertEquals(manual.id, signals.first().outcome.id)
        assertEquals(2, signals.size)
    }
}
