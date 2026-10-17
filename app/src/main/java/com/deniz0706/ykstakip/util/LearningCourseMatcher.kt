package com.deniz0706.ykstakip.util

/**
 * Maps a mock-exam section to learning-center subjects. TYT uses four combined
 * exam sections, while the learning catalog also has individual science, social
 * science and geometry subjects. AYT sections are already separate.
 */
object LearningCourseMatcher {
    fun matches(examType: String, examSubject: String, learningSubject: String): Boolean {
        if (examSubject == learningSubject) return true
        if (examType != "TYT") return false
        return when (examSubject) {
            "Fen Bilimleri" -> learningSubject in setOf("Fizik", "Kimya", "Biyoloji")
            "Sosyal Bilimler" -> learningSubject in setOf(
                "Tarih", "Coğrafya", "Felsefe", "Din Kültürü"
            )
            "Matematik" -> learningSubject == "Geometri"
            else -> false
        }
    }
}
