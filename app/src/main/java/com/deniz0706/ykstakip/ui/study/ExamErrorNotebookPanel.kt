package com.deniz0706.ykstakip.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.util.ExamErrorNotebook

@Composable
fun ExamErrorNotebookPanel(
    suggestions: List<ExamErrorNotebook.Suggestion>,
    onImport: (ExamErrorNotebook.Suggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    DashboardPanel(modifier, accent = true) {
        Text("DENEMEDEN HATA DEFTERİNE", color = colors.secondary,
            style = MaterialTheme.typography.labelSmall, letterSpacing = .8.sp)
        Spacer(Modifier.height(5.dp))
        Text("Etiketlediğin zayıf konular", color = colors.onSurface,
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Denemede işaretlediğin konuları öğrenme ve tekrar listene aktar.",
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        suggestions.take(6).forEachIndexed { index, suggestion ->
            Spacer(Modifier.height(10.dp))
            if (index > 0) androidx.compose.material3.HorizontalDivider(
                color = colors.outlineVariant.copy(alpha = .5f))
            Column(Modifier.fillMaxWidth().padding(top = 7.dp)) {
                Text(suggestion.topic, color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold)
                Text("${suggestion.examTitle} · ${suggestion.examDate} · ${suggestion.examSubject}",
                    color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Text("Ders toplamı: ${suggestion.wrongCount} yanlış · ${suggestion.blankCount} boş",
                    color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Text(
                    when {
                        suggestion.ambiguousMatch -> "Birden fazla kazanım eşleşiyor; otomatik bağlantı yapılmayacak."
                        suggestion.linkedOutcomeId != null -> "Mevcut öğrenme kazanımına bağlanacak."
                        else -> "Bu etiket için yeni bir özel kazanım oluşturulacak."
                    },
                    color = if (suggestion.ambiguousMatch) colors.error else colors.secondary,
                    style = MaterialTheme.typography.labelSmall
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        enabled = !suggestion.ambiguousMatch,
                        onClick = { onImport(suggestion) }
                    ) { Text("Tekrar listesine aktar") }
                }
            }
        }
        if (suggestions.size > 6) Text(
            "+ ${suggestions.size - 6} konu daha",
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        Text("Yanlış/boş sayıları ders toplamıdır; konuya özel sayı olarak yorumlanmaz.",
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}
