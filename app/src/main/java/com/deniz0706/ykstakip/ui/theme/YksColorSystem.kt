package com.deniz0706.ykstakip.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Premium color system for YKS Takip 4.0.
 *
 * A family of coordinated palettes: Light, Graphite, Aurora, Lavender, Mint, Ocean,
 * and Rose. Each defines semantic roles used consistently across every surface, so
 * components never need to pick raw colors.
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
    
    /** Soft lavender light theme. */
    val Lavender = Light.copy(
        name = "lavender",
        background = Color(0xFFF7F4FC),
        surface = Color(0xFFFFFCFF),
        surfaceContainer = Color(0xFFF0EAF8),
        surfaceBright = Color(0xFFECE4F6),
        surfaceDim = Color(0xFFE5DDED),
        outline = Color(0xFFDED5EA),
        outlineVariant = Color(0xFFEAE3F1),
        onSurface = Color(0xFF292333),
        onSurfaceVariant = Color(0xFF71677E),
        primary = Color(0xFF8064B5),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE9DDF8),
        onPrimaryContainer = Color(0xFF34234E),
        secondary = Color(0xFF78658F),
        secondaryContainer = Color(0xFFEDE4F5),
        onSecondaryContainer = Color(0xFF3D304D),
        tertiary = Color(0xFF657FA8),
        tertiaryContainer = Color(0xFFE2EAF7),
        onTertiaryContainer = Color(0xFF283B5A)
    )

    /** Calm mint light theme. */
    val Mint = Light.copy(
        name = "mint",
        background = Color(0xFFF1F8F4),
        surface = Color(0xFFFBFFFC),
        surfaceContainer = Color(0xFFE7F2EA),
        surfaceBright = Color(0xFFE1EFE6),
        surfaceDim = Color(0xFFDCE9E0),
        outline = Color(0xFFCFE0D4),
        outlineVariant = Color(0xFFE0ECE4),
        onSurface = Color(0xFF1E3027),
        onSurfaceVariant = Color(0xFF5D7466),
        primary = Color(0xFF36866A),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD5EFE1),
        onPrimaryContainer = Color(0xFF173F2D),
        secondary = Color(0xFF5D7969),
        secondaryContainer = Color(0xFFDDEBE2),
        onSecondaryContainer = Color(0xFF294637),
        tertiary = Color(0xFF547F91),
        tertiaryContainer = Color(0xFFDDECF0),
        onTertiaryContainer = Color(0xFF254653)
    )

    /** Ocean dark theme: deep blue surfaces and a clear sky-blue accent. */
    val Ocean = Aurora.copy(
        name = "ocean",
        background = Color(0xFF101A2B),
        surface = Color(0xFF19283D),
        surfaceContainer = Color(0xFF253A55),
        surfaceBright = Color(0xFF304966),
        surfaceDim = Color(0xFF0B1422),
        outline = Color(0xFF354B68),
        outlineVariant = Color(0xFF405A7A),
        onSurface = Color(0xFFF0F5FF),
        onSurfaceVariant = Color(0xFFA7B8D0),
        primary = Color(0xFF75A7F7),
        onPrimary = Color(0xFF0D1C35),
        primaryContainer = Color(0xFF294B78),
        onPrimaryContainer = Color(0xFFE0ECFF),
        secondary = Color(0xFF9CC9E8),
        secondaryContainer = Color(0xFF29465D),
        onSecondaryContainer = Color(0xFFDDF2FF),
        tertiary = Color(0xFFB7A4F5),
        tertiaryContainer = Color(0xFF45345D),
        onTertiaryContainer = Color(0xFFF3E8FF)
    )

    /** Muted rose dark theme, intentionally restrained rather than neon. */
    val Rose = Dark.copy(
        name = "rose",
        background = Color(0xFF1C151A),
        surface = Color(0xFF292027),
        surfaceContainer = Color(0xFF392B34),
        surfaceBright = Color(0xFF45343F),
        surfaceDim = Color(0xFF140F13),
        outline = Color(0xFF4B3944),
        outlineVariant = Color(0xFF3D3038),
        onSurface = Color(0xFFF8F0F4),
        onSurfaceVariant = Color(0xFFC2AEB9),
        primary = Color(0xFFD48BA8),
        onPrimary = Color(0xFF321A27),
        primaryContainer = Color(0xFF593747),
        onPrimaryContainer = Color(0xFFFFE0EC),
        secondary = Color(0xFFD1B5C3),
        secondaryContainer = Color(0xFF493641),
        onSecondaryContainer = Color(0xFFF5DDE8),
        tertiary = Color(0xFFA9B8D8),
        tertiaryContainer = Color(0xFF34415C),
        onTertiaryContainer = Color(0xFFE0E8FF)
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
