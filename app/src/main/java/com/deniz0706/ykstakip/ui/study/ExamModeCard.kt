package com.deniz0706.ykstakip.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.data.ExamSessionRepository
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.util.ExamSessionMath
import kotlinx.coroutines.delay

/** Full-length exam clock, independent from the daily focus timer. */
@Composable
fun ExamModeCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember(context) { ExamSessionRepository(context) }
    var state by remember(repository) { mutableStateOf(repository.snapshot()) }
    LaunchedEffect(state.running) {
        while (state.running) {
            delay(1000L)
            state = repository.snapshot()
        }
    }
    val subjects = when (state.examType) {
        "TYT" -> listOf("Türkçe", "Sosyal", "Matematik", "Fen")
        "AYT" -> listOf("Matematik", "Fizik", "Kimya", "Biyoloji")
        else -> listOf("1. Ders", "2. Ders", "3. Ders")
    }
    val elapsedSeconds = (state.elapsedMs / 1000L).toInt()
    val remainingSeconds = ((state.remainingMs + 999L) / 1000L).toInt()
    val clock = ExamSessionMath.formatClock(remainingSeconds)
    val progress = (state.elapsedMs.toFloat() / (state.durationMinutes * 60_000L))
        .coerceIn(0f, 1f)
    val colors = MaterialTheme.colorScheme

    DashboardPanel(modifier, accent = true) {
        Text("GERÇEK DENEME MODU", color = colors.secondary,
            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.1f)
        Spacer(Modifier.height(5.dp))
        Text("Sınav süreni yönet", color = colors.onSurface,
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Uygulamadan çıksan da oturum ve süre korunur.",
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("TYT", "AYT", "BRANŞ").forEach { type ->
                FilterChip(
                    selected = state.examType == type,
                    enabled = !state.running && state.elapsedMs == 0L,
                    onClick = { state = repository.configure(type) },
                    label = { Text(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Text(clock, color = if (state.finished) colors.error else colors.onSurface,
            style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("${state.durationMinutes} dakika · geçen ${ExamSessionMath.formatClock(elapsedSeconds)}",
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress }, modifier = Modifier.fillMaxWidth(),
            color = colors.primary, trackColor = colors.surfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { state = if (state.running) repository.pause() else repository.start() },
                enabled = !state.finished,
                modifier = Modifier.weight(1f)
            ) { Text(if (state.running) "Duraklat" else if (state.elapsedMs > 0L) "Devam et" else "Başlat") }
            OutlinedButton(
                onClick = { state = repository.reset() },
                enabled = !state.running && state.elapsedMs > 0L,
                modifier = Modifier.weight(1f)
            ) { Text("Sıfırla") }
        }
        Text("Ders geçişi kaydet", color = colors.onSurface,
            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            subjects.forEach { subject ->
                FilterChip(
                    selected = false,
                    enabled = state.elapsedMs > 0L,
                    onClick = { state = repository.recordSplit(subject) },
                    label = { Text(subject, maxLines = 1) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        state.splits.takeLast(4).forEach { split ->
            Text("${split.label} · ${ExamSessionMath.formatClock((split.elapsedMs / 1000L).toInt())}",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
        if (state.finished) Text("Sınav süresi tamamlandı.",
            color = colors.error, fontWeight = FontWeight.SemiBold)
    }
}
