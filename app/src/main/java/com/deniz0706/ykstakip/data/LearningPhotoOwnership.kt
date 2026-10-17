package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.QuestionCard

/** Never delete a private photo still referenced by another question. */
internal object LearningPhotoOwnership {
    fun unreferenced(candidates: List<String>, retainedCards: List<QuestionCard>): List<String> {
        val stillUsed = retainedCards.flatMapTo(hashSetOf()) { it.photos }
        return candidates.distinct().filterNot { it in stillUsed }
    }
}
