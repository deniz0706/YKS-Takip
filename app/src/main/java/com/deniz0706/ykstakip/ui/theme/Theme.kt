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

private val AuroraPalette = darkColorScheme(
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

/**
 * Koyu / Graphite: one cohesive black, champagne-gold and sea-glass palette.
 * Set every Material 3 role explicitly: the defaults include lavender accents
 * which leak into chips, menus, overlays and interactive widgets otherwise.
 */
private val DarkPalette = darkColorScheme(
    primary = Color(0xFFE6C38F),
    onPrimary = Color(0xFF302111),
    primaryContainer = Color(0xFF493923),
    onPrimaryContainer = Color(0xFFFFE8C6),
    primaryFixed = Color(0xFFFFE4B5),
    primaryFixedDim = Color(0xFFE6C38F),
    onPrimaryFixed = Color(0xFF241806),
    onPrimaryFixedVariant = Color(0xFF604621),
    secondary = Color(0xFFB4C6CF),
    onSecondary = Color(0xFF1B2A30),
    secondaryContainer = Color(0xFF27373D),
    onSecondaryContainer = Color(0xFFDEEAF0),
    secondaryFixed = Color(0xFFDCE7EC),
    secondaryFixedDim = Color(0xFFB4C6CF),
    onSecondaryFixed = Color(0xFF13232B),
    onSecondaryFixedVariant = Color(0xFF344951),
    tertiary = Color(0xFF83D0BE),
    onTertiary = Color(0xFF0A2C25),
    tertiaryContainer = Color(0xFF20463F),
    onTertiaryContainer = Color(0xFFCCF7EA),
    tertiaryFixed = Color(0xFFC7F2E7),
    tertiaryFixedDim = Color(0xFF83D0BE),
    onTertiaryFixed = Color(0xFF062820),
    onTertiaryFixedVariant = Color(0xFF235D50),
    background = Color(0xFF090B10),
    onBackground = Color(0xFFF5F6FA),
    surface = Color(0xFF15181F),
    surfaceVariant = Color(0xFF242831),
    surfaceTint = Color(0xFFE6C38F),
    surfaceDim = Color(0xFF090B10),
    surfaceBright = Color(0xFF363942),
    surfaceContainerLowest = Color(0xFF0D1016),
    surfaceContainerLow = Color(0xFF14171D),
    surfaceContainer = Color(0xFF191C24),
    surfaceContainerHigh = Color(0xFF222630),
    surfaceContainerHighest = Color(0xFF2D313D),
    onSurface = Color(0xFFF5F6FA),
    onSurfaceVariant = Color(0xFFA8ADBC),
    outline = Color(0xFF525663),
    outlineVariant = Color(0xFF343744),
    inverseSurface = Color(0xFFDFE2EA),
    inverseOnSurface = Color(0xFF222630),
    inversePrimary = Color(0xFF795627),
    scrim = Color(0xFF000000),
    error = Color(0xFFFF90A5),
    onError = Color(0xFF4C1220),
    errorContainer = Color(0xFF64283A),
    onErrorContainer = Color(0xFFFFDCE5)
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
    // selectedMode is observable: Aurora -> Graphite switches with no uiMode
    // change and must still update every retained Compose screen.
    val palette = when (ThemeManager.selectedMode.value) {
        "light" -> LightPalette
        "aurora" -> AuroraPalette
        "dark" -> DarkPalette
        else -> if (darkTheme) DarkPalette else LightPalette
    }
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
    return when (ThemeManager.selectedMode.value) {
        "dark", "aurora" -> true
        "light" -> false
        else -> (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    }
}
