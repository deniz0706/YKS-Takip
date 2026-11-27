package com.deniz0706.ykstakip.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomNavigation
import androidx.compose.material3.BottomNavigationItem
import androidx.compose.material3.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.ui.theme.YksTokens
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalMaterialApi::class)
sealed object Screen {
    object Home : Screen()
    object Exams : Screen()
    object Study : Screen()
    object AiCoach : Screen()
    object Settings : Screen()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun YksNavigation(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    content: @Composable (Screen) -> Unit
) {
    val c = MaterialTheme.colorScheme
    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = c.surfaceContainer
            ) {
                val items = listOf(
                    Pair(Screen.Home, "Ana Sayfa"),
                    Pair(Screen.Exams, "Denemeler"),
                    Pair(Screen.Study, "Çalışma"),
                    Pair(Screen.AiCoach, "AI Koç"),
                    Pair(Screen.Settings, "Ayarlar")
                )
                items.forEachIndexed { _, (screen, label) ->
                    val selected = currentScreen == screen
                    BottomNavigationItem(
                        selected = selected,
                        onClick = { onScreenChange(screen) },
                        icon = {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (screen) {
                                        Screen.Home -> "🏠"
                                        Screen.Exams -> "📝"
                                        Screen.Study -> "📚"
                                        Screen.AiCoach -> "🤖"
                                        Screen.Settings -> "⚙️"
                                    },
                                    fontSize = 20.sp,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        label = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
                        colors = androidx.compose.material3.BottomNavigationDefaults.colors(
                            unselectedItemColor = c.onSurfaceVariant.copy(alpha = 0.6f),
                            selectedItemColor = c.primary
                        )
                    )
                }
            }
        }
    ) { padding ->
        content(currentScreen)
            .modifier(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()))
    }
}
