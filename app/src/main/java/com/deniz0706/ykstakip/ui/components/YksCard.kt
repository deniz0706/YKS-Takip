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
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.ui.theme.YksSpacing
import com.deniz0706.ykstakip.ui.theme.YksShape

@Composable
fun YksCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(YksShape.card)
    val background = if (highlighted) {
        Brush.linearGradient(listOf(colors.surfaceContainer, colors.surfaceContainer))
    } else {
        Brush.linearGradient(listOf(colors.surfaceContainerLow, colors.surfaceContainerLow))
    }
    Column(
        modifier = modifier
            .background(background, shape)
            .padding(YksSpacing.cardContent),
        content = { CompositionLocalProvider(LocalContentColor provides colors.onSurface) { content() } }
    )
}
