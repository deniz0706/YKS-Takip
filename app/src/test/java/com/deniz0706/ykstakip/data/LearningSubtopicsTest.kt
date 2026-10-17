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
                it.title == "İş Güç Enerji"
        }
        assertEquals(listOf(
            "İş, Güç ve Enerji", "Mekanik Enerji",
            "Enerjinin Korunumu ve Enerji Dönüşümleri",
            "Verim", "Enerji Kaynakları"
        ), LearningSubtopics.forOutcome(energy))
    }

    @Test fun electrostaticsIsSeparateFromCircuitElectricity() {
        val science = LearningSeed.starter.filter {
            it.examType == "TYT" && it.subject == "Fizik"
        }
        assertTrue(science.any { it.title == "Elektrostatik" })
        assertTrue(science.any { it.title == "Elektrik" })
    }

    @Test fun nestedHeadingsAreDistinctWithinEachUnit() {
        for (unit in LearningSeed.starter) {
            val children = LearningSubtopics.forOutcome(unit)
            assertEquals("Repeated child under ${unit.title}",
                children.size, children.distinct().size)
        }
    }
}
