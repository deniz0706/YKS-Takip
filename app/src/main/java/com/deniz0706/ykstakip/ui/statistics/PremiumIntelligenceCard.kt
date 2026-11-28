package com.deniz0706.ykstakip.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.util.PerformanceIntelligence

@Composable
internal fun PremiumIntelligenceCard(
    exams: List<Exam>,
    type: ExamType,
    aytField: AytField? = null
) {
    val report = remember(exams, type, aytField) {
        PerformanceIntelligence.analyze(exams, type, aytField, window = 5)
    }
    val c = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = c.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (type == ExamType.TYT) "TYT performans zekâsı"
                    else "AYT performans zekâsı · ${aytField?.name ?: "Tüm alanlar"}",
                style = MaterialTheme.typography.titleMedium,
                color = c.onSurface
            )

            if (report.examCount == 0) {
                Text(
                    text = "Bu kategoride henüz deneme kaydı yok.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Son 5 deneme ortalaması", style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant)
                        Text(
                            report.recentAverage?.let { String.format(java.util.Locale("tr", "TR"), "%.2f net", it) } ?: "—",
                            style = MaterialTheme.typography.titleLarge,
                            color = c.primary
                        )
                    }
                    Column {
                        Text("Önceki döneme fark", style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant)
                        Text(
                            report.change?.let { String.format(java.util.Locale("tr", "TR"), "%+.2f net", it) } ?: "Yetersiz veri",
                            style = MaterialTheme.typography.titleMedium,
                            color = when {
                                report.change == null -> c.onSurfaceVariant
                                report.change >= 0.0 -> c.primary
                                else -> c.error
                            }
                        )
                    }
                }

                report.consistency?.let {
                    Text(
                        text = "Son dönem değişkenliği: ${String.format(java.util.Locale("tr", "TR"), "%.2f", it)} net (düşük olması daha istikrarlı).",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.onSurfaceVariant
                    )
                }

                report.subjects.take(5).forEach { subject ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(subject.subject, style = MaterialTheme.typography.bodyMedium, color = c.onSurface)
                            Text(
                                String.format(java.util.Locale("tr", "TR"), "%.2f net · %d%% doğru", subject.recentAverage, (subject.averageAccuracy * 100).toInt()),
                                style = MaterialTheme.typography.labelMedium,
                                color = c.onSurfaceVariant
                            )
                        }
                        if (subject.weakTopics.isNotEmpty()) {
                            Text(
                                text = "Tekrar etiketleri: ${subject.weakTopics.joinToString()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.onSurfaceVariant
                            )
                        }
                    }
                }

                if (report.insufficientData) {
                    Text(
                        text = "Kayıt sayısı az; eğilimler kesin sonuç olarak yorumlanmamalı.",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.tertiary
                    )
                }
            }
        }
    }
}
