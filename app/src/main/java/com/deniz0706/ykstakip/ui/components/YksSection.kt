package com.deniz0706.ykstakip.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.deniz0706.ykstakip.ui.theme.YksTokens

/** Consistent section heading used across every screen. */
@Composable
fun YksSection(
    title: String,
    detail: String? = null,
    trailing: String? = null,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = YksTokens.Space.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = c.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            detail?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = c.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

