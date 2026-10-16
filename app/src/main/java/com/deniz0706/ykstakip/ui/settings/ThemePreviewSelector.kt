package com.deniz0706.ykstakip.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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

/** Lightweight live theme previews; renders miniature dashboards, not screenshots. */
@Composable
fun ThemePreviewSelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val systemDark = isSystemInDarkTheme()
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        listOf("light" to "Açık", "dark" to "Koyu", "system" to "Sistem")
            .forEach { (key, label) ->
                val darkPreview = when (key) {
                    "light" -> false
                    "dark" -> true
                    else -> systemDark
                }
                val background = if (darkPreview) Color(0xFF101B2D) else Color(0xFFF4F6FB)
                val surface = if (darkPreview) Color(0xFF20344C) else Color.White
                val foreground = if (darkPreview) Color(0xFFEAF3FF) else Color(0xFF14243D)
                val accent = if (darkPreview) Color(0xFF69DAC6) else Color(0xFF087E72)
                val active = selected == key
                Column(
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (active) scheme.primaryContainer else scheme.surfaceVariant)
                        .border(
                            if (active) 2.dp else 1.dp,
                            if (active) scheme.primary else scheme.outline.copy(alpha = .45f),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { onSelect(key) }
                        .padding(7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        Modifier.fillMaxWidth().height(89.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(background)
                            .padding(7.dp)
                    ) {
                        Text("YKS", color = foreground, fontWeight = FontWeight.Bold,
                            fontSize = 9.sp, lineHeight = 12.sp)
                        Spacer(Modifier.height(5.dp))
                        Box(Modifier.fillMaxWidth().height(11.dp)
                            .clip(RoundedCornerShape(3.dp)).background(surface)) {
                            Box(Modifier.fillMaxWidth(.62f).fillMaxHeight()
                                .background(accent.copy(alpha = .9f)))
                        }
                        Spacer(Modifier.height(5.dp))
                        Canvas(Modifier.fillMaxWidth().weight(1f)) {
                            val line = Path().apply {
                                moveTo(0f, size.height * .83f)
                                lineTo(size.width * .23f, size.height * .55f)
                                lineTo(size.width * .46f, size.height * .66f)
                                lineTo(size.width * .70f, size.height * .25f)
                                lineTo(size.width, size.height * .18f)
                            }
                            drawPath(line, accent, style = Stroke(2.dp.toPx()))
                            drawCircle(accent, radius = 2.6.dp.toPx(),
                                center = Offset(size.width, size.height * .18f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(if (active) "✓ $label" else label,
                        color = scheme.onSurface,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelMedium)
                }
            }
    }
}
