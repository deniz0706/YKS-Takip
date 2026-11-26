package com.deniz0706.ykstakip.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiMemoryExtractorTest {

    @Test
    fun extractsExpectedFacts() {
        val cases = listOf(
            "Benim adım Mehmet Deniz." to AiMemoryExtractor.Candidate("İsim", "Mehmet Deniz"),
            "Adım Deniz." to AiMemoryExtractor.Candidate("İsim", "Deniz"),
            "İsmim Mehmet." to AiMemoryExtractor.Candidate("İsim", "Mehmet"),
            "Hedefim tıp fakültesi kazanmak." to AiMemoryExtractor.Candidate("Hedef", "tıp fakültesi kazanmak"),
            "Hedefim bilgisayar mühendisliği okumak." to AiMemoryExtractor.Candidate("Hedef", "bilgisayar mühendisliği okumak"),
            "Tercihim kısa ve doğrudan cevaplar." to AiMemoryExtractor.Candidate("Tercih", "kısa ve doğrudan cevaplar"),
            "Tercihim örneklerle anlatım." to AiMemoryExtractor.Candidate("Tercih", "örneklerle anlatım"),
            "Bundan sonra bana kısa cevap ver." to AiMemoryExtractor.Candidate("Tercih", "Bundan sonra bana kısa cevap ver"),
            "Kaynağım 3D TYT Matematik." to AiMemoryExtractor.Candidate("Kaynak", "3D TYT Matematik")
        )

        cases.forEach { (input, expected) ->
            assertEquals(input, listOf(expected), AiMemoryExtractor.extract(input))
        }
    }

    @Test
    fun rejectsTwentyFalsePositives() {
        val rejected = listOf(
            "adım deniz",
            "Adım Adım plan yap",
            "Bir sonraki adım ne olmalı?",
            "Hedefim için çalışıyorum.",
            "Hedefim yok.",
            "Bundan sonra ne yapmalıyım?",
            "Ne zaman çalışayım?",
            "Kaç saat çalışmalıyım?",
            "Kim bana yardım edecek?",
            "Neden böyle oldu?",
            "Nasıl çalışmalıyım?",
            "Hangi kaynağı almalıyım?",
            "Bundan sonra yarın sınav var, cevap ver.",
            "Bana kısa cevap vermen iyi olur.",
            "Kısa cevap ver, olur mu?",
            "Tercihim ne olmalı?",
            "Kaynak olarak ne çözmeliyim?",
            "Benim adım adım ilerlemem gerekiyor.",
            "Hedefim için ne yapmalıyım?",
            "Bundan sonra ne zaman konu anlatacaksın?"
        )

        rejected.forEach { input ->
            assertTrue("Yanlış pozitif: $input", AiMemoryExtractor.extract(input).isEmpty())
        }
    }

    @Test
    fun extractsMultipleIndependentSentences() {
        assertEquals(
            listOf(
                AiMemoryExtractor.Candidate("İsim", "Mehmet Deniz"),
                AiMemoryExtractor.Candidate("Hedef", "tıp fakültesi kazanmak")
            ),
            AiMemoryExtractor.extract("Benim adım Mehmet Deniz. Hedefim tıp fakültesi kazanmak.")
        )
    }

    @Test
    fun questionWordFilterIsIntentionallyConservative() {
        // This is an accepted false-negative tradeoff: a question word anywhere rejects the sentence.
        assertTrue(AiMemoryExtractor.extract("Tercihim ne olursa olsun kısa cevaplar.").isEmpty())
    }
}
