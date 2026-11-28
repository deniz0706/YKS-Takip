package com.deniz0706.ykstakip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.ui.theme.YksTokens

/** Primary action button: filled, high-contrast, full-width by default. */
@Composable
fun YksButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = true
) {
    val c = MaterialTheme.colorScheme
    val bg = if (enabled) c.primary else c.onSurfaceVariant.copy(alpha = 0.12f)
    val fg = if (enabled) c.onPrimary else c.onSurfaceVariant.copy(alpha = 0.38f)
    Column(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(YksTokens.Size.minimumTouchTarget)
            .clip(RoundedCornerShape(YksTokens.Shape.control))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = YksTokens.Space.md, vertical = YksTokens.Space.xs)
    ) {
        Text(
            text = text,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
            lineHeight = 20.sp
        )
    }
}

/** Secondary (outline) action button. */
@Composable
fun YksOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val c = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(YksTokens.Size.minimumTouchTarget)
            .clip(RoundedCornerShape(YksTokens.Shape.control))
            .background(Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = YksTokens.Space.md, vertical = YksTokens.Space.xs)
    ) {
        Text(
            text = text,
            color = if (enabled) c.primary else c.onSurfaceVariant.copy(alpha = 0.38f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
            lineHeight = 20.sp
        )
    }
}
