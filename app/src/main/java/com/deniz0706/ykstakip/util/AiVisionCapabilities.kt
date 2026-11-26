package com.deniz0706.ykstakip.util

/** Only model explicitly verified for image input is enabled in the UI. */
object AiVisionCapabilities {
    const val VERIFIED_MODEL = "mistralai/mistral-large-4-1t-a52b"

    fun supportsImages(model: String): Boolean =
        model.trim().equals(VERIFIED_MODEL, ignoreCase = true)
}
