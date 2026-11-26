package com.deniz0706.ykstakip.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Shared design tokens for the v3 interface.
 *
 * Keep visual decisions here instead of scattering unrelated dp values and
 * animation timings across individual screens. Tokens are deliberately
 * semantic: components should use card, panel, compact, etc. rather than
 * inventing a new value for every screen.
 */
object YksSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val pageHorizontal = 20.dp
    val pageVertical = 24.dp
    val section = 16.dp
    val cardContent = 18.dp
}

object YksShape {
    val control = 12.dp
    val input = 15.dp
    val card = 22.dp
    val panel = 26.dp
    val floating = 28.dp
    val pill = 100.dp
}

object YksMotion {
    const val immediateMs = 100
    const val quickMs = 160
    const val standardMs = 220
    const val emphasisMs = 320
    const val progressMs = 700
}

object YksComponentSize {
    val minimumTouchTarget = 48.dp
    val compactIcon = 18.dp
    val standardIcon = 22.dp
    val largeIcon = 28.dp
    val navigationBarHeight = 72.dp
}
