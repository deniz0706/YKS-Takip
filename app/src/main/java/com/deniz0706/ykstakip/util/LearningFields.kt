package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField

/** Only show the subjects belonging to the selected AYT track. */
object LearningFields {
    fun includes(exam: String, subject: String, field: AytField): Boolean {
        if (subject == "Eski Kayıtlar") return true
        if (exam == "TYT") return true
        if (exam != "AYT") return false
        return when (field) {
            AytField.SAYISAL -> subject in setOf(
                "Matematik", "Geometri", "Fizik", "Kimya", "Biyoloji"
            )
            AytField.ESIT_AGIRLIK -> subject in setOf(
                "Matematik", "Geometri", "Edebiyat", "Tarih-1", "Coğrafya-1"
            )
            AytField.SOZEL -> subject in setOf(
                "Edebiyat", "Tarih-1", "Coğrafya-1", "Tarih-2", "Coğrafya-2",
                "Felsefe Grubu", "Din Kültürü / Ek Felsefe"
            )
        }
    }
}
