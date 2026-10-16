package com.deniz0706.ykstakip.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ExamTypeTabs(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier.clip(RoundedCornerShape(20.dp)).background(colors.surfaceVariant).padding(4.dp)) {
        listOf("TYT", "AYT").forEach { type ->
            val active = selected == type
            val background = animateColorAsState(if (active) colors.secondary else colors.surfaceVariant, label = "tab-bg")
            val foreground = animateColorAsState(if (active) colors.onSecondary else colors.onSurfaceVariant, label = "tab-text")
            Text(
                type,
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                    .background(background.value).clickable { onSelect(type) }
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center,
                color = foreground.value,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
