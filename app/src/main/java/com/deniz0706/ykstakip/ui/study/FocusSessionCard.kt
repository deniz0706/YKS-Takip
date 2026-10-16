package com.deniz0706.ykstakip.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.data.FocusSessionRepository
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.ui.components.DashboardRing
import kotlinx.coroutines.delay

/**
 * Counts real elapsed time, not composition frames. Persists start/pause state in
 * SharedPreferences so moving to another screen does not discard a focus session.
 * The countdown only recomposes this widget once per second, never the whole journal.
 */
@Composable
fun FocusSessionCard(onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(context) { FocusSessionRepository(context) }
    var state by remember(store) { mutableStateOf(store.snapshot()) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.running) {
        while (state.running) {
            delay(1000L)
            state = store.snapshot()
        }
    }

    val colors = MaterialTheme.colorScheme
    val remainingSeconds = (state.remainingMs + 999L) / 1000L
    val timeText = "%02d:%02d".format(remainingSeconds / 60L, remainingSeconds % 60L)
    val progress = state.elapsedMs.toFloat() / state.totalMs.coerceAtLeast(1L)
    val canChangeDuration = state.elapsedMs == 0L && !state.running

    DashboardPanel(modifier, accent = true) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ODAK SEANSI", color = colors.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(7.dp))
                Text("Dikkatini tek noktaya topla", color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(when {
                    state.completed -> "Süre doldu. Çalışmanı günlüğe ekleyebilirsin."
                    state.running -> "Sayaç çalışıyor; başka sayfaya geçsen de süren korunur."
                    state.elapsedMs > 0L -> "Duraklatıldı. Kaldığın yerden devam et."
                    else -> "Süre seç ve odak seansını başlat."
                }, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(8.dp))
            DashboardRing(progress.coerceIn(0f, 1f), timeText,
                caption = if (state.completed) "bitti" else "kalan",
                diameter = 104.dp)
        }

        Spacer(Modifier.height(15.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(25, 45, 60).forEach { duration ->
                FilterChip(
                    selected = duration == state.durationMinutes,
                    enabled = canChangeDuration,
                    onClick = {
                        state = store.chooseDuration(duration)
                        message = null
                    },
                    label = { Text("$duration dk") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    state = if (state.running) store.pause() else store.start()
                    message = null
                },
                enabled = !state.completed,
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.weight(1f).height(49.dp)
            ) {
                Text(if (state.running) "Ⅱ  Duraklat"
                    else if (state.elapsedMs > 0) "▶  Devam et" else "▶  Başlat",
                    maxLines = 1)
            }
            OutlinedButton(
                onClick = {
                    val saved = store.saveToToday()
                    state = store.snapshot()
                    if (saved != null) {
                        onSaved()
                        message = "$saved dakika bugünkü çalışma kaydına eklendi."
                    } else {
                        message = if (state.savedMinutes < 1)
                            "Kaydetmek için en az 1 dakika çalışmalısın."
                        else "Bugünkü toplam çalışma 1440 dakikayı geçemez."
                    }
                },
                enabled = state.elapsedMs >= 60_000L,
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.weight(1f).height(49.dp)
            ) { Text("✓  Kaydet", maxLines = 1) }
        }
        if (state.elapsedMs > 0L) {
            TextButton(onClick = {
                state = store.discard()
                message = null
            }, modifier = Modifier.align(Alignment.End)) {
                Text("Seansı sıfırla", color = colors.error,
                    style = MaterialTheme.typography.labelMedium)
            }
        }
        if (message != null) {
            Text(message.orEmpty(),
                color = if (message!!.contains("eklendi")) colors.primary else colors.error,
                style = MaterialTheme.typography.bodySmall)
        }
        if (state.elapsedMs == 0L) {
            Text("Kaydedilen süre, günlük çalışma dakikana eklenir. Soru sayın değişmez.",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}
