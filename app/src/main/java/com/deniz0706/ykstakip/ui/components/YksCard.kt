package com.deniz0706.ykstakip.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp

@Composable
fun YksCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    val background = if (highlighted) {
        Brush.linearGradient(listOf(colors.surfaceVariant, colors.surface, colors.surface))
    } else {
        Brush.linearGradient(listOf(colors.surface, colors.surface))
    }
    Column(
        modifier = modifier
            .background(background, shape)
            .border(BorderStroke(1.dp, colors.outline.copy(alpha = 0.75f)), shape)
            .padding(20.dp),
        content = { CompositionLocalProvider(LocalContentColor provides colors.onSurface) { content() } }
    )
}
