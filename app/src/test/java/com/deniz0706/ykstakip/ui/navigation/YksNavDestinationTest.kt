package com.deniz0706.ykstakip.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YksNavDestinationTest {

    @Test
    fun bottomNavDestinationsContainAllMainTabs() {
        val routes = BOTTOM_NAV_DESTINATIONS.map { it.route }
        assertEquals(5, routes.size)
        assertEquals("home", routes[0])
        assertEquals("exams", routes[1])
        assertEquals("study", routes[2])
        assertEquals("ai-coach", routes[3])
        assertEquals("settings", routes[4])
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
