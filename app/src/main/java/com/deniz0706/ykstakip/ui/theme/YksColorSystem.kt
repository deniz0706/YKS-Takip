package com.deniz0706.ykstakip.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Premium color system for YKS Takip 4.0.
 *
 * Three palettes: Light (warm paper), Dark Graphite (near-black neutral), and Aurora
 * (deep navy with vivid accents). Each defines semantic roles used consistently across
 * every surface, so components never pick raw values.
 *
 * Tonal structure (per palette):
 *   - background        : page canvas
 *   - surface           : elevated containers (cards)
 *   - surfaceContainer  : subtle layering within cards
 *   - surfaceBright     : raised interactive containers
 *   - outline           : hairline borders between sections
 *   - onSurface         : primary text / icons
 *   - onSurfaceVariant  : secondary text
 *   - primary           : brand accent (actions, links, selected states)
 *   - onPrimary         : text on primary
 *   - secondary         : supporting accent
 *   - tertiary          : informational accent
 *   - error             : destructive / validation
 *   - onError           : text on error
 */
object YksColorSystem {

    /** Light palette: warm paper with deep botanical green accents. */
    val Light = YksPalette(
        name = "light",
        background = Color(0xFFF7F9F6),
        surface = Color(0xFFFFFFFF),
        surfaceContainer = Color(0xFFF1F5F0),
        surfaceBright = Color(0xFFEEF3EE),
        surfaceDim = Color(0xFFE4EAE2),
        outline = Color(0xFFD2DAD4),
        outlineVariant = Color(0xFFE3E8E3),
        onSurface = Color(0xFF202925),
        onSurfaceVariant = Color(0xFF5D6B65),
        primary = Color(0xFF28685D),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD9EBE3),
        onPrimaryContainer = Color(0xFF173F37),
        secondary = Color(0xFF5F7771),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0E9E5),
        onSecondaryContainer = Color(0xFF30443E),
        tertiary = Color(0xFF526F87),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFDCE7EF),
        onTertiaryContainer = Color(0xFF29465C),
        error = Color(0xFFB33B4B),
        onError = Color.White,
        errorContainer = Color(0xFFFFDDE0),
        onErrorContainer = Color(0xFF6B1725),
        scrim = Color(0x80000000)
    )

    /** Dark Graphite palette: near-black surfaces, soft neutral text, sage accent. */
    val Dark = YksPalette(
        name = "dark",
        background = Color(0xFF0B0D0E),
        surface = Color(0xFF141719),
        surfaceContainer = Color(0xFF1B1F21),
        surfaceBright = Color(0xFF222729),
        surfaceDim = Color(0xFF0A0C0D),
        outline = Color(0xFF3A4042),
        outlineVariant = Color(0xFF2B3032),
        onSurface = Color(0xFFF0F2EF),
        onSurfaceVariant = Color(0xFFAAB5B2),
        primary = Color(0xFFA9C9B9),
        onPrimary = Color(0xFF17231E),
        primaryContainer = Color(0xFF30483D),
        onPrimaryContainer = Color(0xFFDDECE3),
        secondary = Color(0xFFB7C2BE),
        onSecondary = Color(0xFF202A26),
        secondaryContainer = Color(0xFF303B36),
        onSecondaryContainer = Color(0xFFDCE5DF),
        tertiary = Color(0xFF9EB8CC),
        onTertiary = Color(0xFF192630),
        tertiaryContainer = Color(0xFF2D414E),
        onTertiaryContainer = Color(0xFFDCE9F0),
        error = Color(0xFFFFA0A8),
        onError = Color(0xFF4A151B),
        errorContainer = Color(0xFF622A31),
        onErrorContainer = Color(0xFFFFDCE0),
        scrim = Color(0xB3000000)
    )

    /** Aurora palette: deep navy with vivid mint, lilac, electric-blue accents. */
    val Aurora = YksPalette(
        name = "aurora",
        background = Color(0xFF0B1220),
        surface = Color(0xFF172337),
        surfaceContainer = Color(0xFF22334A),
        surfaceBright = Color(0xFF2D405B),
        surfaceDim = Color(0xFF080E19),
        outline = Color(0xFF30445C),
        outlineVariant = Color(0xFF3D526C),
        onSurface = Color(0xFFF3F8FF),
        onSurfaceVariant = Color(0xFF9AAFC5),
        primary = Color(0xFF57D6C1),
        onPrimary = Color(0xFF0B1220),
        primaryContainer = Color(0xFF174A49),
        onPrimaryContainer = Color(0xFFD5FFF6),
        secondary = Color(0xFFB9A4F5),
        onSecondary = Color(0xFF201431),
        secondaryContainer = Color(0xFF45345D),
        onSecondaryContainer = Color(0xFFF3E8FF),
        tertiary = Color(0xFF80A9FF),
        onTertiary = Color(0xFF071B3D),
        tertiaryContainer = Color(0xFF283F67),
        onTertiaryContainer = Color(0xFFE1EAFF),
        error = Color(0xFFFF879A),
        onError = Color(0xFF330812),
        errorContainer = Color(0xFF612333),
        onErrorContainer = Color(0xFFFFDCE3),
        scrim = Color(0xB3000000)
    )
}

/** A single semantic color palette. */
data class YksPalette(
    val name: String,
    val background: Color,
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceBright: Color,
    val surfaceDim: Color,
    val outline: Color,
    val outlineVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val scrim: Color
)
