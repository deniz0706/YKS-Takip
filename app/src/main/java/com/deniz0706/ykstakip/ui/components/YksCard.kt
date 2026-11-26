package com.deniz0706.ykstakip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.deniz0706.ykstakip.ui.theme.YksSpacing
import com.deniz0706.ykstakip.ui.theme.YksShape

/** Compact card primitive sharing the same quiet, borderless surface language. */
@Composable
fun YksCard(modifier: Modifier = Modifier, highlighted: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(YksShape.card)
    val fill = if (highlighted) Brush.linearGradient(listOf(colors.primary.copy(alpha = 0.07f), colors.surfaceContainerHigh))
        else Brush.linearGradient(listOf(colors.surfaceContainerLow, colors.surfaceContainerLow))
    Column(modifier.background(fill, shape).padding(YksSpacing.cardContent),
        content = { CompositionLocalProvider(LocalContentColor provides colors.onSurface) { content() } })
}
