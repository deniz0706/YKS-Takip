package com.deniz0706.ykstakip.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import android.content.Context
import android.content.res.Configuration
import com.deniz0706.ykstakip.data.ThemeManager

private val AuroraPalette = darkColorScheme(
    primary = YksColors.Mint,
    onPrimary = YksColors.Background,
    primaryContainer = Color(0xFF174A49),
    onPrimaryContainer = Color(0xFFD5FFF6),
    secondary = YksColors.Lilac,
    onSecondary = Color(0xFF201431),
    secondaryContainer = Color(0xFF45345D),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = YksColors.Blue,
    onTertiary = Color(0xFF071B3D),
    tertiaryContainer = Color(0xFF283F67),
    onTertiaryContainer = Color(0xFFE1EAFF),
    background = YksColors.Background,
    onBackground = YksColors.TextPrimary,
    surface = YksColors.Surface,
    surfaceVariant = YksColors.SurfaceElevated,
    surfaceTint = YksColors.Mint,
    surfaceDim = Color(0xFF080E19),
    surfaceBright = Color(0xFF2D405B),
    surfaceContainerLowest = Color(0xFF0D1828),
    surfaceContainerLow = Color(0xFF172337),
    surfaceContainer = Color(0xFF1C2A40),
    surfaceContainerHigh = Color(0xFF24354C),
    surfaceContainerHighest = Color(0xFF2C3D55),
    onSurface = YksColors.TextPrimary,
    onSurfaceVariant = YksColors.TextSecondary,
    outline = YksColors.Outline,
    outlineVariant = Color(0xFF3D526C),
    inverseSurface = Color(0xFFE5EBF5),
    inverseOnSurface = Color(0xFF172337),
    inversePrimary = Color(0xFF087E72),
    error = YksColors.Error,
    onError = Color(0xFF330812),
    errorContainer = Color(0xFF612333),
    onErrorContainer = Color(0xFFFFDCE3)
)

/** Graphite: near-black surfaces, soft neutral text and one restrained sage accent. */
private val DarkPalette = darkColorScheme(
    primary = Color(0xFFA9C9B9), onPrimary = Color(0xFF17231E),
    primaryContainer = Color(0xFF30483D), onPrimaryContainer = Color(0xFFDDECE3),
    secondary = Color(0xFFB7C2BE), onSecondary = Color(0xFF202A26),
    secondaryContainer = Color(0xFF303B36), onSecondaryContainer = Color(0xFFDCE5DF),
    tertiary = Color(0xFF9EB8CC), onTertiary = Color(0xFF192630),
    tertiaryContainer = Color(0xFF2D414E), onTertiaryContainer = Color(0xFFDCE9F0),
    background = Color(0xFF0D1012), onBackground = Color(0xFFF0F2EF),
    surface = Color(0xFF171B1D), surfaceVariant = Color(0xFF252C2F),
    surfaceTint = Color(0xFFA9C9B9), surfaceDim = Color(0xFF0A0D0F),
    surfaceBright = Color(0xFF353D40), surfaceContainerLowest = Color(0xFF0A0D0F),
    surfaceContainerLow = Color(0xFF151A1C), surfaceContainer = Color(0xFF1B2123),
    surfaceContainerHigh = Color(0xFF242C2E), surfaceContainerHighest = Color(0xFF2D3638),
    onSurface = Color(0xFFF0F2EF), onSurfaceVariant = Color(0xFFAAB5B2),
    outline = Color(0xFF485451), outlineVariant = Color(0xFF303A37),
    inverseSurface = Color(0xFFE5EAE6), inverseOnSurface = Color(0xFF222927),
    inversePrimary = Color(0xFF557363), scrim = Color(0xFF000000),
    error = Color(0xFFFFA0A8), onError = Color(0xFF4A151B),
    errorContainer = Color(0xFF622A31), onErrorContainer = Color(0xFFFFDCE0)
)

/** Light: warm paper, soft graphite and deep botanical green. */
private val LightPalette = lightColorScheme(
    primary = Color(0xFF28685D), onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EBE3), onPrimaryContainer = Color(0xFF173F37),
    secondary = Color(0xFF5F7771), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E9E5), onSecondaryContainer = Color(0xFF30443E),
    tertiary = Color(0xFF526F87), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCE7EF), onTertiaryContainer = Color(0xFF29465C),
    background = Color(0xFFF3F5F2), onBackground = Color(0xFF202925),
    surface = Color(0xFFFBFCFA), surfaceVariant = Color(0xFFE7ECE8),
    surfaceTint = Color(0xFF28685D), surfaceDim = Color(0xFFD9DFDA),
    surfaceBright = Color(0xFFFBFCFA), surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAF7), surfaceContainer = Color(0xFFEEF2EE),
    surfaceContainerHigh = Color(0xFFE7ECE8), surfaceContainerHighest = Color(0xFFDDE5DF),
    onSurface = Color(0xFF202925), onSurfaceVariant = Color(0xFF5D6B65),
    outline = Color(0xFF9EAAA3), outlineVariant = Color(0xFFD2DAD4),
    inverseSurface = Color(0xFF29332E), inverseOnSurface = Color(0xFFE9EEE9),
    inversePrimary = Color(0xFFA8D4C4), error = Color(0xFFB33B4B),
    onError = Color.White, errorContainer = Color(0xFFFFDDE0),
    onErrorContainer = Color(0xFF6B1725)
)

@Composable
fun YksTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = when (ThemeManager.selectedMode.value) {
        "light" -> LightPalette
        "aurora" -> AuroraPalette
        "dark" -> DarkPalette
        else -> if (darkTheme) DarkPalette else LightPalette
    }
    MaterialTheme(colorScheme = palette) {
        Surface(Modifier.fillMaxSize(), color = palette.background, contentColor = palette.onBackground) {
            content()
        }
    }
}

@Composable
fun activeDarkTheme(context: Context): Boolean {
    val configuration = LocalConfiguration.current
    return when (ThemeManager.selectedMode.value) {
        "dark", "aurora" -> true
        "light" -> false
        else -> (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}
