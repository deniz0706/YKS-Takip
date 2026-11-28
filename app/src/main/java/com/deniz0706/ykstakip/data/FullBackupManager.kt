package com.deniz0706.ykstakip.data

import android.content.Context
import android.net.Uri
import android.content.SharedPreferences
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.StudyDay
import com.deniz0706.ykstakip.data.repository.SqlExamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Safe merge-only archive for exams, study history, learning records, settings,
 * study plans, theme, focus timer, exam session, AI memory and private question photos. */
object FullBackupManager {
    private const val SCHEMA = "yks_takip_full_backup"
    private const val VERSION = 2
    private val SUPPORTED_VERSIONS = setOf(1, 2)
    private const val MAX_MANIFEST = 1_000_000L
    private const val MAX_JSON = 8_000_000L
    private const val MAX_LEARNING = 260_000_000L
    private const val MAX_SETTINGS = 100_000L
    private const val MAX_TOTAL = 280_000_000L

    data class ExportResult(
        val exams: Int,
        val studyDays: Int,
        val learningOutcomes: Int,
        val learningQuestions: Int,
        val photos: Int,
        val settingsCount: Int,
        val studyPlanCount: Int,
        val themeCount: Int,
        val focusSessionCount: Int,
        val examSessionCount: Int,
        val aiMemoryCount: Int
    )

    data class ImportResult(
        val examsAdded: Int,
        val studyDaysAdded: Int,
        val learning: LearningArchiveManager.ImportResult,
        val settings: SettingsBackupManager.Parsed,
        val studyPlan: StudyPlanBackupManager.Parsed,
        val theme: ThemeBackupManager.Parsed?,
        val focusSession: FocusSessionBackupManager.Parsed?,
        val examSession: ExamSessionBackupManager.Parsed?,
        val aiMemory: AiMemoryBackupManager.Parsed?,
        val migratedFromV1: Boolean
    )

    class PreparedImport internal constructor(
        internal val exams: List<Exam>,
        internal val studyDays: List<StudyDay>,
        internal val learning: LearningArchiveManager.PreparedImport,
        internal val settingsJson: String?,
        internal val studyPlanJson: String?,
        internal val themeJson: String?,
        internal val focusSessionJson: String?,
        internal val examSessionJson: String?,
        internal val aiMemoryJson: String?,
        internal val migratedFromV1: Boolean,
        private val staging: File
    ) {
        val examCount: Int get() = exams.size
        val studyDayCount: Int get() = studyDays.size
        val learningOutcomeCount: Int get() = learning.outcomes.size
        val learningQuestionCount: Int get() = learning.questions.size
        val photoCount: Int get() = learning.photoCount

        fun discard() {
            learning.discard()
            staging.deleteRecursively()
        }
    }

