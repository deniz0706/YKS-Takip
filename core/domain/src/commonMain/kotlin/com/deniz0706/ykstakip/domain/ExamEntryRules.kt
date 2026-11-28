package com.deniz0706.ykstakip.domain

/**
 * Validates and assembles an exam entry using the same subject limits and time rules on
 * Android and iOS. It is deliberately independent from persistence and UI frameworks.
 */
object ExamEntryRules {
    fun validate(
        type: ExamType,
        aytField: AytField?,
        subjects: List<SubjectResult>
    ): ExamValidationResult {
        if (type == ExamType.AYT && aytField == null) return ExamValidationResult.MissingAytField
        if (type == ExamType.TYT && aytField != null) return ExamValidationResult.UnexpectedAytField

        val expected = if (type == ExamType.TYT) {
            SubjectConfigs.TYT_SUBJECTS
        } else {
            SubjectConfigs.subjectsFor(requireNotNull(aytField))
        }
        val names = subjects.map { it.subject }
        if (names.size != names.distinct().size) return ExamValidationResult.DuplicateSubject
        val expectedNames = expected.map { it.name }.toSet()
        if (names.toSet() != expectedNames) return ExamValidationResult.IncompleteSubjects

        for (subject in subjects) {
            val rule = expected.first { it.name == subject.subject }
            if (subject.questionCount != rule.questionCount) {
                return ExamValidationResult.SubjectQuestionCountMismatch(
                    subject = subject.subject,
                    expected = rule.questionCount,
                    actual = subject.questionCount
                )
            }
            val answerResult = ExamValidation.validateAnswers(
                correct = subject.correct,
                wrong = subject.wrong,
                subject = rule
            )
            if (answerResult != ExamValidationResult.Valid) return answerResult
            if (subject.timeMinutes < 0) return ExamValidationResult.NegativeTime
        }

        val totalTime = subjects.sumOf { it.timeMinutes }
        return ExamValidation.validateTotalTime(totalTime, type.totalTimeMinutes)
    }

    fun create(
        id: Long,
        title: String,
        date: String,
        type: ExamType,
        aytField: AytField? = null,
        subjects: List<SubjectResult>,
        notes: String = "",
        publisher: String = ""
    ): Exam? {
        if (id < 0 || title.isBlank() || date.isBlank()) return null
        if (validate(type, aytField, subjects) != ExamValidationResult.Valid) return null
        return Exam(
            id = id,
            title = title.trim(),
            date = date,
            type = type,
            aytField = aytField,
            subjects = subjects.toList(),
            notes = notes.trim(),
            publisher = publisher.trim()
        )
    }
}
