package com.deniz0706.ykstakip.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centralized design tokens for YKS Takip 4.0.
 *
 * Every component reads from here instead of scattering hard-coded dp, sp,
 * duration, or shape values across screens. Tokens are semantic so that a
 * change propagates consistently.
 */
object YksTokens {

    /** Spacing scale. */
    object Space {
        val xxs = 4.dp
        val xs = 8.dp
        val sm = 12.dp
        val md = 16.dp
        val lg = 20.dp
        val xl = 24.dp
        val xxl = 32.dp
        val xxxl = 40.dp
        val pageHorizontal = 20.dp
        val pageVertical = 24.dp
        val section = 20.dp
        val cardContent = 20.dp
        val compactContent = 16.dp
        val huge = 56.dp
    }

    /** Shapes (corner radii). */
    object Shape {
        val control = 12.dp     // buttons, chips, toggles
        val input = 16.dp       // text fields
        val card = 20.dp        // cards and panels
        val panel = 24.dp       // large containers
        val floating = 28.dp    // FAB, bottom sheets
        val pill = 100.dp       // fully rounded
        val circle = 50.dp      // avatars, circular icons
    }

    /** Motion / animation timings. */
    object Motion {
        const val immediateMs = 80
        const val quickMs = 160
        const val standardMs = 240
        const val emphasisMs = 320
        const val slowMs = 500
        const val progressMs = 700
    }

    /** Component sizes. */
    object Size {
        val minimumTouchTarget = 48.dp
        val compactIcon = 18.dp
        val standardIcon = 22.dp
        val largeIcon = 28.dp
        val navigationBarHeight = 80.dp
        val topBarHeight = 56.dp
        val bottomSheetHandle = 4.dp
        val avatarSmall = 32.dp
        val avatarMedium = 40.dp
        val avatarLarge = 56.dp
    }

    /** Typography scale. */
    object Text {
        val display = TextStyle(
            fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp, fontFeatureSettings = "tnum"
        )
        val heroNumber = TextStyle(
            fontSize = 56.sp, lineHeight = 60.sp, fontWeight = FontWeight.Bold,
            letterSpacing = (-1.5).sp, fontFeatureSettings = "tnum"
        )
        val pageTitle = TextStyle(
            fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp
        )
        val sectionTitle = TextStyle(
            fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.1).sp
        )
        val statNumber = TextStyle(
            fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp, fontFeatureSettings = "tnum"
        )
        val bodyLarge = TextStyle(
            fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal
        )
        val bodyMedium = TextStyle(
            fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal
        )
        val bodySmall = TextStyle(
            fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal
        )
        val labelLarge = TextStyle(
            fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp
        )
        val labelMedium = TextStyle(
            fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp
        )
        val labelSmall = TextStyle(
            fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
        val caption = TextStyle(
            fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp
        )
        val overline = TextStyle(
            fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.0.sp
        )
    }
}
