package com.deniz0706.ykstakip.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningSubtopicsTest {
    @Test fun everyAccordionHasARealUnit() {
        val missing = LearningSubtopics.catalogUnitIds() - LearningSeed.starterIds
        assertTrue("Unlinked subtopic groups: $missing", missing.isEmpty())
    }

    @Test fun tytEnergyHasExactlyFiveSourceHeadings() {
        val energy = LearningSeed.starter.single {
            it.examType == "TYT" && it.subject == "Fizik" &&
                it.title == "İş, Güç ve Enerji"
        }
        assertEquals(listOf(
            "İş, Enerji ve Güç", "Mekanik Enerji",
            "Enerjinin Korunumu ve Dönüşümleri", "Verim"
        ), LearningSubtopics.forOutcome(energy))
    }

    @Test fun electrostaticsIsSeparateFromCircuitElectricity() {
        val science = LearningSeed.starter.filter {
            it.examType == "TYT" && it.subject == "Fizik"
        }
        assertTrue(science.any { it.title == "Elektrik" })
        assertTrue(!science.any { it.title == "Elektrostatik" })
    }

    @Test fun allTwentyTwoSubjectGroupsAreAvailable() {
        assertEquals(22, LearningKitapsecCatalog.groupedSubjects().size)
    }

    @Test fun specificSourceOutlineTotals() {
        val counts = LearningKitapsecCatalog.groupedSubjects()
        assertEquals(22, counts["TYT/Tarih"])
        assertEquals(15, counts["TYT/Türkçe"])
        assertEquals(12, counts["TYT/Fizik"])
        assertEquals(4, counts["AYT/Edebiyat"])
    }

    @Test fun nestedHeadingsAreDistinctWithinEachUnit() {
        for (unit in LearningSeed.starter) {
            val children = LearningSubtopics.forOutcome(unit)
            assertEquals("Repeated child under ${unit.title}",
                children.size, children.distinct().size)
        }
    }
}
