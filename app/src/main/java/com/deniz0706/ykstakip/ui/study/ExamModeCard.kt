package com.deniz0706.ykstakip.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.data.ExamSessionRepository
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.SubjectConfigs
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.util.ExamSessionMath

/**
 * Exam metadata only. The timer display, start/pause, save and reset controls
 * live exclusively in FocusSessionCard and use the shared FocusSessionRepository.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamModeCard(
    modifier: Modifier = Modifier,
    embedded: Boolean = false,
    timerRunning: Boolean,
    timerElapsedMs: Long,
    onTimerChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember(context) { ExamSessionRepository(context) }
    var state by remember(repository) { mutableStateOf(repository.snapshot()) }
    var showDurationDialog by remember { mutableStateOf(false) }
    var hours by remember(state.durationMinutes) { mutableStateOf((state.durationMinutes / 60).toString()) }
    var minutes by remember(state.durationMinutes) { mutableStateOf((state.durationMinutes % 60).toString()) }
    var durationError by remember { mutableStateOf<String?>(null) }
    var aytField by remember { mutableStateOf(AytField.SAYISAL) }
    val colors = MaterialTheme.colorScheme

    val subjects = when (state.examType) {
        "TYT" -> listOf("Türkçe", "Sosyal Bilimler", "Matematik", "Fen Bilimleri")
        "AYT" -> SubjectConfigs.subjectsFor(aytField).map { it.name }
        else -> listOf("Türkçe", "Sosyal Bilimler", "Matematik", "Fen Bilimleri",
            "Edebiyat", "Tarih-1", "Coğrafya-1", "Tarih-2", "Coğrafya-2",
            "Felsefe Grubu", "Din Kültürü / Ek Felsefe")
    }

    val content: @Composable () -> Unit = {
        Text("SINAV TÜRÜ", color = colors.secondary,
            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.1.sp)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("TYT", "AYT", "BRANŞ").forEach { type ->
                FilterChip(
                    selected = state.examType == type,
                    enabled = !timerRunning && timerElapsedMs == 0L,
                    onClick = {
                        state = repository.configure(type)
                        onTimerChanged()
                        if (type == "BRANŞ") showDurationDialog = true
                    },
                    label = { Text(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.examType == "AYT") {
            Text("AYT ALANI", color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    AytField.SAYISAL to "Sayısal",
                    AytField.ESIT_AGIRLIK to "Eşit Ağırlık",
                    AytField.SOZEL to "Sözel"
                ).forEach { (field, label) ->
                    FilterChip(
                        selected = aytField == field,
                        onClick = { aytField = field },
                        enabled = !timerRunning && timerElapsedMs == 0L,
                        label = { Text(label) }
                    )
                }
            }
        }
        if (state.examType == "BRANŞ") {
            OutlinedButton(
                onClick = {
                    hours = (state.durationMinutes / 60).toString()
                    minutes = (state.durationMinutes % 60).toString()
                    durationError = null
                    showDurationDialog = true
                },
                enabled = !timerRunning && timerElapsedMs == 0L,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Branş süresini ayarla · ${state.durationMinutes} dk") }
        }
        Text("Ders geçişi işaretle", color = colors.onSurface,
            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            subjects.forEach { subject ->
                FilterChip(
                    selected = false,
                    enabled = timerElapsedMs > 0L,
                    onClick = { state = repository.recordSplit(subject) },
                    label = { Text(subject, maxLines = 1) }
                )
            }
        }
        state.splits.takeLast(6).forEach { split ->
            Text("${split.label} · ${ExamSessionMath.formatClock(split.elapsedMs / 1000L)}",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }

    if (embedded) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
    } else {
        DashboardPanel(modifier, accent = true) { content() }
    }

    if (showDurationDialog) {
        AlertDialog(
            onDismissRequest = { showDurationDialog = false },
            title = { Text("Branş süresi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1 dakika ile 10 saat arasında istediğin süreyi belirle.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(hours, { hours = it.filter(Char::isDigit).take(2); durationError = null },
                            label = { Text("Saat") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f))
                        OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(2); durationError = null },
                            label = { Text("Dakika") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f))
                    }
                    if (durationError != null) Text(durationError.orEmpty(), color = colors.error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = hours.toIntOrNull()
                    val m = minutes.toIntOrNull()
                    val total = if (h != null && m != null) h * 60 + m else 0
                    if (h == null || m == null || h !in 0..10 || m !in 0..59 ||
                        total !in ExamSessionMath.MIN_DURATION_MINUTES..ExamSessionMath.MAX_DURATION_MINUTES) {
                        durationError = "1–600 dakika arasında geçerli bir süre gir."
                    } else {
                        state = repository.configure("BRANŞ", total)
                        onTimerChanged()
                        showDurationDialog = false
                    }
                }) { Text("Süreyi uygula") }
            },
            dismissButton = {
                TextButton(onClick = { showDurationDialog = false }) { Text("Vazgeç") }
            }
        )
    }
}
