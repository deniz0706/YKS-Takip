package com.deniz0706.ykstakip.data

import android.content.ContentResolver
import android.net.Uri
import com.deniz0706.ykstakip.model.Exam
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

object BackupManager {
    fun exportToJson(exams: List<Exam>): String = JSONArray(exams.map { it.toJson() }).toString(2)

    fun writeToUri(resolver: ContentResolver, uri: Uri, exams: List<Exam>) {
        resolver.openOutputStream(uri)?.use { it.write(exportToJson(exams).toByteArray()) }
    }

    sealed class ImportResult {
        data class Success(val exams: List<Exam>) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    fun readFromUri(resolver: ContentResolver, uri: Uri): ImportResult {
        return try {
            val text = resolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it)).readText() }
                ?: return ImportResult.Error("Dosya okunamadı.")
            val array = JSONArray(text)
            val exams = (0 until array.length()).map { Exam.fromJson(array.getJSONObject(it)) }
            ImportResult.Success(exams)
        } catch (e: Exception) {
            ImportResult.Error("Geçersiz ya da bozuk yedek dosyası.")
        }
    }
}
