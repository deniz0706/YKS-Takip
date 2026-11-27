package com.deniz0706.ykstakip.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AiMemoryBackupManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("ai_coach_long_term_memory", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test fun exportsOnlyConsentedMemories() {
        val prefs = context.getSharedPreferences("ai_coach_long_term_memory", Context.MODE_PRIVATE)
        val array = """[
            {"id":"m1","category":"Hedef","content":"Matematiğe odaklan","createdAt":1000,"updatedAt":2000,"source":"user_explicit"},
            {"id":"m2","category":"Not","content":"Otomatik yakalanan","createdAt":3000,"updatedAt":4000,"source":"auto_capture"},
            {"id":"m3","category":"İsim","content":"Benim adım Deniz","createdAt":5000,"updatedAt":6000,"source":"explicit_self_description"}
        ]"""
        prefs.edit().putString("memories_v1", array).apply()

        // Export: only user_explicit memory should appear
        val exportedJson = AiMemoryBackupManager.exportJson(context)

        // Clear store, then import exported data to verify round-trip
        prefs.edit().putString("memories_v1", "[]").apply()
        val parsed = AiMemoryBackupManager.importJson(context, exportedJson)

        assertEquals(1, parsed.memories.size)
        assertEquals("m1", parsed.memories[0].id)
        assertEquals("Hedef", parsed.memories[0].category)
        assertEquals("Matematiğe odaklan", parsed.memories[0].content)
    }

    @Test fun emptyMemoryStoreExportsAsEmpty() {
        val json = AiMemoryBackupManager.exportJson(context)
        val parsed = AiMemoryBackupManager.importJson(context, json)
        assertTrue(parsed.memories.isEmpty())
    }

    @Test fun importAddsNewMemoriesAndSkipsDuplicates() {
        val json = """{"schema":"yks_takip_ai_memory_backup","version":1,"memories":[
            {"id":"n1","category":"Hedef","content":"YKS'ye hazırlan","createdAt":1000,"updatedAt":2000,"source":"restored"},
            {"id":"n2","category":"Kaynak","content":"Deneme kağıdı çöz","createdAt":3000,"updatedAt":4000,"source":"restored"}
        ]}"""
        val parsed = AiMemoryBackupManager.importJson(context, json)
        assertEquals(2, parsed.memories.size)

        // Re-importing same data should add nothing new
        val parsed2 = AiMemoryBackupManager.importJson(context, json)
        assertEquals(0, parsed2.memories.size)
    }

    @Test fun missingSchemaIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            AiMemoryBackupManager.importJson(context, """{"version":1,"memories":[]}""")
        }
    }

    @Test fun unknownVersionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            AiMemoryBackupManager.importJson(context,
                """{"schema":"yks_takip_ai_memory_backup","version":99,"memories":[]}""")
        }
    }
}
