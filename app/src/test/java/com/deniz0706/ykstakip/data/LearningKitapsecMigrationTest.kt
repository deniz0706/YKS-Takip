package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.model.QuestionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningKitapsecMigrationTest {
    @Test fun renamedTopicKeepsProgressAndQuestionPhoto() {
        val previous = LearningOutcome(
            id = "unit|TYT|Fizik|Elektrik",
            examType = "TYT",
            subject = "Fizik",
            topic = "Fizik",
            title = "Elektrik",
            completed = true,
            wrongCount = 2,
            reviewLevel = 3
        )
        val question = QuestionCard(
            id = "photo-question",
            outcomeId = previous.id,
            title = "Soru",
            notes = "",
            status = QuestionStatus.COZULEMEDI,
            createdAt = "2026-10-17",
            dueDate = "2026-10-18",
            photos = listOf("question-1.jpg")
        )

        val result = LearningCurriculumMigration.convert(listOf(previous), listOf(question))
        val target = "unit|TYT|Fizik|Elektrik Akımı ve Devreler"
        assertEquals(target, result.questions.single().outcomeId)
        assertEquals(listOf("question-1.jpg"), result.questions.single().photos)
        val migrated = result.outcomes.single { it.id == target }
        assertTrue(migrated.completed)
        assertEquals(2, migrated.wrongCount)
        assertEquals(3, migrated.reviewLevel)
        assertEquals("Elektrik ve Manyetizma", migrated.topic)
    }

    @Test fun existingTopicGetsItsNewUnitGroupWithoutLosingItsTick() {
        val old = LearningOutcome(
            id = "unit|TYT|Türkçe|Sözcükte Anlam",
            examType = "TYT",
            subject = "Türkçe",
            topic = "Türkçe",
            title = "Sözcükte Anlam",
            completed = true
        )
        val migrated = LearningCurriculumMigration.convert(listOf(old), emptyList())
            .outcomes.single()
        assertEquals("Anlam Bilgisi", migrated.topic)
        assertTrue(migrated.completed)
    }

    @Test fun removedTopicAndItsQuestionAreArchivedNotDeleted() {
        val old = LearningOutcome(
            id = "unit|TYT|Tarih|OldLegacyTitle",
            examType = "TYT",
            subject = "Tarih",
            topic = "Tarih",
            title = "OldLegacyTitle",
            wrongCount = 3
        )
        val question = QuestionCard(
            id = "old-question",
            outcomeId = old.id,
            title = "Eski soru",
            notes = "",
            status = QuestionStatus.COZULEMEDI,
            createdAt = "2026-10-17",
            dueDate = "2026-10-18",
            photos = listOf("old-photo.jpg")
        )
        val result = LearningCurriculumMigration.convert(listOf(old), listOf(question))
        assertEquals(old.id, result.questions.single().outcomeId)
        assertEquals(listOf("old-photo.jpg"), result.questions.single().photos)
        val archived = result.outcomes.single { it.id == old.id }
        assertEquals("Eski Kayıtlar", archived.subject)
        assertEquals(3, archived.wrongCount)
    }

    @Test fun questionOnlyReferenceIsRemappedEvenWithoutSavedProgress() {
        val oldId = "unit|TYT|Matematik|Oran Orantı"
        val question = QuestionCard(
            id = "question-only",
            outcomeId = oldId,
            title = "Soru",
            notes = "",
            status = QuestionStatus.COZULEMEDI,
            createdAt = "2026-10-17",
            dueDate = "2026-10-18",
            photos = listOf("ratio.jpg")
        )
        val result = LearningCurriculumMigration.convert(emptyList(), listOf(question))
        assertEquals(
            "unit|TYT|Matematik|Oran – Orantı",
            result.questions.single().outcomeId
        )
        assertEquals(listOf("ratio.jpg"), result.questions.single().photos)
    }

    @Test fun previouslyArchivedLegacyRecordCanReturnToItsMatchingTopic() {
        val old = LearningOutcome(
            id = "unit|TYT|Türkçe|Paragraf",
            examType = "TYT",
            subject = "Eski Kayıtlar",
            topic = "Önceki Müfredat",
            title = "Türkçe: Paragraf",
            completed = true,
            custom = true
        )
        val migrated = LearningCurriculumMigration.convert(listOf(old), emptyList())
            .outcomes.single()
        assertEquals("unit|TYT|Türkçe|Paragrafta Anlam ve Yapı", migrated.id)
        assertEquals("Anlam Bilgisi", migrated.topic)
        assertTrue(migrated.completed)
        assertTrue(!migrated.custom)
    }

    @Test fun userCreatedTopicsRemainUntouched() {
        val custom = LearningOutcome(
            id = "user-topic-1",
            examType = "TYT",
            subject = "Matematik",
            topic = "Kişisel",
            title = "Kendi konum",
            custom = true
        )
        val migrated = LearningCurriculumMigration.convert(listOf(custom), emptyList())
            .outcomes.single()
        assertEquals(custom, migrated)
    }
}
