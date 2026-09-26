package com.deniz0706.ykstakip.model

data class SubjectConfig(val name: String, val questionCount: Int)

enum class AytField {
    SAYISAL,
    ESIT_AGIRLIK,
    SOZEL
}

object SubjectConfigs {

    val TYT_SUBJECTS = listOf(
        SubjectConfig("Türkçe", 40),
        SubjectConfig("Sosyal Bilimler", 20),
        SubjectConfig("Matematik", 40),
        SubjectConfig("Fen Bilimleri", 20)
    )

    val AYT_SAYISAL = listOf(
        SubjectConfig("Matematik", 40),
        SubjectConfig("Fizik", 14),
        SubjectConfig("Kimya", 13),
        SubjectConfig("Biyoloji", 13)
    )

    val AYT_ESIT_AGIRLIK = listOf(
        SubjectConfig("Matematik", 40),
        SubjectConfig("Edebiyat", 24),
        SubjectConfig("Tarih-1", 10),
        SubjectConfig("Coğrafya-1", 6)
    )

    val AYT_SOZEL = listOf(
        SubjectConfig("Edebiyat", 24),
        SubjectConfig("Tarih-1", 10),
        SubjectConfig("Coğrafya-1", 6),
        SubjectConfig("Tarih-2", 11),
        SubjectConfig("Coğrafya-2", 11),
        SubjectConfig("Felsefe Grubu", 12),
        SubjectConfig("Din Kültürü / Ek Felsefe", 6)
    )

    fun subjectsFor(type: ExamType): List<SubjectConfig> = when (type) {
        ExamType.TYT -> TYT_SUBJECTS
        ExamType.AYT -> allAytSubjects
    }

    fun subjectsFor(field: AytField): List<SubjectConfig> = when (field) {
        AytField.SAYISAL -> AYT_SAYISAL
        AytField.ESIT_AGIRLIK -> AYT_ESIT_AGIRLIK
        AytField.SOZEL -> AYT_SOZEL
    }

    private val allAytSubjects: List<SubjectConfig> by lazy {
        (AYT_SAYISAL + AYT_ESIT_AGIRLIK + AYT_SOZEL).distinctBy { it.name }
    }
}
