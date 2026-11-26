package com.deniz0706.ykstakip.model

/** Android source-compatibility aliases for shared exam configuration values. */
typealias SubjectConfig = com.deniz0706.ykstakip.domain.SubjectConfig
typealias AytField = com.deniz0706.ykstakip.domain.AytField

/** Keeps the original Android object API while delegating catalog data to commonMain. */
object SubjectConfigs {
    val TYT_SUBJECTS get() = com.deniz0706.ykstakip.domain.SubjectConfigs.TYT_SUBJECTS
    val AYT_SAYISAL get() = com.deniz0706.ykstakip.domain.SubjectConfigs.AYT_SAYISAL
    val AYT_ESIT_AGIRLIK get() = com.deniz0706.ykstakip.domain.SubjectConfigs.AYT_ESIT_AGIRLIK
    val AYT_SOZEL get() = com.deniz0706.ykstakip.domain.SubjectConfigs.AYT_SOZEL

    fun subjectsFor(type: ExamType): List<SubjectConfig> =
        com.deniz0706.ykstakip.domain.SubjectConfigs.subjectsFor(type)

    fun subjectsFor(field: AytField): List<SubjectConfig> =
        com.deniz0706.ykstakip.domain.SubjectConfigs.subjectsFor(field)
}
