package com.deniz0706.ykstakip.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YksNavDestinationTest {

    @Test
    fun bottomNavDestinationsContainAllMainTabs() {
        val routes = BOTTOM_NAV_DESTINATIONS.map { it.route }
        assertEquals(
            listOf("statistics", "home", "exams", "study", "ai-coach", "settings"),
            routes
        )
    }

    @Test
    fun examDetailIsNotBottomNavDestination() {
        assertFalse(isBottomNavDestination(YksNavDestination.ExamDetail(1L)))
    }

    @Test
    fun homeIsBottomNavDestination() {
        assertTrue(isBottomNavDestination(YksNavDestination.Home))
    }

    @Test
    fun destinationLabelsAreNonEmpty() {
        BOTTOM_NAV_DESTINATIONS.forEach { destination ->
            val label = destinationLabel(destination)
            assertTrue(label.isNotEmpty())
        }
        assertEquals("Sınav Detayı", destinationLabel(YksNavDestination.ExamDetail(42L)))
    }

    @Test
    fun destinationIconsAreNonEmpty() {
        BOTTOM_NAV_DESTINATIONS.forEach { destination ->
            val icon = destinationIcon(destination)
            assertTrue(icon.isNotEmpty())
        }
    }

    @Test
    fun examDetailRouteContainsParameter() {
        assertEquals("exams/{examId}", YksNavDestination.ExamDetail(-1L).route)
        assertTrue(YksNavDestination.ExamDetail(-1L).arguments.any { it.name == "examId" })
    }

    @Test
    fun homeRouteHasNoArguments() {
        assertEquals(0, YksNavDestination.Home.arguments.size)
    }

    @Test
    fun deepLinkPatternsAreValid() {
        assertTrue(YksNavDestination.Home.route.startsWith("home"))
        assertTrue(YksNavDestination.Exams.route.startsWith("exams"))
        assertTrue(YksNavDestination.ExamDetail(1L).route.contains("{examId}"))
    }
}
