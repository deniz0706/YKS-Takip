package com.deniz0706.ykstakip.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.ui.theme.YksSpacing
import com.deniz0706.ykstakip.ui.theme.YksShape
import com.deniz0706.ykstakip.ui.theme.YksMotion

/** Shared, borderless visual primitives used by every main screen. */
@Composable
fun DashboardPageHeader(eyebrow: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth().padding(top = 5.dp, bottom = 3.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(eyebrow.uppercase(), color = c.primary, style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.7.sp, fontWeight = FontWeight.Bold)
        Text(title, color = c.onSurface, fontSize = 31.sp, lineHeight = 36.sp,
            fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp)
        Text(subtitle, color = c.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
    }
}

@Composable
fun DashboardPanel(modifier: Modifier = Modifier, accent: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val c = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(YksShape.panel)
    val fill = if (accent) Brush.linearGradient(listOf(c.primary.copy(alpha = 0.075f), c.surfaceContainerHigh, c.surfaceContainerHigh))
        else Brush.linearGradient(listOf(c.surfaceContainerLow, c.surfaceContainerLow))
    Column(modifier.background(fill, shape).padding(YksSpacing.cardContent),
        content = { CompositionLocalProvider(LocalContentColor provides c.onSurface) { content() } })
}

@Composable
fun DashboardMetric(label: String, value: String, modifier: Modifier = Modifier, detail: String? = null,
    color: Color = MaterialTheme.colorScheme.onSurface) {
    val c = MaterialTheme.colorScheme
    DashboardPanel(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant,
            letterSpacing = 1.1.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(9.dp))
        Text(value, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp, color = color, maxLines = 1)
        if (detail != null) {
            Spacer(Modifier.height(6.dp))
            Text(detail, style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
fun DashboardProgress(fraction: Float, modifier: Modifier = Modifier, foreground: Color = MaterialTheme.colorScheme.primary) {
    val c = MaterialTheme.colorScheme
    val progress = remember { Animatable(0f) }
    val target = fraction.coerceIn(0f, 1f)
    LaunchedEffect(target) { progress.animateTo(target, tween(YksMotion.progressMs)) }
    Box(modifier.height(8.dp).drawWithCache {
        val radius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
        val track = c.surfaceContainerHighest
        onDrawBehind {
            drawRoundRect(track, cornerRadius = radius)
            if (progress.value > 0f) drawRoundRect(foreground,
                size = androidx.compose.ui.geometry.Size(size.width * progress.value, size.height), cornerRadius = radius)
        }
    })
}

@Composable
fun DashboardRing(fraction: Float, center: String, modifier: Modifier = Modifier, caption: String = "",
    diameter: androidx.compose.ui.unit.Dp = 106.dp) {
    val c = MaterialTheme.colorScheme
    val progress = remember { Animatable(0f) }
    val target = fraction.coerceIn(0f, 1f)
    LaunchedEffect(target) { progress.animateTo(target, tween(YksMotion.emphasisMs + YksMotion.progressMs)) }
    val track = c.surfaceContainerHighest
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().drawWithCache {
            val style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
            onDrawBehind {
                drawArc(track, -90f, 360f, false, style = style)
                drawArc(c.primary, -90f, 360f * progress.value, false, style = style)
            }
        })
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(center, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
            if (caption.isNotEmpty()) Text(caption, style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant)
        }
    }
}
