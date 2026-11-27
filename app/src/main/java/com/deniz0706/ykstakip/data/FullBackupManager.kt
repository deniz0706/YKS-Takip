package com.deniz0706.ykstakip.data

import android.content.Context
import android.net.Uri
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.StudyDay
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Safe merge-only archive for exams, study history, learning records and private question photos. */
object FullBackupManager {
    private const val SCHEMA = "yks_takip_full_backup"
    private const val VERSION = 1
    private const val MAX_MANIFEST = 1_000_000L
    private const val MAX_JSON = 8_000_000L
    private const val MAX_LEARNING = 260_000_000L
    private const val MAX_TOTAL = 280_000_000L

    data class ExportResult(
        val exams: Int,
        val studyDays: Int,
        val learningOutcomes: Int,
        val learningQuestions: Int,
        val photos: Int
    )

    data class ImportResult(
        val examsAdded: Int,
        val studyDaysAdded: Int,
        val learning: LearningArchiveManager.ImportResult
    )

    class PreparedImport internal constructor(
        internal val exams: List<Exam>,
        internal val studyDays: List<StudyDay>,
        internal val learning: LearningArchiveManager.PreparedImport,
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
        val exams = ExamRepository.getInstance(app).getAllExams()
        val studyDays = StudyRepository(app).getAll()
        val examBytes = BackupManager.exportToJson(exams).toByteArray(Charsets.UTF_8)
        val studyBytes = StudyBackupManager.exportJson(studyDays).toByteArray(Charsets.UTF_8)
        if (examBytes.size > MAX_JSON || studyBytes.size > MAX_JSON) {
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
                put("secretsIncluded", false)
            }.toString().toByteArray(Charsets.UTF_8)

            val output = app.contentResolver.openOutputStream(destination, "w")
                ?: throw IOException("Yedek dosyası oluşturulamadı.")
            output.use { raw ->
                ZipOutputStream(raw).use { zip ->
                    writeEntry(zip, "manifest.json", manifest)
                    writeEntry(zip, "exams.json", examBytes)
                    writeEntry(zip, "study.json", studyBytes)
                    zip.putNextEntry(ZipEntry("learning.zip"))
                    learningFile.inputStream().use { it.copyTo(zip, 32 * 1024) }
                    zip.closeEntry()
                }
            }
            return ExportResult(
                exams.size, studyDays.size, learningResult.outcomes,
                learningResult.questions, learningResult.photos
            )
        } finally {
            learningFile.delete()
        }
    }

    fun prepareImport(context: Context, source: Uri): PreparedImport {
        val app = context.applicationContext
        val stage = File(app.cacheDir, "full-backup-import-${UUID.randomUUID()}")
        if (!stage.mkdirs()) throw IOException("Geçici yedek klasörü açılamadı.")
        try {
            val allowed = setOf("manifest.json", "exams.json", "study.json", "learning.zip")
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
                            "exams.json", "study.json" -> MAX_JSON
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
            if (extracted.keys != allowed) throw IOException("Tam yedek eksik bölüm içeriyor.")
            val manifest = JSONObject(extracted.getValue("manifest.json").readText())
            require(manifest.optString("schema") == SCHEMA) { "Bu dosya YKS-Takip tam yedeği değil." }
            require(manifest.optInt("version") == VERSION) { "Desteklenmeyen tam yedek sürümü." }

            val examResult = BackupManager.readFromUri(app.contentResolver, Uri.fromFile(extracted.getValue("exams.json")))
            val exams = (examResult as? BackupManager.ImportResult.Success)?.exams
                ?: throw IOException((examResult as? BackupManager.ImportResult.Error)?.message ?: "Deneme bölümü okunamadı.")
            val study = StudyBackupManager.parseJson(extracted.getValue("study.json").readText())
            val learning = LearningArchiveManager(app).prepareImport(Uri.fromFile(extracted.getValue("learning.zip")))
            extracted.getValue("learning.zip").delete()

            if (manifest.optInt("examCount", -1) != exams.size ||
                manifest.optInt("studyDayCount", -1) != study.days.size ||
                manifest.optInt("learningOutcomeCount", -1) != learning.outcomes.size ||
                manifest.optInt("learningQuestionCount", -1) != learning.questions.size ||
                manifest.optInt("photoCount", -1) != learning.photoCount
            ) {
                learning.discard()
                throw IOException("Yedek özeti ile içerik sayıları uyuşmuyor.")
            }
            return PreparedImport(exams, study.days, learning, stage)
        } catch (e: Exception) {
            stage.deleteRecursively()
            if (e is IOException) throw e
            throw IOException("Tam yedek doğrulanamadı: ${e.message}", e)
        }
    }

    /** Merge-only restore: existing local records win; no dataset is blindly overwritten. */
    fun merge(context: Context, prepared: PreparedImport): ImportResult {
        val app = context.applicationContext
        val examRepository = ExamRepository.getInstance(app)
        val studyRepository = StudyRepository(app)
        val previousExams = examRepository.getAllExams()
        val existingExamIds = previousExams.map { it.id }.toSet()
        val mergedExams = previousExams + prepared.exams.filterNot { it.id in existingExamIds }
        var studyChanges = 0
        var examChangesApplied = false
        try {
            studyChanges = studyRepository.importDays(prepared.studyDays, overwrite = false)
            examRepository.replaceAll(mergedExams)
            examChangesApplied = true
            val learningResult = LearningArchiveManager(app).merge(prepared.learning)
            return ImportResult(
                examsAdded = mergedExams.size - previousExams.size,
                studyDaysAdded = studyChanges,
                learning = learningResult
            )
        } catch (e: Exception) {
            if (examChangesApplied) examRepository.replaceAll(previousExams)
            if (studyChanges > 0 && studyRepository.canUndoLastImport()) {
                studyRepository.undoLastImport()
            }
            throw e
        } finally {
            prepared.discard()
        }
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, bytes: ByteArray) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(bytes)
        zip.closeEntry()
    }
}
