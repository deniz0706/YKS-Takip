package com.deniz0706.ykstakip.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared visual primitives: no shadows, images or per-frame allocations required. */
@Composable
fun DashboardPanel(
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = MaterialTheme.colorScheme
    val shape = remember { RoundedCornerShape(26.dp) }
    val fill = androidx.compose.runtime.remember(c.surface, c.surfaceVariant, accent) {
        if (accent) Brush.linearGradient(listOf(c.surfaceVariant, c.surface, c.surface))
        else Brush.linearGradient(listOf(c.surface, c.surface))
    }
    Column(
        modifier.background(fill, shape)
            .border(1.dp, c.outline.copy(alpha = .38f), shape)
            .padding(18.dp),
        content = { CompositionLocalProvider(LocalContentColor provides c.onSurface) { content() } }
    )
}

@Composable
fun DashboardMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val c = MaterialTheme.colorScheme
    DashboardPanel(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
            color = c.onSurfaceVariant, letterSpacing = 1.sp)
        Spacer(Modifier.height(9.dp))
        Text(value, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold,
            color = color, maxLines = 1)
        if (detail != null) {
            Spacer(Modifier.height(6.dp))
            Text(detail, style = MaterialTheme.typography.labelSmall,
                color = c.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun DashboardProgress(
    fraction: Float,
    modifier: Modifier = Modifier,
    foreground: Color = MaterialTheme.colorScheme.primary
) {
    val c = MaterialTheme.colorScheme
    val progress = animateFloatAsState(fraction.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 350f), label = "goal-progress")
    Box(modifier.height(9.dp).clip(RoundedCornerShape(100.dp))
        .background(c.surfaceVariant)) {
        Box(Modifier.fillMaxWidth(progress.value).fillMaxHeight()
            .clip(RoundedCornerShape(100.dp)).background(foreground))
    }
}

@Composable
fun DashboardRing(
    fraction: Float,
    center: String,
    modifier: Modifier = Modifier,
    caption: String = ""
) {
    val c = MaterialTheme.colorScheme
    val progress = animateFloatAsState(fraction.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 250f), label = "ring-progress")
    Box(modifier.size(106.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val width = 9.dp.toPx()
            drawArc(c.outline.copy(alpha = .36f), -90f, 360f, false,
                style = Stroke(width, cap = StrokeCap.Round))
            drawArc(c.primary, -90f, 360f * progress.value, false,
                style = Stroke(width, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(center, fontSize = 21.sp, fontWeight = FontWeight.Bold,
                color = c.onSurface)
            if (caption.isNotEmpty()) Text(caption, style = MaterialTheme.typography.labelSmall,
                color = c.onSurfaceVariant)
        }
    }
}
