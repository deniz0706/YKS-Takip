package com.deniz0706.ykstakip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.ui.theme.YksComponentSize
import com.deniz0706.ykstakip.ui.theme.YksShape
import com.deniz0706.ykstakip.ui.theme.YksText
import com.deniz0706.ykstakip.util.Fmt

/** Sentence-case section heading with an optional quiet value on the right. */
@Composable
fun YksSectionTitle(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    val c = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = YksText.sectionTitle, color = c.onSurface)
        if (trailing != null) Text(trailing, style = YksText.caption, color = c.onSurfaceVariant)
    }
}

/** Net change versus the previous exam; neutral when there is nothing to compare against. */
@Composable
fun YksDeltaChip(change: Double?, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val tint = when {
        change == null -> c.onSurfaceVariant
        change >= 0 -> c.primary
        else -> c.error
    }
    val fill = when {
        change == null -> c.surfaceVariant
        change >= 0 -> c.primary.copy(alpha = .13f)
        else -> c.error.copy(alpha = .13f)
    }
    val label = if (change == null) "İlk deneme"
        else (if (change >= 0) "↗ " else "↘ ") + Fmt.netWithSign(change) + " net"
    Surface(modifier = modifier, shape = RoundedCornerShape(YksShape.pill), color = fill) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = tint,
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

/** One tappable next step: glyph tile, title, supporting line and a chevron. */
@Composable
fun YksActionRow(
    glyph: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier.fillMaxWidth()
            .clip(RoundedCornerShape(YksShape.input))
            .clickable(onClick = onClick)
            .heightIn(min = YksComponentSize.minimumTouchTarget)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(YksShape.control)),
            contentAlignment = Alignment.Center
        ) {
            Text(glyph, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = c.onSurface, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Text("›", color = c.onSurfaceVariant, fontSize = 22.sp)
    }
}

/** Label / value / caption triplet used in compact statistic grids. */
@Composable
fun YksStatCell(
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val c = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = YksText.caption, color = c.onSurfaceVariant)
        Text(value, style = YksText.statNumber, color = valueColor, maxLines = 1)
        Text(caption, style = YksText.caption, color = c.onSurfaceVariant.copy(alpha = .8f))
    }
}
