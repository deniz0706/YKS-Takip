package com.deniz0706.ykstakip.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.model.QuestionCard
import com.deniz0706.ykstakip.model.QuestionStatus
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.util.UUID

/** All entries live offline. Captured/imported photos are private app files. */
class LearningRepository(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("yks_learning_journal", Context.MODE_PRIVATE)
    private val photoDir = File(app.filesDir, "review_photos").apply { mkdirs() }
    private val maxPhotoBytes = 20_000_000L

    init {
        migrateOldSkillsOnce()
        migrateMebVerifiedUnitsOnce()
        migrateMebUnitNamesV2Once()
        migrateKitapsecOnce()
        archiveRemovedAytScienceOnce()
    }

    private fun archiveRemovedAytScienceOnce() {
        if (prefs.getBoolean("learning_science_ayt_separation_v1", false)) return
        val saved = try {
            val a = JSONArray(prefs.getString("outcomes", "[]") ?: "[]")
            (0 until a.length()).map { LearningOutcome.fromJson(a.getJSONObject(it)) }
        } catch (_: Exception) { return }
        val cards = try {
            val a = JSONArray(prefs.getString("questions", "[]") ?: "[]")
            (0 until a.length()).map { QuestionCard.fromJson(a.getJSONObject(it)) }
        } catch (_: Exception) { return }
        val after = LearningScienceSeparation.convert(saved, cards)
        prefs.edit().putString("outcomes", JSONArray(after.map { it.toJson() }).toString())
            .putBoolean("learning_science_ayt_separation_v1", true)
            .commit()
    }

    /** One-time, transactional change of curriculum without deleting linked photos. */
    private fun migrateKitapsecOnce() {
        if (prefs.getBoolean("learning_kitapsec_v1", false)) return
        val oldRawOutcomes = prefs.getString("outcomes", "[]") ?: "[]"
        val oldRawQuestions = prefs.getString("questions", "[]") ?: "[]"
        val recorded = try {
            val arr = JSONArray(oldRawOutcomes)
            (0 until arr.length()).map { LearningOutcome.fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { return }
        val linked = try {
            val arr = JSONArray(oldRawQuestions)
            (0 until arr.length()).map { QuestionCard.fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { return }

        val migrated = LearningKitapsecMigration.convert(recorded, linked)
        val defaults = LearningSeed.starter.associateBy { it.id }
        val saved = migrated.outcomes.filter { defaults[it.id] != it }
        prefs.edit()
            // Raw snapshots are retained for recovery, separately from JSON/ZIP exports.
            .putString("before_kitapsec_outcomes_v1", oldRawOutcomes)
            .putString("before_kitapsec_questions_v1", oldRawQuestions)
            .putString("outcomes", JSONArray(saved.map { it.toJson() }).toString())
            .putString("questions", JSONArray(migrated.questions.map { it.toJson() }).toString())
            .putBoolean("learning_kitapsec_v1", true)
            .commit()
    }

    /** Second official unit-name review. Runs also on devices that completed v1. */
    private fun migrateMebUnitNamesV2Once() {
        if (prefs.getBoolean("learning_meb_units_v2", false)) return
        val savedOutcomes = try {
            val arr = JSONArray(prefs.getString("outcomes", "[]") ?: "[]")
            (0 until arr.length()).map { LearningOutcome.fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { return }
        val savedQuestions = try {
            val arr = JSONArray(prefs.getString("questions", "[]") ?: "[]")
            (0 until arr.length()).map { QuestionCard.fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { return }
        val migrated = LearningSeed.mergeLegacyProgress(savedOutcomes)
        val remapped = savedQuestions.map { card ->
            card.copy(outcomeId = LearningSeed.topicIdFor(card.outcomeId))
        }
        val defaults = LearningSeed.starter.associateBy { it.id }
        val changed = migrated.filter { defaults[it.id] != it }
        prefs.edit()
            .putString("outcomes", JSONArray(changed.map { it.toJson() }).toString())
            .putString("questions", JSONArray(remapped.map { it.toJson() }).toString())
            .putBoolean("learning_meb_units_v2", true)
            .commit()
    }

    /** Apply post-2026-MEB-audit unit renames without losing photo references. */
    private fun migrateMebVerifiedUnitsOnce() {
        if (prefs.getBoolean("learning_meb_units_v1", false)) return
        val savedOutcomes = try {
            val arr = JSONArray(prefs.getString("outcomes", "[]") ?: "[]")
            (0 until arr.length()).map { LearningOutcome.fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { return }
        val savedQuestions = try {
            val arr = JSONArray(prefs.getString("questions", "[]") ?: "[]")
            (0 until arr.length()).map { QuestionCard.fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { return }
        val migrated = LearningSeed.mergeLegacyProgress(savedOutcomes)
        val remapped = savedQuestions.map { card ->
            card.copy(outcomeId = LearningSeed.topicIdFor(card.outcomeId))
        }
        val defaults = LearningSeed.starter.associateBy { it.id }
        val changes = migrated.filter { defaults[it.id] != it }
        prefs.edit()
            .putString("outcomes", JSONArray(changes.map { it.toJson() }).toString())
            .putString("questions", JSONArray(remapped.map { it.toJson() }).toString())
            .putBoolean("learning_meb_units_v1", true)
            .commit()
    }

    /**
     * Migrate before displaying any rows: otherwise historical skill IDs
     * appear as user-defined topics and attached photos become orphaned.
     * A single committed preferences transaction preserves the links.
     */
    private fun migrateOldSkillsOnce() {
        if (prefs.getBoolean("learning_topics_v1", false)) return
        val storedOutcomes = try {
            val arr = JSONArray(prefs.getString("outcomes", "[]") ?: "[]")
            (0 until arr.length()).map { LearningOutcome.fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { return }
        val storedQuestions = try {
            val arr = JSONArray(prefs.getString("questions", "[]") ?: "[]")
            (0 until arr.length()).map { QuestionCard.fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { return }
        val migrated = LearningSeed.mergeLegacyProgress(storedOutcomes)
        val linked = storedQuestions.map {
            it.copy(outcomeId = LearningSeed.topicIdFor(it.outcomeId))
        }
        val defaults = LearningSeed.starter.associateBy { it.id }
        val changed = migrated.filter { defaults[it.id] != it }
        prefs.edit()
            .putString("outcomes", JSONArray(changed.map { it.toJson() }).toString())
            .putString("questions", JSONArray(linked.map { it.toJson() }).toString())
            .putBoolean("learning_topics_v1", true)
            .commit()
    }

    private fun isValidImage(file: File): Boolean {
        if (!file.isFile || file.length() !in 1L..maxPhotoBytes) return false
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        return try {
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            bounds.outWidth > 0 && bounds.outHeight > 0
        } catch (_: Exception) {
            false
        }
    }

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
        // The catalog now has hundreds of default items. Only persist modified
        // seeded items plus custom entries, not 800+ untouched JSON objects after
        // each checkbox tap. The defaults are reconstructed from stable IDs.
        val defaults = LearningSeed.starter.associateBy { it.id }
        val changed = outcomes.filter { outcome ->
            val base = defaults[outcome.id]
            base == null || base != outcome
        }
        prefs.edit().putString(
            "outcomes", JSONArray(changed.map { it.toJson() }).toString()
        ).apply()
    }
    private fun saveQuestions(cards: List<QuestionCard>) {
        prefs.edit().putString("questions", JSONArray(cards.map { it.toJson() }).toString()).apply()
    }

    /** Persist a verified archive merge in one preferences transaction. */
    fun persistRestoredSnapshot(outcomes: List<LearningOutcome>, cards: List<QuestionCard>): Boolean {
        val defaults = LearningSeed.starter.associateBy { it.id }
        val changed = outcomes.filter { defaults[it.id] != it }
        return prefs.edit()
            .putString("outcomes", JSONArray(changed.map { it.toJson() }).toString())
            .putString("questions", JSONArray(cards.map { it.toJson() }).toString())
            .commit()
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
        updateOutcome(id) { unit ->
            unit.copy(completed = checked,
                completedSubtopics = if (checked) LearningSubtopics.forOutcome(unit) else emptyList())
        }
    }

    fun setSubtopicCompleted(id: String, subtopic: String, checked: Boolean) {
        updateOutcome(id) { unit ->
            val all = LearningSubtopics.forOutcome(unit)
            if (subtopic !in all) unit else {
                val previous = if (unit.completed && unit.completedSubtopics.isEmpty())
                    all.toSet() else unit.completedSubtopics.toSet()
                val selected = if (checked) previous + subtopic else previous - subtopic
                val done = all.filter { it in selected }
                unit.copy(completedSubtopics = done, completed = done.size == all.size)
            }
        }
    }
    fun markWrong(id: String) {
        updateOutcome(id) { it.copy(
            completed = false, wrongCount = it.wrongCount + 1,
            reviewRequested = false,
            nextReviewDate = LocalDate.now().plusDays(1).toString(), reviewLevel = 0
        ) }
    }
    fun requestReview(id: String) {
        updateOutcome(id) { it.copy(reviewRequested = true, nextReviewDate = LocalDate.now().toString()) }
    }
    fun postponeReview(id: String) {
        updateOutcome(id) {
            it.copy(completed = false, reviewRequested = false, reviewLevel = 0,
                lastReviewedDate = LocalDate.now().toString(),
                nextReviewDate = LocalDate.now().plusDays(1).toString())
        }
    }

    fun reviewOutcome(id: String) {
        val intervals = listOf(1L, 3L, 7L, 14L, 30L)
        updateOutcome(id) {
            // level 0 means the NEXT successful review is due tomorrow.
            // Preserve the existing saved level as the next index after each success.
            val days = intervals[it.reviewLevel.coerceIn(0, intervals.lastIndex)]
            val nextLevel = (it.reviewLevel + 1).coerceAtMost(intervals.lastIndex)
            it.copy(completed = true,
                completedSubtopics = LearningSubtopics.forOutcome(it),
                reviewRequested = false,
                acknowledgedWrongCount = it.wrongCount,
                reviewLevel = nextLevel, lastReviewedDate = LocalDate.now().toString(),
                nextReviewDate = LocalDate.now().plusDays(days).toString())
        }
    }
    fun deleteCustomOutcome(id: String) {
        val item = outcomes().find { it.id == id } ?: return
        if (!item.custom || questions().any { it.outcomeId == id }) return
        saveOutcomes(outcomes().filterNot { it.id == id })
    }

    fun addQuestion(outcomeId: String, title: String, notes: String,
                    status: QuestionStatus): QuestionCard {
        require(outcomes().any { it.id == outcomeId }) { "Konu bulunamadı" }
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
                reviewCount = newCount,
                // An unsolved question should come back tomorrow, not slip
                // farther into a spaced-repetition schedule.
                dueDate = if (solved) "" else LocalDate.now().plusDays(1).toString())
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
        val file = photo(name)
        // FileProvider needs a real writable target before the camera starts.
        file.parentFile?.mkdirs()
        file.createNewFile()
        return name to file
    }
    fun discardCameraPhoto(name: String) { photo(name).delete() }

    fun attachCapturedPhoto(id: String, filename: String): Boolean {
        if (!isValidImage(photo(filename))) {
            discardCameraPhoto(filename)
            return false
        }
        if (questions().none { it.id == id }) {
            discardCameraPhoto(filename)
            return false
        }
        updateQuestion(id) { it.copy(photos = it.photos + filename) }
        return true
    }

    fun attachGalleryPhoto(id: String, uri: Uri): Boolean {
        if (questions().none { it.id == id }) return false
        var pending: File? = null
        var success = false
        try {
            val (name, target) = newCameraPhoto()
            pending = target
            // Do not copy an unbounded gallery stream into the private app folder.
            val input = app.contentResolver.openInputStream(uri) ?: return false
            input.use { source ->
                target.outputStream().use { destination ->
                    val buffer = ByteArray(16 * 1024)
                    var copied = 0L
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        if (count == 0) continue
                        copied += count
                        if (copied > maxPhotoBytes) throw IOException("Image is too large")
                        destination.write(buffer, 0, count)
                    }
                }
            }
            // A URI advertised as image/* might contain an invalid or unsupported file.
            if (!isValidImage(target)) return false
            if (questions().none { it.id == id }) return false
            updateQuestion(id) { it.copy(photos = it.photos + name) }
            success = true
        } catch (_: Exception) {
            success = false
        } finally {
            if (!success) pending?.delete()
        }
        return success
    }

    fun removePhoto(id: String, filename: String) {
        if (questions().none { it.id == id && filename in it.photos }) return
        updateQuestion(id) { it.copy(photos = it.photos.filterNot { name -> name == filename }) }
        photo(filename).delete()
    }
}
