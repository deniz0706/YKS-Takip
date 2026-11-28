package com.deniz0706.ykstakip.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.ui.theme.clayInset
import com.deniz0706.ykstakip.ui.theme.clayRaised

@Composable
fun ClayBottomBar(currentDestination: YksNavDestination, onDestinationSelected: (YksNavDestination) -> Unit, onQuickAdd: () -> Unit) {
    val barShape = RoundedCornerShape(32.dp)
    val left = BOTTOM_NAV_DESTINATIONS.take(3)
    val right = BOTTOM_NAV_DESTINATIONS.drop(3)
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth().clayRaised(shape = barShape, depth = 12.dp).padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            left.forEach { destination ->
                ClayNavItem(destination, isSelected(currentDestination, destination), { onDestinationSelected(destination) }, Modifier.weight(1f))
            }
            ClayAddButton(onQuickAdd)
            right.forEach { destination ->
                ClayNavItem(destination, isSelected(currentDestination, destination), { onDestinationSelected(destination) }, Modifier.weight(1f))
            }
        }
    }
}

private fun isSelected(current: YksNavDestination, destination: YksNavDestination): Boolean =
    current == destination || (current is YksNavDestination.ExamDetail && destination == YksNavDestination.Exams)

private fun shortLabel(destination: YksNavDestination): String = when (destination) {
    YksNavDestination.Home -> "Kokpit"
    YksNavDestination.Exams -> "Deneme"
    YksNavDestination.Statistics -> "Analiz"
    YksNavDestination.Study -> "Çalışma"
    YksNavDestination.AiCoach -> "AI Koç"
    YksNavDestination.Settings -> "Ayarlar"
    is YksNavDestination.ExamDetail -> "Detay"
}

@Composable
private fun ClayNavItem(destination: YksNavDestination, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(22.dp)
    Column(modifier.padding(horizontal = 1.dp).then(if (selected) Modifier.clayInset(shape) else Modifier)
        .clip(shape).clickable(role = Role.Tab, onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(destinationIcon(destination), fontSize = 20.sp, modifier = Modifier.alpha(if (selected) 1f else 0.6f))
        Text(shortLabel(destination), style = MaterialTheme.typography.labelSmall,
            color = if (selected) c.primary else c.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ClayAddButton(onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Box(Modifier.padding(horizontal = 4.dp).size(52.dp).clayRaised(shape = CircleShape, depth = 8.dp, tint = c.primaryContainer)
        .clip(CircleShape).clickable(onClickLabel = "Yeni deneme ekle", role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center) {
        Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = c.onPrimaryContainer)
    }
}
