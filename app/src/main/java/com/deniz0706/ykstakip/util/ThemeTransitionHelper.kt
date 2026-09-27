package com.deniz0706.ykstakip.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.ViewGroup
import android.widget.ImageView

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

        activity.overridePendingTransition(0, 0)
    }

    fun applyPendingSnapshot(activity: Activity) {
        val bitmap = pendingSnapshot ?: return
        pendingSnapshot = null

        val decor = activity.window.decorView as? ViewGroup ?: return
        val overlay = ImageView(activity).apply {
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.FIT_XY
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        decor.addView(overlay)
        overlay.animate()
            .alpha(0f)
            .setDuration(280)
            .withEndAction {
                decor.removeView(overlay)
                if (!bitmap.isRecycled) bitmap.recycle()
            }
            .start()
    }
}
