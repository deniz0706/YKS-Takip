package com.deniz0706.ykstakip.ui.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.components.ExamTypeTabs
import com.deniz0706.ykstakip.ui.components.YksCard
import com.deniz0706.ykstakip.util.Fmt

@Composable
fun HistoryScreenV2(
    exams: List<Exam>,
    onExamClick: (Long) -> Unit,
    onPickDate: (Boolean) -> Unit,
    fromDate: String?,
    toDate: String?,
    onClearDates: () -> Unit,
    modifier: Modifier = Modifier
) {
    var type by remember { mutableStateOf(ExamType.TYT) }
    var query by remember { mutableStateOf("") }
    val filtered = remember(exams, type, query, fromDate, toDate) {
        exams.filter { exam ->
            exam.type == type &&
                (query.isBlank() || exam.title.contains(query.trim(), ignoreCase = true) ||
                    exam.publisher.contains(query.trim(), ignoreCase = true)) &&
                (fromDate == null || exam.date >= fromDate) &&
                (toDate == null || exam.date <= toDate)
        }.sortedByDescending { it.date }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 42.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("DENEME ARŞİVİ", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelLarge)
            Text("Geçmiş", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
        item { ExamTypeTabs(type.name, { type = ExamType.valueOf(it) }) }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Deneme veya yayınevi ara") },
                leadingIcon = { Text("⌕", fontSize = 24.sp) },
                shape = RoundedCornerShape(18.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { onPickDate(true) }, modifier = Modifier.weight(1f)) {
                    Text(fromDate ?: "Başlangıç", maxLines = 1, fontSize = 12.sp)
                }
                OutlinedButton(onClick = { onPickDate(false) }, modifier = Modifier.weight(1f)) {
                    Text(toDate ?: "Bitiş", maxLines = 1, fontSize = 12.sp)
                }
                if (fromDate != null || toDate != null) {
                    TextButton(onClick = onClearDates) { Text("Sil") }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kayıtlar", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text("${filtered.size} deneme", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (filtered.isEmpty()) {
            item {
                YksCard {
                    Text("Burada henüz deneme görünmüyor", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Arama ve tarih filtrelerini kontrol edebilir veya yeni bir deneme ekleyebilirsin.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        items(filtered, key = { it.id }) { exam ->
            YksCard(Modifier.fillMaxWidth().clickable { onExamClick(exam.id) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(exam.title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(5.dp))
                        Text(exam.date + if (exam.publisher.isBlank()) "" else " · " + exam.publisher,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(Fmt.net(exam.totalNet), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("NET", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(13.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .4f))
                Spacer(Modifier.height(10.dp))
                Text("${exam.totalCorrect} doğru  ·  ${exam.totalWrong} yanlış  ·  ${exam.totalBlank} boş  ·  ${exam.totalTimeMinutes} dk",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
