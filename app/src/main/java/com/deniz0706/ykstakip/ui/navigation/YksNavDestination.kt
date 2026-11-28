package com.deniz0706.ykstakip.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/** Typed navigation destinations for YKS Takip 4.0.
 *
 * Every reachable screen is a destination with optional typed arguments.
 * Arguments are declared here so Navigation Compose can deep-link and
 * restore state correctly after process recreation.
 */
sealed class YksNavDestination {

    object Home : YksNavDestination() {
        override val route = "home"
        override val arguments = emptyList<androidx.navigation.NamedNavArgument>()
    }

    object Exams : YksNavDestination() {
        override val route = "exams"
        override val arguments = emptyList<androidx.navigation.NamedNavArgument>()
    }

    data class ExamDetail(val examId: Long) : YksNavDestination() {
        override val route = "exams/{examId}"
        override val arguments =
            listOf(navArgument("examId") { type = NavType.LongType })
    }

    object Study : YksNavDestination() {
        override val route = "study"
        override val arguments = emptyList<androidx.navigation.NamedNavArgument>()
    }

    object Statistics : YksNavDestination() {
        override val route = "statistics"
        override val arguments = emptyList<androidx.navigation.NamedNavArgument>()
    }

    object AiCoach : YksNavDestination() {
        override val route = "ai-coach"
        override val arguments = emptyList<androidx.navigation.NamedNavArgument>()
    }

    object Settings : YksNavDestination() {
        override val route = "settings"
        override val arguments = emptyList<androidx.navigation.NamedNavArgument>()
    }

    abstract val route: String
    abstract val arguments: List<androidx.navigation.NamedNavArgument>
}

/** Bottom-nav level destinations (always visible in the shell). */
val BOTTOM_NAV_DESTINATIONS = listOf(
    YksNavDestination.Statistics,
    YksNavDestination.Home,
    YksNavDestination.Exams,
    YksNavDestination.Study,
    YksNavDestination.AiCoach,
    YksNavDestination.Settings,
)

/** Maps each destination to its tab label and monochrome symbol. */
fun destinationLabel(destination: YksNavDestination): String = when (destination) {
    YksNavDestination.Home -> "Ana Sayfa"
    YksNavDestination.Exams -> "Denemeler"
    YksNavDestination.Statistics -> "İstatistikler"
    YksNavDestination.Study -> "Çalışma"
    YksNavDestination.AiCoach -> "AI Koç"
    YksNavDestination.Settings -> "Ayarlar"
    is YksNavDestination.ExamDetail -> "Sınav Detayı"
}

fun destinationIcon(destination: YksNavDestination): String = when (destination) {
    YksNavDestination.Home -> "⌂"
    YksNavDestination.Exams -> "▤"
    YksNavDestination.Statistics -> "▥"
    YksNavDestination.Study -> "◷"
    YksNavDestination.AiCoach -> "✧"
    YksNavDestination.Settings -> "⚙"
    is YksNavDestination.ExamDetail -> "⌕"
}

/** Whether a destination should appear in the bottom navigation shell. */
fun isBottomNavDestination(destination: YksNavDestination): Boolean =
    destination !is YksNavDestination.ExamDetail
