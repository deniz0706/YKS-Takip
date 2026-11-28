package com.deniz0706.ykstakip.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class ClayTones(val top: Color, val bottom: Color, val rimLight: Color, val rimDark: Color, val shadow: Color)

internal fun clayTones(base: Color, dark: Boolean): ClayTones = ClayTones(
    top = lerp(base, Color.White, if (dark) 0.05f else 0.55f),
    bottom = lerp(base, Color.Black, if (dark) 0.20f else 0.05f),
    rimLight = Color.White.copy(alpha = if (dark) 0.09f else 0.75f),
    rimDark = Color.Black.copy(alpha = if (dark) 0.35f else 0.07f),
    shadow = Color.Black.copy(alpha = if (dark) 0.60f else 0.14f)
)

@Composable
fun Modifier.clayRaised(shape: Shape = RoundedCornerShape(YksTokens.Shape.panel), depth: Dp = 10.dp, tint: Color? = null): Modifier {
    val colors = MaterialTheme.colorScheme
    val tones = clayTones(tint ?: colors.surfaceContainerLow, colors.background.luminance() < 0.5f)
    return this.shadow(depth, shape, clip = false, ambientColor = tones.shadow, spotColor = tones.shadow)
        .background(Brush.linearGradient(listOf(tones.top, tones.bottom)), shape)
        .border(1.dp, Brush.linearGradient(listOf(tones.rimLight, tones.rimDark)), shape)
}

@Composable
fun Modifier.clayInset(shape: Shape = RoundedCornerShape(YksTokens.Shape.pill)): Modifier {
    val colors = MaterialTheme.colorScheme
    val dark = colors.background.luminance() < 0.5f
    val well = lerp(colors.background, Color.Black, if (dark) 0.30f else 0.04f)
    val lip = lerp(colors.background, Color.White, if (dark) 0.04f else 0.60f)
    val rimTop = Color.Black.copy(alpha = if (dark) 0.35f else 0.08f)
    val rimBottom = Color.White.copy(alpha = if (dark) 0.06f else 0.70f)
    return this.background(Brush.verticalGradient(listOf(well, lip)), shape)
        .border(1.dp, Brush.verticalGradient(listOf(rimTop, rimBottom)), shape)
}
