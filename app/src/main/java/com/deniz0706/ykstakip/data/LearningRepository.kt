package com.deniz0706.ykstakip.data

import android.content.Context
import android.net.Uri
import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.model.QuestionCard
import com.deniz0706.ykstakip.model.QuestionStatus
import org.json.JSONArray
import java.io.File
import java.time.LocalDate
import java.util.UUID

/** All entries live offline. Captured/imported photos are private app files. */
class LearningRepository(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("yks_learning_journal", Context.MODE_PRIVATE)
    private val photoDir = File(app.filesDir, "review_photos").apply { mkdirs() }

    fun outcomes(): List<LearningOutcome> {
        val raw = prefs.getString("outcomes", null)
        val recorded = try {
            val a = JSONArray(raw ?: "[]")
            (0 until a.length()).map { LearningOutcome.fromJson(a.getJSONObject(it)) }
        } catch (_: Exception) { emptyList() }
        val byId = recorded.associateBy { it.id }
        val seeded = LearningSeed.starter.map { byId[it.id] ?: it }
        val custom = recorded.filter { it.id !in LearningSeed.starterIds }
        return seeded + custom
    }

    fun questions(): List<QuestionCard> {
        val raw = prefs.getString("questions", null) ?: return emptyList()
        return try {
            val a = JSONArray(raw)
            (0 until a.length()).map { QuestionCard.fromJson(a.getJSONObject(it)) }
                .sortedByDescending { it.createdAt }
        } catch (_: Exception) { emptyList() }
    }

    private fun saveOutcomes(outcomes: List<LearningOutcome>) {
        prefs.edit().putString("outcomes", JSONArray(outcomes.map { it.toJson() }).toString()).apply()
    }
    private fun saveQuestions(cards: List<QuestionCard>) {
        prefs.edit().putString("questions", JSONArray(cards.map { it.toJson() }).toString()).apply()
    }

    fun addOutcome(examType: String, subject: String, topic: String, title: String): LearningOutcome {
        require(examType in setOf("TYT","AYT") && subject.isNotBlank() && title.isNotBlank())
        val item = LearningOutcome(
            id = UUID.randomUUID().toString(), examType = examType,
            subject = subject.trim(), topic = topic.trim().ifBlank { "Diğer" },
            title = title.trim(), custom = true
        )
        saveOutcomes(outcomes() + item)
        return item
    }

    fun updateOutcome(id: String, transform: (LearningOutcome) -> LearningOutcome) {
        saveOutcomes(outcomes().map { if (it.id == id) transform(it) else it })
    }

    fun setCompleted(id: String, checked: Boolean) {
        updateOutcome(id) { it.copy(completed = checked) }
    }
    fun markWrong(id: String) {
        updateOutcome(id) { it.copy(
            wrongCount = it.wrongCount + 1, reviewRequested = true,
            nextReviewDate = LocalDate.now().plusDays(1).toString(), reviewLevel = 0
        ) }
    }
    fun requestReview(id: String) {
        updateOutcome(id) { it.copy(reviewRequested = true, nextReviewDate = LocalDate.now().toString()) }
    }
    fun reviewOutcome(id: String) {
        val intervals = listOf(1L, 3L, 7L, 14L, 30L)
        updateOutcome(id) {
            val next = (it.reviewLevel + 1).coerceAtMost(intervals.lastIndex)
            it.copy(reviewRequested = false, acknowledgedWrongCount = it.wrongCount,
                reviewLevel = next,
                nextReviewDate = LocalDate.now().plusDays(intervals[next]).toString())
        }
    }
    fun deleteCustomOutcome(id: String) {
        val item = outcomes().find { it.id == id } ?: return
        if (!item.custom || questions().any { it.outcomeId == id }) return
        saveOutcomes(outcomes().filterNot { it.id == id })
    }

    fun addQuestion(outcomeId: String, title: String, notes: String,
                    status: QuestionStatus): QuestionCard {
        require(outcomes().any { it.id == outcomeId }) { "Kazanım bulunamadı" }
        val today = LocalDate.now().toString()
        val card = QuestionCard(UUID.randomUUID().toString(), outcomeId, title.trim(),
            notes.trim(), status, today, LocalDate.now().plusDays(1).toString())
        saveQuestions(questions() + card)
        return card
    }
    fun updateQuestion(id: String, transform: (QuestionCard) -> QuestionCard) {
        saveQuestions(questions().map { if (it.id == id) transform(it) else it })
    }
    fun reviewQuestion(id: String, solved: Boolean) {
        updateQuestion(id) {
            val newCount = it.reviewCount + 1
            it.copy(status = if (solved) QuestionStatus.COZULDU else QuestionStatus.GERI_DON,
                reviewCount = newCount, dueDate = if (solved) ""
                    else LocalDate.now().plusDays(listOf(1L, 3L, 7L, 14L, 30L)
                        [newCount.coerceAtMost(4)]).toString())
        }
    }
    fun markQuestionUnsolved(id: String) {
        updateQuestion(id) { it.copy(status = QuestionStatus.COZULEMEDI,
            dueDate = LocalDate.now().plusDays(1).toString()) }
    }
    fun deleteQuestion(id: String) {
        val q = questions().find { it.id == id } ?: return
        saveQuestions(questions().filterNot { it.id == id })
        q.photos.forEach { photo(it).delete() }
    }

    fun photo(name: String): File = File(photoDir, File(name).name)
    fun newCameraPhoto(): Pair<String, File> {
        val name = UUID.randomUUID().toString() + ".jpg"
        return name to photo(name)
    }
    fun discardCameraPhoto(name: String) { photo(name).delete() }

    fun attachCapturedPhoto(id: String, filename: String) {
        if (!photo(filename).isFile || photo(filename).length() == 0L) {
            discardCameraPhoto(filename)
            return
        }
        if (questions().none { it.id == id }) { discardCameraPhoto(filename); return }
        updateQuestion(id) { it.copy(photos = it.photos + filename) }
    }

    fun attachGalleryPhoto(id: String, uri: Uri): Boolean {
        if (questions().none { it.id == id }) return false
        val (name, target) = newCameraPhoto()
        return try {
            app.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { input.copyTo(it) }
            } ?: return false
            if (target.length() == 0L) return false
            updateQuestion(id) { it.copy(photos = it.photos + name) }
            true
        } catch (_: Exception) { false }
            .also { success -> if (!success) target.delete() }
    }

    fun removePhoto(id: String, filename: String) {
        if (questions().none { it.id == id && filename in it.photos }) return
        updateQuestion(id) { it.copy(photos = it.photos.filterNot { name -> name == filename }) }
        photo(filename).delete()
    }
}
