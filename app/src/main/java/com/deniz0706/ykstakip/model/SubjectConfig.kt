package com.deniz0706.ykstakip.model

data class SubjectConfig(val name: String, val questionCount: Int)

object SubjectConfigs {
    val TYT_SUBJECTS = listOf(
        SubjectConfig("Türkçe", 40),
        SubjectConfig("Sosyal Bilimler", 20),
        SubjectConfig("Matematik", 40),
        SubjectConfig("Fen Bilimleri", 20)
    )
    val AYT_SUBJECTS = listOf(
        SubjectConfig("Matematik", 40),
        SubjectConfig("Fen Bilimleri", 40)
    )

    fun subjectsFor(type: ExamType): List<SubjectConfig> = when (type) {
        ExamType.TYT -> TYT_SUBJECTS
        ExamType.AYT -> AYT_SUBJECTS
    }
}
