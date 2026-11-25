package com.deniz0706.ykstakip.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningSubtopicsTest {
    @Test fun curriculumContainsOnlyTheSuppliedExamSubjects() {
        assertEquals(15, LearningKitapsecCatalog.groupedSubjects().size)
        assertEquals(
            setOf("TYT", "AYT"),
            LearningSeed.starter.map { it.examType }.toSet()
        )
    }

    @Test fun turkishTopicsUseTheSuppliedUnitGroups() {
        val turkish = LearningSeed.starter.filter {
            it.examType == "TYT" && it.subject == "Türkçe"
        }
        assertEquals(
            listOf(
                "Anlam Bilgisi",
                "Ses ve Yapı Bilgisi",
                "Yazım ve Noktalama",
                "Sözcük Türleri",
                "Cümle Bilgisi"
            ),
            turkish.map { it.topic }.distinct()
        )
        assertTrue(turkish.any {
            it.topic == "Anlam Bilgisi" && it.title == "Paragrafta Anlam ve Yapı"
        })
    }

    @Test fun allTopicsAreSingleLevelCheckboxes() {
        assertTrue(LearningSeed.starter.all {
            LearningSubtopics.forOutcome(it).isEmpty()
        })
        assertTrue(LearningSubtopics.catalogUnitIds().isEmpty())
    }

    @Test fun topicIdsAreUniqueAndGroupNamesAreNotUsedAsTopicRows() {
        val ids = LearningSeed.starter.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        assertTrue(LearningSeed.starter.none {
            it.title == it.topic && it.topic.isNotBlank()
        })
    }

    @Test fun aytScienceAndMathMatchTheProvidedOutline() {
        val ayt = LearningSeed.starter.filter { it.examType == "AYT" }
        assertEquals(
            setOf("Matematik", "Geometri", "Fizik", "Kimya", "Biyoloji"),
            ayt.map { it.subject }.toSet()
        )
        assertTrue(ayt.any {
            it.subject == "Fizik" && it.topic == "Modern Fizik" &&
                it.title == "Siyah Cisim Işıması ve Fotoelektrik Olay"
        })
        assertTrue(ayt.any {
            it.subject == "Biyoloji" && it.topic == "İnsan Fizyolojisi (Sistemler)" &&
                it.title == "Dolaşım ve Bağışıklık Sistemi"
        })
    }
}
