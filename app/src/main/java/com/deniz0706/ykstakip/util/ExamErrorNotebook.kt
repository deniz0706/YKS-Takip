package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.model.QuestionCard
import java.util.Locale

/** Converts user-tagged mock-exam weak topics into safe learning-center import suggestions. */
object ExamErrorNotebook {
    data class Suggestion(
        val examId: Long,
        val examType: String,
        val examTitle: String,
        val examDate: String,
        val examSubject: String,
        val topic: String,
        val wrongCount: Int,
        val blankCount: Int,
        val linkedOutcomeId: String?,
        val ambiguousMatch: Boolean,
        val sourceKey: String
    )

    private val tr = Locale("tr", "TR")
    private fun normalize(value: String): String = value.lowercase(tr)
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim().replace(Regex("\\s+"), " ")

    fun suggestions(
        exams: List<Exam>,
        outcomes: List<LearningOutcome>,
        questions: List<QuestionCard>
    ): List<Suggestion> {
        val importedKeys = questions.mapNotNull { question ->
            Regex("""\\[deneme-kaynak:([^\\]]+)\\]""")
                .find(question.notes)?.groupValues?.getOrNull(1)
        }.toSet()

        return exams.asSequence()
            .sortedWith(compareByDescending<Exam> { it.date }.thenByDescending { it.id })
            .flatMap { exam ->
                exam.subjects.asSequence().flatMap { subject ->
                    subject.weakTopics.asSequence()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .distinctBy(::normalize)
                        .map { topic ->
                            val key = "${exam.id}|${normalize(subject.subject)}|${normalize(topic)}"
                            val matches = outcomes.filter { outcome ->
                                outcome.examType == exam.type.name &&
                                    LearningCourseMatcher.matches(exam.type.name, subject.subject, outcome.subject) &&
                                    (normalize(outcome.topic) == normalize(topic) ||
                                        normalize(outcome.title) == normalize(topic))
                            }
                            Suggestion(
                                examId = exam.id,
                                examType = exam.type.name,
                                examTitle = exam.title,
                                examDate = exam.date,
                                examSubject = subject.subject,
                                topic = topic,
                                wrongCount = subject.wrong,
                                blankCount = subject.blank,
                                linkedOutcomeId = matches.singleOrNull()?.id,
                                ambiguousMatch = matches.size > 1,
                                sourceKey = key
                            )
                        }
                }
            }
            .filterNot { it.sourceKey in importedKeys }
            .toList()
    }
}
