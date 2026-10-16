package com.deniz0706.ykstakip.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import android.content.Context
import android.content.res.Configuration
import com.deniz0706.ykstakip.data.ThemeManager
import androidx.compose.ui.graphics.Color

private val DarkPalette = darkColorScheme(
    primary = YksColors.Mint,
    onPrimary = YksColors.Background,
    primaryContainer = Color(0xFF174A49),
    onPrimaryContainer = Color(0xFFD5FFF6),
    onSecondary = Color(0xFF201431),
    secondaryContainer = Color(0xFF45345D),
    onSecondaryContainer = Color(0xFFF3E8FF),
    onTertiary = Color(0xFF071B3D),
    tertiaryContainer = Color(0xFF283F67),
    onTertiaryContainer = Color(0xFFE1EAFF),
    secondary = YksColors.Lilac,
    tertiary = YksColors.Blue,
    background = YksColors.Background,
    onBackground = YksColors.TextPrimary,
    surface = YksColors.Surface,
    surfaceVariant = YksColors.SurfaceElevated,
    surfaceTint = YksColors.Mint,
    surfaceContainer = Color(0xFF1C2A40),
    surfaceContainerHigh = Color(0xFF24354C),
    surfaceContainerHighest = Color(0xFF2C3D55),
    onSurface = YksColors.TextPrimary,
    onSurfaceVariant = YksColors.TextSecondary,
    outline = YksColors.Outline,
    outlineVariant = Color(0xFF3D526C),
    inverseSurface = Color(0xFFE5EBF5),
    inverseOnSurface = Color(0xFF172337),
    error = YksColors.Error,
    onError = Color(0xFF330812),
    errorContainer = Color(0xFF612333),
    onErrorContainer = Color(0xFFFFDCE3)
)

private val LightPalette = lightColorScheme(
    primary = Color(0xFF087E72),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F6EF),
    onPrimaryContainer = Color(0xFF064D46),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAE1FA),
    onSecondaryContainer = Color(0xFF362456),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDE9FF),
    onTertiaryContainer = Color(0xFF173968),
    secondary = Color(0xFF7055A5),
    tertiary = Color(0xFF365FA7),
    background = Color(0xFFF5F7FC),
    onBackground = Color(0xFF152238),
    surface = Color.White,
    onSurface = Color(0xFF152238),
    surfaceVariant = Color(0xFFE9EDF5),
    onSurfaceVariant = Color(0xFF53657D),
    outline = Color(0xFFCAD3DF),
    outlineVariant = Color(0xFFD9E0E9),
    error = Color(0xFFB42342),
    onError = Color.White,
    errorContainer = Color(0xFFFFD9E1),
    onErrorContainer = Color(0xFF680B22)
)

@Composable
fun YksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    MaterialTheme(colorScheme = palette) {
        // MaterialTheme does not itself create a Surface. Without a root Surface,
        // Text outside cards can inherit the platform's default BLACK content
        // color even while our Compose palette uses a dark background.
        // Keep this one source of truth for every Compose fragment and dialog.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = palette.background,
            contentColor = palette.onBackground
        ) {
            content()
        }
    }
}

/**
 * Read LocalConfiguration even for explicitly selected themes, so kept-alive
 * fragments recompose when the Activity handles uiMode without recreation.
 */
@Composable
fun activeDarkTheme(context: Context): Boolean {
    val configuration = LocalConfiguration.current
    return when (ThemeManager.currentTheme(context)) {
        "dark" -> true
        "light" -> false
        else -> (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    }
}
