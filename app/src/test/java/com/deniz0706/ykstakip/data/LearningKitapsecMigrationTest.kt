package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.model.QuestionCard
import com.deniz0706.ykstakip.model.QuestionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningKitapsecMigrationTest {
    @Test fun correctUnitRenameKeepsPhotoLinkAndTick() {
        val previous = LearningOutcome(
            id = "unit|TYT|Fizik|İş Güç Enerji", examType = "TYT",
            subject = "Fizik", topic = "Fizik", title = "İş Güç Enerji", completed = true
        )
        val question = QuestionCard(
            id = "photo-question", outcomeId = previous.id, title = "Soru",
            notes = "", status = QuestionStatus.COZULEMEDI,
            createdAt = "2026-10-17", dueDate = "2026-10-18",
            photos = listOf("question-1.jpg")
        )
        val result = LearningKitapsecMigration.convert(listOf(previous), listOf(question))
        val target = "unit|TYT|Fizik|İş, Güç ve Enerji"
        assertEquals(target, result.questions.single().outcomeId)
        assertEquals(listOf("question-1.jpg"), result.questions.single().photos)
        assertTrue(result.outcomes.single { it.id == target }.completed)
    }

    @Test fun aBroadOldTopicCannotAutoCompleteAnUnrelatedNarrowUnit() {
        val old = LearningOutcome(
            "unit|TYT|Matematik|Sayma ve Olasılık", "TYT", "Matematik",
            "Matematik", "Sayma ve Olasılık", completed = true
        )
        val result = LearningKitapsecMigration.convert(listOf(old), emptyList())
        assertEquals(old.id, result.outcomes.single().id)
        assertEquals("Eski Kayıtlar", result.outcomes.single().subject)
        assertTrue(result.outcomes.single().completed)
    }

    @Test fun renamedButExpandedTopicDoesNotClaimUnstudiedChildren() {
        val old = LearningOutcome(
            "unit|TYT|Fizik|Isı Sıcaklık", "TYT", "Fizik",
            "Fizik", "Isı Sıcaklık", completed = true
        )
        val result = LearningKitapsecMigration.convert(listOf(old), emptyList())
        val newUnit = result.outcomes.single { it.id == "unit|TYT|Fizik|Isı, Sıcaklık ve Genleşme" }
        assertTrue(!newUnit.completed)
        assertEquals("Eski Kayıtlar", result.outcomes.single { it.id == old.id }.subject)
        assertTrue(result.outcomes.single { it.id == old.id }.completed)
    }

    @Test fun previouslyCheckedChildOnlyCompletesThatChild() {
        val old = LearningOutcome(
            "unit|AYT|Biyoloji|Komünite Ekolojisi", "AYT", "Biyoloji",
            "Biyoloji", "Komünite Ekolojisi", completed = true
        )
        val result = LearningKitapsecMigration.convert(listOf(old), emptyList())
        val migrated = result.outcomes.single()
        assertEquals("Komünite ve Popülasyon Ekolojisi", migrated.title)
        assertEquals(listOf("Komünite Ekolojisi"), migrated.completedSubtopics)
        assertTrue(!migrated.completed)
    }

    @Test fun unknownProgressAndUnmappedQuestionAreArchivedNotLost() {
        val old = LearningOutcome(
            id = "unit|TYT|Tarih|UnknownLegacyTitle", examType = "TYT",
            subject = "Tarih", topic = "Tarih", title = "UnknownLegacyTitle",
            wrongCount = 3
        )
        val photo = QuestionCard("q2", old.id, "Unsolved", "",
            QuestionStatus.COZULEMEDI, "2026-10-17", "2026-10-18",
            photos = listOf("img.png"))
        val result = LearningKitapsecMigration.convert(listOf(old), listOf(photo))
        assertEquals(old.id, result.questions.single().outcomeId)
        assertEquals(listOf("img.png"), result.questions.single().photos)
        assertEquals(3, result.outcomes.single().wrongCount)
        assertEquals("Eski Kayıtlar", result.outcomes.single().subject)
    }

    @Test fun previousElectrostaticsTickCompletesOnlyItsChild() {
        val old = LearningOutcome("unit|TYT|Fizik|Elektrostatik",
            "TYT", "Fizik", "Fizik", "Elektrostatik", completed = true)
        val record = LearningKitapsecMigration.convert(listOf(old), emptyList())
            .outcomes.single()
        assertEquals("Elektrik", record.title)
        assertTrue("Elektrik Yükleri" in record.completedSubtopics)
        assertTrue(!record.completed)
    }
}
