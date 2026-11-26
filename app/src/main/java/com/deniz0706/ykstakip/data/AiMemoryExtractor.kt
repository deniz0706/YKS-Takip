package com.deniz0706.ykstakip.data

/**
 * Conservative, side-effect-free extraction of durable user facts.
 * Not connected to chat capture until JVM and device validation are complete.
 */
internal object AiMemoryExtractor {
    data class Candidate(val category: String, val content: String)

    private val questionWords = Regex(
        """(?iu)\b(ne|neden|nasıl|niçin|niye|hangi|kaç|kim|nerede|ne zaman)\b|\b(mı|mi|mu|mü)\b"""
    )
    private val namePatterns = listOf(
        Regex("""(?iu)^\s*benim\s+adım\s+((?-i:[\p{Lu}][\p{Ll}]+(?:\s+[\p{Lu}][\p{Ll}]+){0,2}))\s*[.!]?$"""),
        Regex("""(?iu)^\s*ismim\s+((?-i:[\p{Lu}][\p{Ll}]+(?:\s+[\p{Lu}][\p{Ll}]+){0,2}))\s*[.!]?$"""),
        Regex("""(?iu)^\s*adım\s+((?-i:[\p{Lu}][\p{Ll}]+(?:\s+[\p{Lu}][\p{Ll}]+){0,2}))\s*[.!]?$""")
    )
    private val goalPattern = Regex("""(?iu)^\s*hedefim\s+(?!için\b)(.{4,120}?)\s*[.!]?$""")
    private val preferencePattern = Regex("""(?iu)^\s*tercihim\s+(.{3,120}?)\s*[.!]?$""")
    // Intentionally strict: a short imperative must be at the very end.
    private val futurePreferencePattern = Regex(
        """(?iu)^\s*bundan\s+sonra\s+([^,.!?]{2,100}?)\s+(anlat|yaz|ver|kullan|konuş|hatırlat)\s*[.!]?$"""
    )
    private val resourcePatterns = listOf(
        Regex("""(?iu)^\s*(kaynağım|kaynak olarak|çözdüğüm kaynak)\s+(.{2,120}?)\s*[.!]?$"""),
        Regex("""(?iu)^\s*(kitabım|kitap olarak)\s+(.{2,120}?)\s*[.!]?$""")
    )

    fun extract(message: String): List<Candidate> =
        message.split(Regex("""(?<=[.!?])\s+|\n+"""))
            .mapNotNull { raw ->
                val sentence = raw.trim()
                if (sentence.isEmpty() || '?' in sentence || questionWords.containsMatchIn(sentence)) {
                    return@mapNotNull null
                }
                namePatterns.firstNotNullOfOrNull { pattern ->
                    pattern.matchEntire(sentence)?.groupValues?.getOrNull(1)
                        ?.let { Candidate("İsim", it.trim()) }
                } ?: goalPattern.matchEntire(sentence)?.groupValues?.getOrNull(1)
                    ?.let { Candidate("Hedef", it.trim()) }
                    ?: preferencePattern.matchEntire(sentence)?.groupValues?.getOrNull(1)
                        ?.let { Candidate("Tercih", it.trim()) }
                    ?: futurePreferencePattern.matchEntire(sentence)?.let {
                        Candidate("Tercih", "Bundan sonra " + it.groupValues[1].trim() + " " + it.groupValues[2].trim())
                    }
                    ?: resourcePatterns.firstNotNullOfOrNull { pattern ->
                        pattern.matchEntire(sentence)?.groupValues?.getOrNull(2)
                            ?.let { Candidate("Kaynak", it.trim()) }
                    }
            }
            .filter { it.content.length >= 2 }
            .distinctBy { it.category to it.content.lowercase(java.util.Locale.forLanguageTag("tr-TR")) }
}
