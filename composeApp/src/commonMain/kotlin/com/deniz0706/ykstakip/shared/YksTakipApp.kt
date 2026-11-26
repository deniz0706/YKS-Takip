package com.deniz0706.ykstakip.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val GraphiteBackground = Color(0xFF0F1115)
private val GraphiteSurface = Color(0xFF181B20)
private val GraphiteAccent = Color(0xFFB8C7E3)

@Composable
fun YksTakipApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = GraphiteAccent,
            background = GraphiteBackground,
            surface = GraphiteSurface,
            onBackground = Color(0xFFE8EAF0),
            onSurface = Color(0xFFE8EAF0),
            onSurfaceVariant = Color(0xFFA5AAB5),
            outline = Color(0xFF343943)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = GraphiteBackground) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("YKS TAKİP", style = MaterialTheme.typography.labelLarge, color = GraphiteAccent)
                Text("4.0", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                SharedExamTrendCard(
                    examType = "TYT",
                    latestNet = null,
                    change = null,
                    goal = null,
                    goalProgressLabel = null,
                    trend = emptyList(),
                    trendCount = 0,
                    showTarget = false,
                    onShowTargetChange = {}
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Ortak arayüz kabuğu. iOS veri ve gezinme adaptörleri henüz bağlanmadı.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
