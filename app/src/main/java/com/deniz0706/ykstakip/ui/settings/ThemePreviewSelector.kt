package com.deniz0706.ykstakip.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class PreviewOption(
    val id: String, val title: String, val subtitle: String,
    val background: Color, val surface: Color, val text: Color, val accent: Color,
    val splitPreview: Boolean = false
)

/** Two-column, tappable theme gallery. Each preview reflects its real palette. */
@Composable
fun ThemePreviewSelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    // Observe UI configuration changes, but read the application-level system
    // configuration: Activity uiMode is deliberately overridden for Aurora/Dark.
    val activeConfiguration = LocalConfiguration.current
    val appContext = LocalContext.current.applicationContext
    val systemDark = (appContext.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val options = listOf(
        PreviewOption("light", "Açık", "Ferahlık",
            Color(0xFFF5F7FC), Color.White, Color(0xFF152238), Color(0xFF087E72)),
        PreviewOption("dark", "Koyu", "Grafit",
            Color(0xFF090B10), Color(0xFF15181F), Color(0xFFF5F6FA), Color(0xFFCBB7FF)),
        PreviewOption("aurora", "Aurora", "Mint & lila",
            Color(0xFF0B1220), Color(0xFF172337), Color(0xFFF3F8FF), Color(0xFF57D6C1)),
        PreviewOption("system", "Sistem", "Otomatik",
            if (systemDark) Color(0xFF090B10) else Color(0xFFF5F7FC),
            if (systemDark) Color(0xFF15181F) else Color.White,
            if (systemDark) Color(0xFFF5F6FA) else Color(0xFF152238),
            if (systemDark) Color(0xFFCBB7FF) else Color(0xFF087E72),
            splitPreview = true)
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { option ->
                    val isSelected = selected == option.id
                    Column(
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(19.dp))
                            .background(if (isSelected) scheme.primaryContainer else scheme.surfaceVariant)
                            .border(if (isSelected) 2.dp else 1.dp,
                                if (isSelected) scheme.primary else scheme.outline.copy(alpha = .5f),
                                RoundedCornerShape(19.dp))
                            .clickable { onSelect(option.id) }
                            .padding(9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.fillMaxWidth().height(94.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(option.background)
                        ) {
                            if (option.splitPreview) {
                                Box(Modifier.align(Alignment.CenterEnd)
                                    .fillMaxWidth(.5f).fillMaxHeight()
                                    .background(if (systemDark) Color(0xFFF5F7FC) else Color(0xFF090B10)))
                            }
                            Column(Modifier.fillMaxSize().padding(10.dp)) {
                                Text("YKS", color = option.text, fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold, lineHeight = 12.sp)
                                Spacer(Modifier.height(7.dp))
                                Box(Modifier.fillMaxWidth().height(13.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(option.surface)) {
                                    Box(Modifier.fillMaxWidth(.6f).fillMaxHeight()
                                        .background(option.accent.copy(alpha = .87f)))
                                }
                                Spacer(Modifier.height(8.dp))
                                Canvas(Modifier.fillMaxWidth().weight(1f)) {
                                    val line = Path().apply {
                                        moveTo(0f, size.height * .88f)
                                        lineTo(size.width * .2f, size.height * .56f)
                                        lineTo(size.width * .43f, size.height * .7f)
                                        lineTo(size.width * .72f, size.height * .22f)
                                        lineTo(size.width, size.height * .14f)
                                    }
                                    drawPath(line, option.accent, style = Stroke(2.3.dp.toPx()))
                                    drawCircle(option.accent, radius = 3.dp.toPx(),
                                        center = Offset(size.width, size.height * .14f))
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(if (isSelected) "✓ ${option.title}" else option.title,
                            color = scheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleSmall)
                        Text(option.subtitle, color = scheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