    fun exportTo(context: Context, destination: Uri): ExportResult {
        val app = context.applicationContext
        LegacyDataMigrator.ensureMigrationReadyForWrites(app)
        val examRepository = SqlExamRepository.getInstance(app)
        val exams = runBlocking(Dispatchers.IO) { examRepository.getAllExams().first() }
        val studyRepository = StudyRepository(app)
        val studyDays = studyRepository.getAll()
        check(!studyRepository.hasUnreadableLegacyData()) {
            "Çalışma geçmişi okunamadı. Veri kaybını önlemek için tam yedek oluşturma durduruldu."
        }
        val examBytes = BackupManager.exportToJson(exams).toByteArray(Charsets.UTF_8)
        val studyBytes = StudyBackupManager.exportJson(studyDays).toByteArray(Charsets.UTF_8)
        val settingsBytes = SettingsBackupManager.exportJson(app).toByteArray(Charsets.UTF_8)
        val studyPlanBytes = StudyPlanBackupManager.exportJson(app).toByteArray(Charsets.UTF_8)
        val themeBytes = ThemeBackupManager.exportJson(app).toByteArray(Charsets.UTF_8)
        val focusBytes = FocusSessionBackupManager.exportJson(app).toByteArray(Charsets.UTF_8)
        val examSessionBytes = ExamSessionBackupManager.exportJson(app).toByteArray(Charsets.UTF_8)
        val aiMemoryBytes = AiMemoryBackupManager.exportJson(app).toByteArray(Charsets.UTF_8)
        if (examBytes.size > MAX_JSON || studyBytes.size > MAX_JSON ||
            settingsBytes.size > MAX_SETTINGS || studyPlanBytes.size > MAX_JSON ||
            themeBytes.size > MAX_JSON || focusBytes.size > MAX_JSON ||
            examSessionBytes.size > MAX_JSON || aiMemoryBytes.size > MAX_JSON
        ) {
            throw IOException("Deneme veya çalışma yedeği boyut sınırını aşıyor.")
        }

        val learningFile = File(app.cacheDir, "full-backup-learning-${UUID.randomUUID()}.zip")
        try {
            learningFile.createNewFile()
            val learningResult = LearningArchiveManager(app).exportTo(Uri.fromFile(learningFile))
            val manifest = JSONObject().apply {
                put("schema", SCHEMA)
                put("version", VERSION)
                put("createdAt", Instant.now().toString())
                put("examCount", exams.size)
                put("studyDayCount", studyDays.size)
                put("learningOutcomeCount", learningResult.outcomes)
                put("learningQuestionCount", learningResult.questions)
                put("photoCount", learningResult.photos)
                put("settingsCount", settingsBytes.size)
                put("studyPlanCount", studyPlanBytes.size)
                put("themeCount", themeBytes.size)
                put("focusSessionCount", focusBytes.size)
                put("examSessionCount", examSessionBytes.size)
                put("aiMemoryCount", aiMemoryBytes.size)
                put("secretsIncluded", false)
            }.toString().toByteArray(Charsets.UTF_8)

            val output = app.contentResolver.openOutputStream(destination, "w")
                ?: throw IOException("Yedek dosyası oluşturulamadı.")
            output.use { raw ->
                ZipOutputStream(raw).use { zip ->
                    writeEntry(zip, "manifest.json", manifest)
                    writeEntry(zip, "exams.json", examBytes)
                    writeEntry(zip, "study.json", studyBytes)
                    writeEntry(zip, "settings.json", settingsBytes)
                    writeEntry(zip, "study_plan.json", studyPlanBytes)
                    writeEntry(zip, "theme.json", themeBytes)
                    writeEntry(zip, "focus_session.json", focusBytes)
                    writeEntry(zip, "exam_session.json", examSessionBytes)
                    writeEntry(zip, "ai_memory.json", aiMemoryBytes)
                    zip.putNextEntry(ZipEntry("learning.zip"))
                    learningFile.inputStream().use { it.copyTo(zip, 32 * 1024) }
                    zip.closeEntry()
                }
            }
            return ExportResult(
                exams.size, studyDays.size, learningResult.outcomes,
                learningResult.questions, learningResult.photos,
                settingsBytes.size, studyPlanBytes.size,
                themeBytes.size, focusBytes.size, examSessionBytes.size, aiMemoryBytes.size
            )
        } finally {
            learningFile.delete()
        }
    }

    /** Required sections present in every full backup (v1 and v2). */
    private val REQUIRED_SECTIONS = setOf("manifest.json", "exams.json", "study.json", "learning.zip")

    /** Optional sections added in v2. */
    private val OPTIONAL_V2_SECTIONS = setOf("settings.json", "study_plan.json", "theme.json",
        "focus_session.json", "exam_session.json", "ai_memory.json")

