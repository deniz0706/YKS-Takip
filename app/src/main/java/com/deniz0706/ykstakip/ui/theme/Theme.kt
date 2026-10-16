package com.deniz0706.ykstakip.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkPalette = darkColorScheme(
    primary = YksColors.Mint,
    onPrimary = YksColors.Background,
    secondary = YksColors.Lilac,
    tertiary = YksColors.Blue,
    background = YksColors.Background,
    onBackground = YksColors.TextPrimary,
    surface = YksColors.Surface,
    onSurface = YksColors.TextPrimary,
    surfaceVariant = YksColors.SurfaceElevated,
    onSurfaceVariant = YksColors.TextSecondary,
    outline = YksColors.Outline,
    error = YksColors.Error
)

private val LightPalette = lightColorScheme(
    primary = Color(0xFF087E72),
    onPrimary = Color.White,
    secondary = Color(0xFF7055A5),
    tertiary = Color(0xFF365FA7),
    background = Color(0xFFF5F7FC),
    onBackground = Color(0xFF152238),
    surface = Color.White,
    onSurface = Color(0xFF152238),
    surfaceVariant = Color(0xFFE9EDF5),
    onSurfaceVariant = Color(0xFF53657D),
    outline = Color(0xFFCAD3DF)
)

@Composable
fun YksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkPalette else LightPalette,
        content = content
    )
}
