package com.deniz0706.ykstakip.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException

/** Decodes and downsizes a selected image before sending it as a data URL to the vision model. */
object AiImageEncoder {
    const val MAX_ENCODED_BYTES = 2_500_000
    private const val MAX_DIMENSION = 1600

    fun encodeJpegDataUrl(context: Context, uri: Uri): String {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: throw IOException("Görsel açılamadı.")
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("Seçilen dosya geçerli bir görsel değil.")
        }

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_DIMENSION) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        var bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IOException("Görsel çözümlenemedi.")

        try {
            var quality = 82
            while (true) {
                val output = ByteArrayOutputStream()
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
                    throw IOException("Görsel sıkıştırılamadı.")
                }
                val bytes = output.toByteArray()
                if (bytes.size <= MAX_ENCODED_BYTES) {
                    return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                }

                if (quality > 52) {
                    quality -= 10
                    continue
                }
                if (maxOf(bitmap.width, bitmap.height) <= 640) {
                    throw IOException("Görsel 2,5 MB sınırının altına indirilemedi.")
                }
                val scale = 0.72f
                val resized = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
                if (resized !== bitmap) bitmap.recycle()
                bitmap = resized
                quality = 78
            }
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }
}
