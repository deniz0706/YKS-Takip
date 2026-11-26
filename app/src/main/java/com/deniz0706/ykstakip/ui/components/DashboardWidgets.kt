package com.deniz0706.ykstakip.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/** Shared visual primitives: no shadows, images or per-frame allocations required. */

@Composable
fun DashboardPageHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    val shape = remember { RoundedCornerShape(YksShape.panel) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = c.primaryContainer
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.width(4.dp).height(54.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(c.onPrimaryContainer.copy(alpha = .68f))
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    eyebrow.uppercase(),
                    color = c.onPrimaryContainer.copy(alpha = .76f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.25.sp
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    title,
                    color = c.onPrimaryContainer,
                    fontSize = 27.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    subtitle,
                    color = c.onPrimaryContainer.copy(alpha = .78f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun DashboardPanel(
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = MaterialTheme.colorScheme
    val shape = remember { RoundedCornerShape(YksShape.panel) }
    val fill = androidx.compose.runtime.remember(
        c.surface, c.surfaceVariant, c.primary, accent
    ) {
        if (accent) Brush.linearGradient(
            listOf(c.primary.copy(alpha = .11f), c.surfaceVariant.copy(alpha = .72f), c.surface)
        ) else Brush.linearGradient(listOf(c.surface, c.surface))
    }
    val borderColor = if (accent) c.primary.copy(alpha = .30f)
        else c.outline.copy(alpha = .38f)
    Column(
        modifier.background(fill, shape)
            .border(1.dp, borderColor, shape)
            .padding(YksSpacing.cardContent),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(7.dp).background(color.copy(alpha = .85f), RoundedCornerShape(50))
            )
            Spacer(Modifier.width(7.dp))
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
                color = c.onSurfaceVariant, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(11.dp))
        Text(value, fontSize = 28.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold,
            color = color, maxLines = 1)
        Box(
            Modifier.padding(top = 10.dp).width(30.dp).height(3.dp)
                .clip(RoundedCornerShape(2.dp)).background(color.copy(alpha = .72f))
        )
        if (detail != null) {
            Spacer(Modifier.height(7.dp))
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
    val progress = remember { Animatable(0f) }
    val target = fraction.coerceIn(0f, 1f)
    LaunchedEffect(target) { progress.animateTo(target, tween(YksMotion.progressMs)) }
    Box(modifier.height(9.dp).drawWithCache {
        val radius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
        val track = c.surfaceVariant
        onDrawBehind {
            drawRoundRect(track, cornerRadius = radius)
            if (progress.value > 0f) {
                drawRoundRect(foreground,
                    size = androidx.compose.ui.geometry.Size(size.width * progress.value, size.height),
                    cornerRadius = radius)
            }
        }
    })
}

@Composable
fun DashboardRing(
    fraction: Float,
    center: String,
    modifier: Modifier = Modifier,
    caption: String = "",
    diameter: androidx.compose.ui.unit.Dp = 106.dp
) {
    val c = MaterialTheme.colorScheme
    val progress = remember { Animatable(0f) }
    val target = fraction.coerceIn(0f, 1f)
    LaunchedEffect(target) { progress.animateTo(target, tween(YksMotion.emphasisMs + YksMotion.progressMs)) }
    val track = c.outline.copy(alpha = .36f)
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().drawWithCache {
            val style = Stroke(9.dp.toPx(), cap = StrokeCap.Round)
            onDrawBehind {
                drawArc(track, -90f, 360f, false, style = style)
                drawArc(c.primary, -90f, 360f * progress.value, false, style = style)
            }
        })
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(center, fontSize = 21.sp, fontWeight = FontWeight.Bold,
                color = c.onSurface)
            if (caption.isNotEmpty()) Text(caption, style = MaterialTheme.typography.labelSmall,
                color = c.onSurfaceVariant)
        }
    }
}
