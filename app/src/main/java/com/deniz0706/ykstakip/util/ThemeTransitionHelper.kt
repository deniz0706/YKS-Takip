package com.deniz0706.ykstakip.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.ViewGroup
import android.widget.ImageView

/**
 * setDefaultNightMode() bir Activity'yi otomatik recreate() ediyor, bu da
 * varsayılan olarak sert bir "flaş" gibi hissettiriyor. Bunu yumuşatmak için:
 * 1) Tema değişmeden HEMEN ÖNCE ekranın anlık görüntüsünü (bitmap) alıyoruz.
 * 2) Activity yeniden oluşup yeni tema çizildikten sonra, bu eski görüntüyü
 *    üstüne kaplıyoruz ve yavaşça (fade) kayboluyor — göz, eski temadan yeni
 *    temaya yumuşak bir geçiş görüyor.
 */
object ThemeTransitionHelper {

    private var pendingSnapshot: Bitmap? = null

    fun captureSnapshot(activity: Activity) {
        val decor = activity.window.decorView
        pendingSnapshot = try {
            if (decor.width <= 0 || decor.height <= 0) return
            val bitmap = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            decor.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun applyPendingSnapshot(activity: Activity) {
        val bitmap = pendingSnapshot ?: return
        pendingSnapshot = null

        val decor = activity.window.decorView as? ViewGroup ?: return
        val overlay = ImageView(activity).apply {
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.FIT_XY
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.
