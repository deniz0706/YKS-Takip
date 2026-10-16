package com.deniz0706.ykstakip.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
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
    var savedSuccessfully by remember { mutableStateOf(false) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, store) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) state = store.snapshot()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

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
                    savedSuccessfully = false
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
                    val result = store.saveToToday()
                    state = store.snapshot()
                    when (result) {
                        is FocusSessionRepository.SaveResult.Saved -> {
                            onSaved()
                            savedSuccessfully = true
                            message = when {
                                result.loggedMinutes > 0 && result.carriedSeconds > 0 ->
                                    "${result.loggedMinutes} dk günlüğe eklendi; ${result.carriedSeconds} sn sonraki seansa taşındı."
                                result.loggedMinutes > 0 ->
                                    "${result.loggedMinutes} dk bugünkü çalışma günlüğüne eklendi."
                                else ->
                                    "Kalan ${result.carriedSeconds} sn kaydedildi; sonraki seansa eklenecek."
                            }
                        }
                        FocusSessionRepository.SaveResult.NoTime -> {
                            savedSuccessfully = false
                            message = "Önce sayacı başlat."
                        }
                        FocusSessionRepository.SaveResult.DailyLimit -> {
                            savedSuccessfully = false
                            message = "Günlük çalışma süresi 1440 dakikayı aşamaz."
                        }
                    }
                },
                // Saving a paused session must never require completing the preset
                // or even its first full minute.
                enabled = !state.running && state.elapsedMs > 0L,
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.weight(1f).height(49.dp)
            ) { Text("✓  Kaydet", maxLines = 1) }
        }
        if (state.elapsedMs > 0L) {
            TextButton(onClick = {
                state = store.discard()
                message = null
                savedSuccessfully = false
            }, modifier = Modifier.align(Alignment.End)) {
                Text("Seansı sıfırla", color = colors.error,
                    style = MaterialTheme.typography.labelMedium)
            }
        }
        if (message != null) {
            Text(message.orEmpty(),
                color = if (savedSuccessfully) colors.primary else colors.error,
                style = MaterialTheme.typography.bodySmall)
        }
        if (state.elapsedMs == 0L) {
            Text("Duraklatıp istediğin an kaydet. Tam dakikaya ulaşmayan saniyeler sonraki seansa eklenir.",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}