    fun prepareImport(context: Context, source: Uri): PreparedImport {
        val app = context.applicationContext
        val stage = File(app.cacheDir, "full-backup-import-${UUID.randomUUID()}")
        if (!stage.mkdirs()) throw IOException("Geçici yedek klasörü açılamadı.")
        try {
            val allowed = REQUIRED_SECTIONS + OPTIONAL_V2_SECTIONS
            val extracted = mutableMapOf<String, File>()
            var total = 0L
            val input = app.contentResolver.openInputStream(source)
                ?: throw IOException("Yedek dosyası okunamadı.")
            input.use { stream ->
                ZipInputStream(stream).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        val name = entry.name
                        if (entry.isDirectory || name !in allowed || extracted.containsKey(name)) {
                            throw IOException("Yedekte beklenmeyen veya tekrarlanan dosya var.")
                        }
                        val limit = when (name) {
                            "manifest.json" -> MAX_MANIFEST
                            "exams.json", "study.json", "study_plan.json",
                            "theme.json", "focus_session.json", "exam_session.json",
                            "ai_memory.json" -> MAX_JSON
                            "settings.json" -> MAX_SETTINGS
                            "learning.zip" -> MAX_LEARNING
                            else -> throw IOException("Bilinmeyen yedek bölümü.")
                        }
                        val file = File(stage, name)
                        val written = file.outputStream().use { output ->
                            val buffer = ByteArray(16 * 1024)
                            var count = 0L
                            while (true) {
                                val read = zip.read(buffer)
                                if (read < 0) break
                                if (read == 0) continue
                                count += read
                                total += read
                                if (count > limit || total > MAX_TOTAL) {
                                    throw IOException("Yedek dosyası boyut sınırını aştı.")
                                }
                                output.write(buffer, 0, read)
                            }
                            count
                        }
                        if (written <= 0L) throw IOException("Yedekte boş bölüm var: $name")
                        extracted[name] = file
                        zip.closeEntry()
                    }
                }
            }
            if (!REQUIRED_SECTIONS.all { extracted.containsKey(it) }) throw IOException("Tam yedek gerekli bölümleri içermiyor.")
            val manifest = JSONObject(extracted.getValue("manifest.json").readText())
            require(manifest.optString("schema") == SCHEMA) { "Bu dosya YKS-Takip tam yedeği değil." }
            val version = manifest.optInt("version", -1)
            require(version in SUPPORTED_VERSIONS) { "Desteklenmeyen tam yedek sürümü: $version" }
            val migratedFromV1 = version == 1

            val examResult = BackupManager.readFromUri(app.contentResolver, Uri.fromFile(extracted.getValue("exams.json")))
            val exams = (examResult as? BackupManager.ImportResult.Success)?.exams
                ?: throw IOException((examResult as? BackupManager.ImportResult.Error)?.message ?: "Deneme bölümü okunamadı.")
            val study = StudyBackupManager.parseJson(extracted.getValue("study.json").readText())

            // Keep optional sections staged as raw JSON. Parsing/importing them here used to
            // mutate SharedPreferences before the complete archive had been validated.
            val settingsJson = extracted["settings.json"]?.readText()
            val studyPlanJson = extracted["study_plan.json"]?.readText()
            val themeJson = extracted["theme.json"]?.readText()
            val focusSessionJson = extracted["focus_session.json"]?.readText()
            val examSessionJson = extracted["exam_session.json"]?.readText()
            val aiMemoryJson = extracted["ai_memory.json"]?.readText()

            val learning = LearningArchiveManager(app).prepareImport(Uri.fromFile(extracted.getValue("learning.zip")))
            extracted.getValue("learning.zip").delete()

