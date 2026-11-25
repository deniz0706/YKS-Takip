package com.deniz0706.ykstakip.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts user-provided API credentials with a non-exportable Android Keystore key. */
object AiSecretStore {
    private const val PREFS = "ai_secret_store"
    private const val KEY_API = "provider_api_key"
    private const val KEY_ALIAS = "yks_takip_ai_secret_v1"

    fun hasApiKey(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(KEY_API)

    fun saveApiKey(context: Context, value: String) {
        val clean = value.trim()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (clean.isEmpty()) { prefs.edit().remove(KEY_API).apply(); return }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val packed = cipher.iv + cipher.doFinal(clean.toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString(KEY_API, Base64.encodeToString(packed, Base64.NO_WRAP)).commit())
    }

    fun readApiKey(context: Context): String? {
        val encoded = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_API, null) ?: return null
        val packed = Base64.decode(encoded, Base64.NO_WRAP)
        require(packed.size > 12) { "Kayıtlı API anahtarı okunamadı." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, packed.copyOfRange(0, 12)))
        return cipher.doFinal(packed.copyOfRange(12, packed.size)).toString(Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }
}