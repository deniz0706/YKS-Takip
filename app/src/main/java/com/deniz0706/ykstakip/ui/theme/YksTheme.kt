package com.deniz0706.ykstakip.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import android.content.Context
import android.content.res.Configuration
import com.deniz0706.ykstakip.data.ThemeManager

/**
 * YKS Takip 4.0 theme entry point.
 *
 * Wraps content in MaterialTheme with the selected palette. Reads the saved
 * theme mode from ThemeManager and repaints retained screens when the mode
 * changes without recreating the Activity.
 */
@Composable
fun YksTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = when (ThemeManager.selectedMode.value) {
        "light" -> YksColorSystem.Light
        "aurora" -> YksColorSystem.Aurora
        "dark" -> YksColorSystem.Dark
        else -> if (darkTheme) YksColorSystem.Dark else YksColorSystem.Light
    }

    val colorScheme = when (val name = palette.name) {
        "light" -> lightColorScheme(
            primary = palette.primary, onPrimary = palette.onPrimary,
            primaryContainer = palette.primaryContainer, onPrimaryContainer = palette.onPrimaryContainer,
            secondary = palette.secondary, onSecondary = palette.onSecondary,
            secondaryContainer = palette.secondaryContainer, onSecondaryContainer = palette.onSecondaryContainer,
            tertiary = palette.tertiary, onTertiary = palette.onTertiary,
            tertiaryContainer = palette.tertiaryContainer, onTertiaryContainer = palette.onTertiaryContainer,
            background = palette.background, onBackground = palette.onSurface,
            surface = palette.surface, surfaceVariant = palette.surfaceContainer,
            surfaceTint = palette.primary, surfaceDim = palette.surfaceDim,
            surfaceBright = palette.surfaceBright,
            surfaceContainerLowest = palette.surface,
            surfaceContainerLow = palette.surfaceContainer,
            surfaceContainer = palette.surfaceBright,
            surfaceContainerHigh = palette.surface,
            surfaceContainerHighest = palette.surface,
            onSurface = palette.onSurface, onSurfaceVariant = palette.onSurfaceVariant,
            outline = palette.outline, outlineVariant = palette.outlineVariant,
            inverseSurface = palette.surface, inverseOnSurface = palette.onSurface,
            inversePrimary = palette.primary,
            error = palette.error, onError = palette.onError,
            errorContainer = palette.errorContainer, onErrorContainer = palette.onErrorContainer
        )
        "aurora" -> darkColorScheme(
            primary = palette.primary, onPrimary = palette.onPrimary,
            primaryContainer = palette.primaryContainer, onPrimaryContainer = palette.onPrimaryContainer,
            secondary = palette.secondary, onSecondary = palette.onSecondary,
            secondaryContainer = palette.secondaryContainer, onSecondaryContainer = palette.onSecondaryContainer,
            tertiary = palette.tertiary, onTertiary = palette.onTertiary,
            tertiaryContainer = palette.tertiaryContainer, onTertiaryContainer = palette.onTertiaryContainer,
            background = palette.background, onBackground = palette.onSurface,
            surface = palette.surface, surfaceVariant = palette.surfaceContainer,
            surfaceTint = palette.primary, surfaceDim = palette.surfaceDim,
            surfaceBright = palette.surfaceBright,
            surfaceContainerLowest = palette.surface,
            surfaceContainerLow = palette.surfaceContainer,
            surfaceContainer = palette.surfaceBright,
            surfaceContainerHigh = palette.surface,
            surfaceContainerHighest = palette.surface,
            onSurface = palette.onSurface, onSurfaceVariant = palette.onSurfaceVariant,
            outline = palette.outline, outlineVariant = palette.outlineVariant,
            inverseSurface = palette.surface, inverseOnSurface = palette.onSurface,
            inversePrimary = palette.primary,
            error = palette.error, onError = palette.onError,
            errorContainer = palette.errorContainer, onErrorContainer = palette.onErrorContainer
        )
        else -> darkColorScheme(
            primary = palette.primary, onPrimary = palette.onPrimary,
            primaryContainer = palette.primaryContainer, onPrimaryContainer = palette.onPrimaryContainer,
            secondary = palette.secondary, onSecondary = palette.onSecondary,
            secondaryContainer = palette.secondaryContainer, onSecondaryContainer = palette.onSecondaryContainer,
            tertiary = palette.tertiary, onTertiary = palette.onTertiary,
            tertiaryContainer = palette.tertiaryContainer, onTertiaryContainer = palette.onTertiaryContainer,
            background = palette.background, onBackground = palette.onSurface,
            surface = palette.surface, surfaceVariant = palette.surfaceContainer,
            surfaceTint = palette.primary, surfaceDim = palette.surfaceDim,
            surfaceBright = palette.surfaceBright,
            surfaceContainerLowest = palette.surface,
            surfaceContainerLow = palette.surfaceContainer,
            surfaceContainer = palette.surfaceBright,
            surfaceContainerHigh = palette.surface,
            surfaceContainerHighest = palette.surface,
            onSurface = palette.onSurface, onSurfaceVariant = palette.onSurfaceVariant,
            outline = palette.outline, outlineVariant = palette.outlineVariant,
            inverseSurface = palette.surface, inverseOnSurface = palette.onSurface,
            inversePrimary = palette.primary,
            error = palette.error, onError = palette.onError,
            errorContainer = palette.errorContainer, onErrorContainer = palette.onErrorContainer
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YksTypography,
        shapes = YksShapes,
        content = content
    )
}

/** Typography for the design system. */
private val YksTypography = androidx.compose.material3.Typography(
    displayLarge = YksTokens.Text.display,
    displayMedium = YksTokens.Text.pageTitle,
    displaySmall = YksTokens.Text.sectionTitle,
    headlineLarge = YksTokens.Text.heroNumber,
    headlineMedium = YksTokens.Text.statNumber,
    titleLarge = YksTokens.Text.sectionTitle,
    titleMedium = YksTokens.Text.bodyLarge,
    titleSmall = YksTokens.Text.labelLarge,
    bodyLarge = YksTokens.Text.bodyLarge,
    bodyMedium = YksTokens.Text.bodyMedium,
    bodySmall = YksTokens.Text.bodySmall,
    labelLarge = YksTokens.Text.labelLarge,
    labelMedium = YksTokens.Text.labelMedium,
    labelSmall = YksTokens.Text.labelSmall,
    caption = YksTokens.Text.caption
)

/** Shapes for the design system. */
private val YksShapes = androidx.compose.material3.Shapes(
    small = YksTokens.Shape.control,
    medium = YksTokens.Shape.input,
    large = YksTokens.Shape.card,
    extraLarge = YksTokens.Shape.panel
)

/** Returns whether the active theme is dark. */
@Composable
fun activeDarkTheme(context: Context): Boolean {
    val configuration = LocalConfiguration.current
    return when (ThemeManager.selectedMode.value) {
        "dark", "aurora" -> true
        "light" -> false
        else -> (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}
