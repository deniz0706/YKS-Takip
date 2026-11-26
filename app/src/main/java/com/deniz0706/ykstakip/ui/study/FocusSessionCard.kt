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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
    var customDurationDialog by remember { mutableStateOf(false) }
    var manualTimeDialog by remember { mutableStateOf(false) }
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
    val seconds = if (state.stopwatch) state.elapsedMs / 1000L
        else (state.remainingMs + 999L) / 1000L
    val timeText = if (state.stopwatch || seconds >= 3600L) {
        "%02d:%02d:%02d".format(seconds / 3600L, (seconds / 60L) % 60L, seconds % 60L)
    } else "%02d:%02d".format(seconds / 60L, seconds % 60L)
    val progress = if (state.stopwatch) 0f
        else state.elapsedMs.toFloat() / state.totalMs.coerceAtLeast(1L)
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
                    state.completed -> "Geri sayım bitti. Çalışmanı günlüğe ekleyebilirsin."
                    state.limitReached -> "24 saatlik günlük üst sınıra ulaştın. Seansı kaydet."
                    state.running && state.stopwatch -> "Kronometre sürüyor; 60 dakikada durmaz."
                    state.running -> "Geri sayım çalışıyor; başka sayfaya geçsen de süre korunur."
                    state.elapsedMs > 0L -> "Duraklatıldı. Kaldığın yerden devam et."
                    state.stopwatch -> "Süresiz kronometreyi başlat. İstediğin an durdurup kaydet."
                    else -> "Geri sayım süresini seç ve odak seansını başlat."
                }, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(8.dp))
            DashboardRing(progress.coerceIn(0f, 1f), timeText,
                caption = if (state.stopwatch) "geçen"
                    else if (state.completed) "bitti" else "kalan",
                diameter = if (state.stopwatch || seconds >= 3600L) 122.dp else 104.dp)
        }

        Spacer(Modifier.height(15.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !state.stopwatch,
                onClick = {
                    state = store.chooseDuration(state.durationMinutes)
                    message = null
                },
                enabled = canChangeDuration,
                label = { Text("Geri sayım") }, modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = state.stopwatch,
                onClick = {
                    state = store.chooseStopwatch()
                    message = null
                },
                enabled = canChangeDuration,
                label = { Text("Kronometre") }, modifier = Modifier.weight(1f)
            )
        }
        if (!state.stopwatch) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(25, 45, 60, 120).forEach { duration ->
                    FilterChip(
                        selected = duration == state.durationMinutes,
                        enabled = canChangeDuration,
                        onClick = {
                            state = store.chooseDuration(duration)
                            message = null
                        },
                        label = { Text("$duration") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            OutlinedButton(
                onClick = { customDurationDialog = true },
                enabled = canChangeDuration,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Özel geri sayım süresi · ${state.durationMinutes} dk") }
        } else {
            Text("Hedef süre yok. Kronometre 60 veya 120 dakikada durmaz; " +
                "yalnızca 24 saatlik günlük güvenlik sınırı vardır.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    state = if (state.running) store.pause() else store.start()
                    message = null
                    savedSuccessfully = false
                },
                enabled = !state.completed && !state.limitReached,
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
        OutlinedButton(onClick = { manualTimeDialog = true },
            modifier = Modifier.fillMaxWidth()) {
            Text("+ Bugüne elle çalışma süresi ekle")
        }
        if (message != null) {
            Text(message.orEmpty(),
                color = if (savedSuccessfully) colors.primary else colors.error,
                style = MaterialTheme.typography.bodySmall)
        }
        if (state.elapsedMs == 0L) {
            Text("Duraklatıp istediğin an kaydet. Tam dakikaya ulaşmayan saniyeler " +
                "aynı günkü sonraki seansa aktarılır. Elle girdiğin süreyi tekrar sayaçtan kaydetme.",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
        }
    }

    if (customDurationDialog) {
        var hours by remember { mutableStateOf("2") }
        var minutes by remember { mutableStateOf("0") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { customDurationDialog = false },
            title = { Text("Özel geri sayım") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1 dakika – 24 saat arası süre belirle.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(hours, { hours = it; error = null },
                            label = { Text("Saat") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f))
                        OutlinedTextField(minutes, { minutes = it; error = null },
                            label = { Text("Dakika") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f))
                    }
                    if (error != null) Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = hours.toIntOrNull()
                    val m = minutes.toIntOrNull()
                    if (h == null || m == null || h !in 0..24 || m !in 0..59 ||
                        h * 60 + m !in 1..FocusSessionRepository.MAX_MINUTES) {
                        error = "Geçerli bir saat ve dakika gir (en fazla 24 saat)."
                    } else {
                        state = store.chooseDuration(h * 60 + m)
                        message = null
                        customDurationDialog = false
                    }
                }) { Text("Süreyi seç") }
            },
            dismissButton = {
                TextButton(onClick = { customDurationDialog = false }) { Text("Vazgeç") }
            }
        )
    }

    if (manualTimeDialog) {
        var hours by remember { mutableStateOf("0") }
        var minutes by remember { mutableStateOf("0") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { manualTimeDialog = false },
            title = { Text("Bugüne süre ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Girdiğin süre bugünkü çalışma toplamına EKLENİR. " +
                        "Aktif sayaç ayrıca kaydedilirse aynı süreyi iki kez saymamaya dikkat et.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(hours, { hours = it; error = null },
                            label = { Text("Saat") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f))
                        OutlinedTextField(minutes, { minutes = it; error = null },
                            label = { Text("Dakika") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f))
                    }
                    if (error != null) Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = hours.toIntOrNull()
                    val m = minutes.toIntOrNull()
                    if (h == null || m == null || h !in 0..24 || m !in 0..59 ||
                        h * 60 + m !in 1..FocusSessionRepository.MAX_MINUTES) {
                        error = "Geçerli bir süre gir (1 dakika – 24 saat)."
                    } else {
                        when (store.addManualMinutes(h * 60 + m)) {
                            is FocusSessionRepository.SaveResult.Saved -> {
                                onSaved()
                                savedSuccessfully = true
                                message = "${h * 60 + m} dk bugünkü çalışma toplamına eklendi."
                                manualTimeDialog = false
                            }
                            FocusSessionRepository.SaveResult.DailyLimit ->
                                error = "Bugünün toplam çalışma süresi 24 saati aşamaz."
                            FocusSessionRepository.SaveResult.NoTime ->
                                error = "Süre sıfırdan büyük olmalı."
                        }
                    }
                }) { Text("Bugüne ekle") }
            },
            dismissButton = {
                TextButton(onClick = { manualTimeDialog = false }) { Text("Vazgeç") }
            }
        )
    }
}