            // Validate required section counts against manifest (manifest may omit optional counts in v1)
            if (manifest.optInt("examCount", -1) != exams.size ||
                manifest.optInt("studyDayCount", -1) != study.days.size ||
                manifest.optInt("learningOutcomeCount", -1) != learning.outcomes.size ||
                manifest.optInt("learningQuestionCount", -1) != learning.questions.size ||
                manifest.optInt("photoCount", -1) != learning.photoCount
            ) {
                learning.discard()
                throw IOException("Yedek özeti ile içerik sayıları uyuşmuyor.")
            }
            return PreparedImport(exams, study.days, learning, settingsJson, studyPlanJson,
                themeJson, focusSessionJson, examSessionJson, aiMemoryJson, migratedFromV1, stage)
        } catch (e: Exception) {
            stage.deleteRecursively()
            if (e is IOException) throw e
            throw IOException("Tam yedek doğrulanamadı: ${e.message}", e)
        }
    }

    /** Merge-only restore. All optional sections are applied only after staging/validation,
     * and their SharedPreferences snapshots are restored if any later merge step fails. */
    fun merge(context: Context, prepared: PreparedImport): ImportResult {
        val app = context.applicationContext
        LegacyDataMigrator.ensureMigrationReadyForWrites(app)
        val examRepository = SqlExamRepository.getInstance(app)
        val studyRepository = StudyRepository(app)
        val previousExams = runBlocking(Dispatchers.IO) { examRepository.getAllExams().first() }
        val existingExamIds = previousExams.map { it.id }.toSet()
        val mergedExams = previousExams + prepared.exams.filterNot { it.id in existingExamIds }
        val preferenceNames = listOf(
            "yks_takip_prefs",
            "yks_takip_study_plan",
            "yks_focus_session",
            "yks_exam_session_v1",
            "ai_coach_long_term_memory"
        )
        val preferenceSnapshots = preferenceNames.associateWith { name ->
            PreferenceSnapshot.capture(app.getSharedPreferences(name, Context.MODE_PRIVATE))
        }
        var examsApplied = false
        var studyChanges = 0
        try {
            // importJson methods validate their complete payload before writing. They are
            // intentionally called here, not from prepareImport, so a rejected archive
            // cannot alter user settings merely by being previewed.
            val settings = prepared.settingsJson?.let {
                SettingsBackupManager.importJson(app, it)
            } ?: SettingsBackupManager.Parsed(null, null, null, null, null, null, null, null)
            val studyPlan = prepared.studyPlanJson?.let {
                StudyPlanBackupManager.importJson(app, it)
            } ?: StudyPlanBackupManager.Parsed(emptyList())
            val theme = prepared.themeJson?.let { ThemeBackupManager.importJson(app, it) }
            val focusSession = prepared.focusSessionJson?.let {
                FocusSessionBackupManager.importJson(app, it)
            }
            val examSession = prepared.examSessionJson?.let {
                ExamSessionBackupManager.importJson(app, it)
            }
            val aiMemory = prepared.aiMemoryJson?.let { AiMemoryBackupManager.importJson(app, it) }

            studyChanges = studyRepository.importDays(prepared.studyDays, overwrite = false)
            runBlocking(Dispatchers.IO) {
                examRepository.replaceAll(mergedExams)
                examsApplied = true
            }
            // LearningArchiveManager handles its own photo-file rollback on failure.
            // It is deliberately the final data merge so no later operation can leave
            // a successfully imported learning archive paired with rolled-back settings.
            val learningResult = LearningArchiveManager(app).merge(prepared.learning)
            return ImportResult(
                examsAdded = mergedExams.size - previousExams.size,
                studyDaysAdded = studyChanges,
                learning = learningResult,
                settings = settings,
                studyPlan = studyPlan,
                theme = theme,
                focusSession = focusSession,
                examSession = examSession,
                aiMemory = aiMemory,
                migratedFromV1 = prepared.migratedFromV1
            )
        } catch (e: Exception) {
            if (examsApplied) {
                runCatching { runBlocking(Dispatchers.IO) { examRepository.replaceAll(previousExams) } }
                    .onFailure(e::addSuppressed)
            }
            if (studyChanges > 0 && studyRepository.canUndoLastImport()) {
                runCatching { studyRepository.undoLastImport() }.onFailure(e::addSuppressed)
            }
            preferenceSnapshots.forEach { (name, snapshot) ->
                runCatching {
                    snapshot.restore(app.getSharedPreferences(name, Context.MODE_PRIVATE))
                }.onFailure(e::addSuppressed)
            }
            throw e
        } finally {
            prepared.discard()
        }
    }

    /** Exact snapshot of a SharedPreferences file, including unknown/future keys. */
    private data class PreferenceSnapshot(private val values: Map<String, Any?>) {
        fun restore(preferences: SharedPreferences) {
            val editor = preferences.edit().clear()
            values.forEach { (key, value) ->
                when (value) {
                    null -> editor.remove(key)
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        editor.putStringSet(key, value as Set<String>)
                    }
                    else -> throw IllegalStateException("Desteklenmeyen ayar türü: ${value::class.java.name}")
                }
            }
            check(editor.commit()) { "Yedek geri alma işlemi tamamlanamadı." }
        }

        companion object {
            fun capture(preferences: SharedPreferences): PreferenceSnapshot =
                PreferenceSnapshot(preferences.all.toMap())
        }
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, bytes: ByteArray) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(bytes)
        zip.closeEntry()
    }
}
