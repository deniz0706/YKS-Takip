package com.deniz0706.ykstakip.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.ui.components.YksSection
import com.deniz0706.ykstakip.ui.components.YksStatTile
import com.deniz0706.ykstakip.ui.theme.YksSpacing

@Composable
fun NewStatisticsScreen(
    exams: List<Exam>,
    obp: Double?,
    targetTyt: Float?,
    targetAyt: Float?
) {
    val c = MaterialTheme.colorScheme
    val tytExams = exams.filter { it.type.label == "TYT" }
    val aytExams = exams.filter { it.type.label == "AYT" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = YksSpacing.pageHorizontal,
            vertical = YksSpacing.pageVertical
        ),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(YksSpacing.section)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "İstatistikler",
                    style = MaterialTheme.typography.displayMedium,
                    color = c.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${exams.size} deneme · OBP: ${Fmt.obp(obp)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)
            ) {
                YksStatTile(
                    label = "TYT deneme",
                    value = tytExams.size.toString(),
                    detail = "kayıtlı",
                    modifier = Modifier.weight(1f)
                )
                YksStatTile(
                    label = "AYT deneme",
                    value = aytExams.size.toString(),
                    detail = "kayıtlı",
                    modifier = Modifier.weight(1f)
                )
                YksStatTile(
                    label = "Ortalama net",
                    value = Fmt.net(exams.averageOfOrNull { it.totalNet }),
                    detail = "tüm sınavlar",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            YksSection(title = "Hedefler", detail = "OBP ve net hedeflerinizin ilerlemesi")
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)
            ) {
                YksStatTile(
                    label = "TYT hedef neti",
                    value = targetTyt?.let { it.toInt().toString() } ?: "—",
                    detail = "0–120",
                    modifier = Modifier.weight(1f)
                )
                YksStatTile(
                    label = "AYT hedef neti",
                    value = targetAyt?.let { it.toInt().toString() } ?: "—",
                    detail = "0–80",
                    modifier = Modifier.weight(1f)
                )
                YksStatTile(
                    label = "OBP",
                    value = obp?.let { it.toInt().toString() } ?: "—",
                    detail = "0–100",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(Modifier.height(96.dp)) }
    }
}

private object Fmt {
    fun obp(value: Double?): String = value?.let { "%.2f".format(it) } ?: "—"
    fun net(value: Double?): String = value?.let { "%.2f".format(it) } ?: "—"
}
