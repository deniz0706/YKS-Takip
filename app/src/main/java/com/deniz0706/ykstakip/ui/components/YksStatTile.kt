package com.deniz0706.ykstakip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.ui.theme.YksTokens
import com.deniz0706.ykstakip.ui.theme.clayRaised

/** A single metric display: large number with label and optional detail. */
@Composable
fun YksStatTile(
    label: String,
    value: String,
    detail: String? = null,
    valueColor: androidx.compose.ui.graphics.Color? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val c = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(YksTokens.Shape.input))
            .clayRaised(shape = RoundedCornerShape(YksTokens.Shape.input), depth = 5.dp, tint = c.surfaceContainer)
            .padding(YksTokens.Space.md)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = c.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = valueColor ?: c.onSurface,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp
        )
        detail?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
