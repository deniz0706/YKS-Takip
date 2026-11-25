package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome

/**
 * The supplied curriculum stops at the topic level.
 * Kept as a small compatibility adapter for the existing learning UI.
 */
object LearningSubtopics {
    internal fun catalogUnitIds(): Set<String> = emptySet()

    fun forOutcome(outcome: LearningOutcome): List<String> = emptyList()
}
