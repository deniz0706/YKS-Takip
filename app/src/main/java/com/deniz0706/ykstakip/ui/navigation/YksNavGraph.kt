package com.deniz0706.ykstakip.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.ui.theme.YksTokens

sealed class Screen {
    object Home : Screen()
    object Exams : Screen()
    object Study : Screen()
    object AiCoach : Screen()
    object Settings : Screen()
}

data class NavItem(val screen: Screen, val label: String, val icon: String)

@Composable
fun YksNavigation(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    content: @Composable (Screen) -> Unit
) {
    val c = MaterialTheme.colorScheme
    Scaffold(
        bottomBar = {
            Surface(
                color = c.surfaceContainer,
                modifier = Modifier.height(56.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = listOf(
                        NavItem(Screen.Home, "Ana Sayfa", "🏠"),
                        NavItem(Screen.Exams, "Denemeler", "📝"),
                        NavItem(Screen.Study, "Çalışma", "📚"),
                        NavItem(Screen.AiCoach, "AI Koç", "🤖"),
                        NavItem(Screen.Settings, "Ayarlar", "⚙️")
                    )
                    items.forEach { item ->
                        val selected = currentScreen == item.screen
                        androidx.compose.material3.IconButton(
                            onClick = { onScreenChange(item.screen) },
                            modifier = Modifier.size(48.dp),
                            enabled = true
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = item.icon,
                                    fontSize = 20.sp,
                                    color = if (selected) c.primary else c.onSurfaceVariant
                                )
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) c.primary else c.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())
        ) {
            content(currentScreen)
        }
    }
}
