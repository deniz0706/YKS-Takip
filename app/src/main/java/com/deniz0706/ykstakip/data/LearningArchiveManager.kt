package com.deniz0706.ykstakip.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.model.QuestionCard
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Learning records and their private photos. Independent of the existing exam JSON backup. */
class LearningArchiveManager(context: Context) {
    private val app = context.applicationContext
    private val repo = LearningRepository(app)
    private val photoDir = File(app.filesDir, "review_photos").apply { mkdirs() }
    private val maxPhoto = 20_000_000L
    private val maxAll = 250_000_000L
    private val maxMeta = 8_000_000L
    private val maxImages = 3000
    private val validFilename = Regex("[a-zA-Z0-9_-]{1,90}\\.(jpg|jpeg|png|webp)", RegexOption.IGNORE_CASE)

    class PreparedImport internal constructor(
        val outcomes: List<LearningOutcome>,
        val questions: List<QuestionCard>,
        internal val files: Map<String, File>,
        private val staging: File
    ) {
        val photoCount: Int get() = files.size
        fun discard() { staging.deleteRecursively() }
    }

    data class ExportResult(val outcomes: Int, val questions: Int, val photos: Int)
    data class ImportResult(val outcomesAdded: Int, val questionsAdded: Int, val photosAdded: Int)

    private fun validName(s: String): Boolean = validFilename.matches(s) && File(s).name == s

    private fun checkImage(f: File) {
        if (!f.isFile || f.length() !in 1L..maxPhoto) throw IOException("Eksik veya büyük fotoğraf: " + f.name)
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(f.absolutePath, opts)
        if (opts.outWidth <= 0 || opts.outHeight <= 0) throw IOException("Geçersiz fotoğraf: " + f.name)
    }

    private fun transfer(source: InputStream, sink: OutputStream, limit: Long): Long {
        val buffer = ByteArray(16 * 1024)
        var total = 0L
        while (true) {
            val n = source.read(buffer)
            if (n < 0) break
            if (n == 0) continue
            total += n
            if (total > limit) throw IOException("ZIP dosya boyutu sınırını aştı")
            sink.write(buffer, 0, n)
        }
        return total
    }

    /** Incomplete backups are rejected rather than omitting missing photos. */
    fun exportTo(uri: Uri): ExportResult {
        val outcomes = repo.outcomes()
        val questions = repo.questions()
        val photos = questions.flatMap { it.photos }.distinct()
        if (photos.size > maxImages) throw IOException("Fotoğraf sayısı sınırı aşıldı")
        var total = 0L
        photos.forEach { name ->
            if (!validName(name)) throw IOException("Geçersiz fotoğraf adı")
            val f = repo.photo(name)
            checkImage(f)
            total += f.length()
            if (total > maxAll) throw IOException("Fotoğraf arşivi çok büyük")
        }
        val bytes = JSONObject().apply {
            put("format", "yks-learning-archive")
            put("version", 1)
            put("date", LocalDate.now().toString())
            put("outcomes", JSONArray(outcomes.map { it.toJson() }))
            put("questions", JSONArray(questions.map { it.toJson() }))
        }.toString().toByteArray(Charsets.UTF_8)
        if (bytes.size > maxMeta) throw IOException("Kayıtlar çok büyük")
        val out = app.contentResolver.openOutputStream(uri, "w")
            ?: throw IOException("ZIP yazılamadı")
        out.use { stream ->
            ZipOutputStream(stream).use { zip ->
                zip.putNextEntry(ZipEntry("learning.json"))
                zip.write(bytes)
                zip.closeEntry()
                photos.forEach { name ->
                    zip.putNextEntry(ZipEntry("photos/" + name))
                    repo.photo(name).inputStream().use { it.copyTo(zip, 16 * 1024) }
                    zip.closeEntry()
                }
            }
        }
        return ExportResult(outcomes.size, questions.size, photos.size)
    }

