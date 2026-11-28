package com.deniz0706.ykstakip.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.ui.theme.YksShape
import com.deniz0706.ykstakip.ui.theme.YksSpacing
import com.deniz0706.ykstakip.ui.theme.clayRaised

@Composable
fun YksCard(modifier: Modifier = Modifier, highlighted: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(YksShape.card)
    val tint = if (highlighted) lerp(colors.surfaceContainerLow, colors.primary, 0.12f) else null
    Column(modifier.clayRaised(shape = shape, depth = 8.dp, tint = tint).padding(YksSpacing.cardContent),
        content = { CompositionLocalProvider(LocalContentColor provides colors.onSurface) { content() } })
}
