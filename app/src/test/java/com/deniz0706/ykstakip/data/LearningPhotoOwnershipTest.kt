package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.QuestionCard
import com.deniz0706.ykstakip.model.QuestionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class LearningPhotoOwnershipTest {
    private fun question(id: String, vararg photos: String) = QuestionCard(
        id = id, outcomeId = "unit|TYT|Fizik|Optik",
        title = "Soru", notes = "", status = QuestionStatus.COZULEMEDI,
        createdAt = "2026-10-17", dueDate = "2026-10-18",
        photos = photos.toList()
    )

    @Test fun deletingOneQuestionKeepsImagesStillUsedByAnother() {
        val other = question("other", "shared.jpg", "other.png")
        assertEquals(listOf("only-old.jpg"),
            LearningPhotoOwnership.unreferenced(
                listOf("shared.jpg", "only-old.jpg"), listOf(other)))
    }

    @Test fun removingOneReferenceNeverDeletesAStillSharedPhoto() {
        val retained = listOf(question("first"), question("second", "same.png"))
        assertEquals(emptyList<String>(),
            LearningPhotoOwnership.unreferenced(listOf("same.png"), retained))
    }

    @Test fun unreferencedImagesAreDeletedOnlyOnce() {
        assertEquals(listOf("gone.jpg"),
            LearningPhotoOwnership.unreferenced(
                listOf("gone.jpg", "gone.jpg"), emptyList()))
    }
}
