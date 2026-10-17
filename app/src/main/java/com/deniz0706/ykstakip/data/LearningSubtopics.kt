package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome

/** Nested headings from user-supplied nine-page TYT/AYT teacher handout.
 * This reference is not the official MEB YKS scope. Unlisted units remain
 * ordinary checkboxes rather than using invented subtopics.
 */
object LearningSubtopics {
    private val catalog: Map<String, List<String>> by lazy {
        val result = linkedMapOf<String, List<String>>()
        rows.trimIndent().lineSequence().filter { it.isNotBlank() }.forEach { row ->
            val parts = row.trim().split('|', limit = 4)
            require(parts.size == 4)
            val id = parts.take(3).joinToString("|") { it.trim() }
            require(id !in result) { "Duplicate subtopic group: $id" }
            result[id] = parts[3].split(';').map { it.trim() }
                .filter { it.isNotBlank() }.distinct()
        }
        result
    }
    fun forOutcome(outcome: LearningOutcome): List<String> =
        catalog["${outcome.examType}|${outcome.subject}|${outcome.title}"].orEmpty()

    private val rows = """
${data.trim()}
    """.trimIndent()
}