    /** Unpack into a private staging folder with strict ZIP entry and size validation. */
    fun prepareImport(uri: Uri): PreparedImport {
        val stage = File(app.cacheDir, "learning-import-" + UUID.randomUUID())
        if (!stage.mkdirs()) throw IOException("Geçici klasör açılamadı")
        try {
            val files = linkedMapOf<String, File>()
            var metadata: ByteArray? = null
            var total = 0L
            var entries = 0
            val inStream = app.contentResolver.openInputStream(uri)
                ?: throw IOException("ZIP açılamadı")
            inStream.use { stream ->
                ZipInputStream(stream).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        entries++
                        if (entries > maxImages + 2) throw IOException("ZIP çok fazla dosya içeriyor")
                        val path = entry.name
                        if (entry.isDirectory && path == "photos/") {
                            zip.closeEntry()
                            continue
                        }
                        if (entry.isDirectory) throw IOException("Geçersiz ZIP klasörü")
                        when {
                            path == "learning.json" -> {
                                if (metadata != null) throw IOException("Tekrarlanan metadata")
                                val output = ByteArrayOutputStream()
                                total += transfer(zip, output, maxMeta)
                                metadata = output.toByteArray()
                            }
                            path.startsWith("photos/") -> {
                                val name = path.removePrefix("photos/")
                                if (!validName(name) || name in files || files.size >= maxImages)
                                    throw IOException("Geçersiz fotoğraf adı")
                                val f = File(stage, name)
                                f.outputStream().use { total += transfer(zip, it, maxPhoto) }
                                checkImage(f)
                                files[name] = f
                            }
                            else -> throw IOException("Öğrenme ZIP biçimi geçersiz")
                        }
                        if (total > maxAll + maxMeta) throw IOException("ZIP çok büyük")
                        zip.closeEntry()
                    }
                }
            }
            val raw = metadata ?: throw IOException("learning.json eksik")
            val json = JSONObject(String(raw, Charsets.UTF_8))
            if (json.optString("format") != "yks-learning-archive" || json.optInt("version") != 1)
                throw IOException("Desteklenmeyen öğrenme ZIP'i")
            val os = json.getJSONArray("outcomes")
            val qs = json.getJSONArray("questions")
            if (os.length() > 6000 || qs.length() > 10000) throw IOException("Çok fazla kayıt var")
            val outcomes = (0 until os.length()).map { LearningOutcome.fromJson(os.getJSONObject(it)) }
            val questions = (0 until qs.length()).map { QuestionCard.fromJson(qs.getJSONObject(it)) }
            val ids = outcomes.map { it.id }
            if (ids.any { it.isBlank() } || ids.distinct().size != ids.size ||
                outcomes.any { it.title.isBlank() } ||
                questions.any { it.id.isBlank() || it.outcomeId !in ids } ||
                questions.map { it.id }.distinct().size != questions.size ||
                questions.any { q -> q.photos.any { !files.containsKey(it) } })
                throw IOException("Bozuk soru veya kazanım bağlantıları")
            return PreparedImport(outcomes, questions, files, stage)
        } catch (e: Exception) {
            stage.deleteRecursively()
            throw e
        }
    }

    /**
     * Existing question IDs and modified local progress win. Importing a default
     * outcome's progress is allowed; current photo files are never overwritten.
     */
    fun merge(pending: PreparedImport): ImportResult {
        val localOutcomes = repo.outcomes()
        val localQuestions = repo.questions()
        val defaults = LearningSeed.starter.associateBy { it.id }
        val outcomes = LinkedHashMap(localOutcomes.associateBy { it.id })
        var newOutcomes = 0
        LearningSeed.mergeLegacyProgress(pending.outcomes).forEach { incoming ->
            val local = outcomes[incoming.id]
            if (local == null) {
                outcomes[incoming.id] = incoming
                newOutcomes++
            } else if (local == defaults[incoming.id] && incoming != local) {
                outcomes[incoming.id] = incoming
            }
        }
        val existing = localQuestions.map { it.id }.toSet()
        val additions = pending.questions.filter { it.id !in existing }.map { card ->
            card.copy(outcomeId = LearningSeed.topicIdFor(card.outcomeId))
        }
        val restoreMissing = localQuestions.flatMap { it.photos }.toSet().filter {
            !repo.photo(it).exists() && pending.files.containsKey(it)
        }
        val needed = additions.flatMap { it.photos }.toSet() + restoreMissing
        val mapped = mutableMapOf<String, String>()
        val created = mutableListOf<File>()
        try {
            needed.forEach { name ->
                val source = pending.files[name] ?: throw IOException("Fotoğraf eksik")
                checkImage(source)
                var destinationName = name
                if (repo.photo(name).exists()) {
                    // A collision cannot replace an existing photo's contents.
                    val extension = name.substringAfterLast('.')
                    do { destinationName = UUID.randomUUID().toString() + "." + extension }
                    while (repo.photo(destinationName).exists())
                }
                val destination = repo.photo(destinationName)
                val temp = File(photoDir, destinationName + ".incoming")
                try {
                    source.copyTo(temp, overwrite = false)
                    if (!temp.renameTo(destination)) throw IOException("Fotoğraf kopyalanamadı")
                } finally {
                    temp.delete()
                }
                created += destination
                mapped[name] = destinationName
            }
            val converted = additions.map { card ->
                card.copy(photos = card.photos.map { name ->
                    mapped[name] ?: throw IOException("Fotoğraf bağlantısı eksik")
                })
            }
            if (!repo.persistRestoredSnapshot(outcomes.values.toList(), localQuestions + converted))
                throw IOException("Öğrenme kayıtları kaydedilemedi")
            return ImportResult(newOutcomes, converted.size, created.size)
        } catch (e: Exception) {
            created.forEach { it.delete() }
            throw e
        }
    }
}
