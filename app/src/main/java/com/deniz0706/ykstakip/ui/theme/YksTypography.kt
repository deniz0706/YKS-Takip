package com.deniz0706.ykstakip.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Screen-level text styles. Numbers use tabular figures so values do not jitter
 * while they animate or change between exams.
 */
object YksText {
    val heroNumber = TextStyle(
        fontSize = 56.sp, lineHeight = 60.sp, fontWeight = FontWeight.Bold,
        letterSpacing = (-1.5).sp, fontFeatureSettings = "tnum"
    )
    val statNumber = TextStyle(
        fontSize = 26.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp, fontFeatureSettings = "tnum"
    )
    val smallNumber = TextStyle(
        fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum"
    )
    val pageTitle = TextStyle(
        fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp
    )
    val sectionTitle = TextStyle(
        fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.1).sp
    )
    val caption = TextStyle(
        fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp
    )
}
