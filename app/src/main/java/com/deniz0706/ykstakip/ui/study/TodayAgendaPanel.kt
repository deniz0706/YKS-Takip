package com.deniz0706.ykstakip.ui.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.StudyPlanItem
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.ui.components.DashboardRing

/** A compact daily agenda backed by the same weekly plan as the Plan tab. */
@Composable
fun TodayAgendaPanel(
    items: List<StudyPlanItem>,
    onToggle: (Long) -> Unit,
    onAdd: () -> Unit,
    onShowPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val done = items.count { it.completed }
    DashboardPanel(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("BUGÜNÜN PROGRAMI", color = colors.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(5.dp))
                Text(if (items.isEmpty()) "Bugün neler yapacağız?"
                    else "$done / ${items.size} ders tamamlandı",
                    color = colors.onSurface, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Text(if (items.isEmpty()) "Ders ekleyip gününü planlayabilirsin."
                    else "Tamamladığın dersleri buradan işaretle.",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
            if (items.isNotEmpty()) {
                DashboardRing(done.toFloat() / items.size,
                    "${done * 100 / items.size}%",
                    caption = "bugün", diameter = 80.dp)
            }
        }
        Spacer(Modifier.height(10.dp))
        if (items.isEmpty()) {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Text("+  Bugüne ders ekle")
            }
        } else {
            items.take(4).forEach { lesson ->
                HorizontalDivider(color = colors.outline.copy(alpha = .22f))
                Row(Modifier.fillMaxWidth()
                    .clickable { onToggle(lesson.id) }
                    .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = lesson.completed,
                        onCheckedChange = { onToggle(lesson.id) }
                    )
                    Column(Modifier.weight(1f)) {
                        Text(lesson.subject, color = if (lesson.completed)
                            colors.onSurfaceVariant else colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium)
                        if (lesson.topic.isNotBlank()) {
                            Text(lesson.topic, color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(lesson.startTime,
                        color = colors.secondary,
                        style = MaterialTheme.typography.labelMedium)
                }
            }
            if (items.size > 4) {
                Text("Diğer ${items.size - 4} ders programda",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onShowPlan, modifier = Modifier.align(Alignment.End)) {
                Text("Tüm programı gör  ↗")
            }
        }
        Text("Dersler haftalık tekrar eder. Tamamlanma işaretleri her pazartesi yenilenir.",
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall)
    }
}
