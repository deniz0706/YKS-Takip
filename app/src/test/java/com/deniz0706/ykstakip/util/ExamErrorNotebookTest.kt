package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.model.QuestionStatus
import com.deniz0706.ykstakip.domain.model.SubjectResult
import org.junit.Assert.*
import org.junit.Test

class ExamErrorNotebookTest {
    private fun exam(
        id: Long = 10L,
        subject: String = "Matematik",
        tags: List<String> = listOf("Fonksiyonlar", "Polinomlar")
    ) = Exam(
        id = id, title = "Kurum Denemesi", date = "2026-11-20",
        type = ExamType.TYT,
        subjects = listOf(SubjectResult(subject, 30, 6, 4, 55, tags))
    )

    private fun outcome(id: String = "topic-1", subject: String = "Matematik",
                        topic: String = "Fonksiyonlar") = LearningOutcome(
        id = id, examType = "TYT", subject = subject, topic = topic,
        title = topic
    )

    @Test fun matchesTaggedTopicToExistingLearningOutcome() {
        val suggestions = ExamErrorNotebook.suggestions(
            listOf(exam(tags = listOf("Fonksiyonlar"))),
            listOf(outcome()), emptyList()
        )
        assertEquals(1, suggestions.size)
        assertEquals("topic-1", suggestions.single().linkedOutcomeId)
        assertFalse(suggestions.single().ambiguousMatch)
    }

    @Test fun doesNotGuessWhenMultipleOutcomesMatch() {
        val suggestions = ExamErrorNotebook.suggestions(
            listOf(exam(tags = listOf("Fonksiyonlar"))),
            listOf(outcome("one"), outcome("two")), emptyList()
        )
        assertTrue(suggestions.single().ambiguousMatch)
        assertNull(suggestions.single().linkedOutcomeId)
    }

    @Test fun preservesSectionCountsWithoutCallingThemTopicCounts() {
        val suggestion = ExamErrorNotebook.suggestions(
            listOf(exam(tags = listOf("Fonksiyonlar"))),
            emptyList(), emptyList()
        ).single()
        assertEquals(6, suggestion.wrongCount)
        assertEquals(4, suggestion.blankCount)
        assertNull(suggestion.linkedOutcomeId)
    }

    @Test fun importedSourceMarkerPreventsDuplicateSuggestions() {
        val source = ExamErrorNotebook.suggestions(
            listOf(exam(tags = listOf("Fonksiyonlar"))), emptyList(), emptyList()
        ).single()
        val question = QuestionCard(
            id = "q1", outcomeId = "topic-1", title = "Deneme: Kurum Denemesi",
            notes = "[deneme-kaynak:${source.sourceKey}]",
            status = QuestionStatus.GERI_DON,
            createdAt = "2026-11-20", dueDate = "2026-11-21"
        )
        assertTrue(ExamErrorNotebook.suggestions(
            listOf(exam(tags = listOf("Fonksiyonlar"))), emptyList(), listOf(question)
        ).isEmpty())
    }

    @Test fun mapsTytCombinedSectionsToSeparateLearningCourses() {
        val suggestions = ExamErrorNotebook.suggestions(
            listOf(exam(subject = "Fen Bilimleri", tags = listOf("Hareket"))),
            listOf(outcome(subject = "Fizik", topic = "Hareket")), emptyList()
        )
        assertEquals("topic-1", suggestions.single().linkedOutcomeId)
    }
}
