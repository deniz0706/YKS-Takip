package com.deniz0706.ykstakip.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.ui.components.YksCard
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.ui.components.DashboardRing
import com.deniz0706.ykstakip.ui.components.DashboardProgress

@Composable
fun SettingsScreenV2(
    selectedTheme: String,
    onThemeChange: (String) -> Unit,
    tyt: String,
    ayt: String,
    obp: String,
    daily: String,
    onSave: (String, String, String, String) -> String?,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    var tytValue by remember { mutableStateOf(tyt) }
    var aytValue by remember { mutableStateOf(ayt) }
    var obpValue by remember { mutableStateOf(obp) }
    var dailyValue by remember { mutableStateOf(daily) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Column(
        Modifier.fillMaxSize().background(colors.background).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 26.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("KİŞİSELLEŞTİRME", color = colors.secondary, style = MaterialTheme.typography.labelLarge)
        Text("Ayarlar", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
        Text("Hedeflerini, görünümü ve verilerini tek yerden yönet.",
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)

        DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
            Text("HEDEF PANOSU", color = colors.secondary,
                style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(5.dp))
            Text("Hedeflerini bir bakışta gör", color = colors.onSurface,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Aşağıdaki göstergeler hedeflerinin sınavın tam puanına oranını gösterir.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            val tytTarget = tytValue.replace(',', '.').toFloatOrNull()?.coerceIn(0f, 120f) ?: 0f
            val aytTarget = aytValue.replace(',', '.').toFloatOrNull()?.coerceIn(0f, 80f) ?: 0f
            val obpTarget = obpValue.replace(',', '.').toFloatOrNull()?.coerceIn(0f, 100f) ?: 0f
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DashboardRing(tytTarget / 120f, "${tytTarget.toInt()}", caption = "TYT", diameter = 78.dp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DashboardRing(aytTarget / 80f, "${aytTarget.toInt()}", caption = "AYT", diameter = 78.dp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DashboardRing(obpTarget / 100f, "${obpTarget.toInt()}", caption = "OBP", diameter = 78.dp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("GÜNLÜK SORU HEDEFİ", color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
            Spacer(Modifier.height(5.dp))
            Text(dailyValue.toIntOrNull()?.takeIf { it > 0 }?.let { "$it soru / gün" }
                ?: "Henüz belirlenmedi", color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        }

        DashboardPanel(Modifier.fillMaxWidth()) {
            Text("01  /  GÖRÜNÜM", style = MaterialTheme.typography.labelSmall, color = colors.secondary, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text("Görünüm", color = colors.onSurface,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            ThemePreviewSelector(
                selected = selectedTheme,
                onSelect = onThemeChange,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text("Açık özgün görünümünü korur. Aurora önceki lacivert–mint temamız; Koyu ise yeni grafit tasarım.",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
        }

        DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
            Text("02  /  HEDEFLER", style = MaterialTheme.typography.labelSmall, color = colors.secondary, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text("Hedeflerin", color = colors.onSurface,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Boş bıraktığın hedefler kaldırılır.", color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            @Composable
            fun field(value: String, change: (String) -> Unit, label: String) {
                OutlinedTextField(
                    value = value, onValueChange = { change(it); error = null; saved = false },
                    label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
                Spacer(Modifier.height(10.dp))
            }
            field(tytValue, { tytValue = it }, "TYT net hedefi · 0–120")
            field(aytValue, { aytValue = it }, "AYT net hedefi · 0–80")
            field(obpValue, { obpValue = it }, "OBP · 0–100")
            field(dailyValue, { dailyValue = it }, "Günlük soru hedefi")
            if (error != null) Text(error.orEmpty(), color = colors.error, style = MaterialTheme.typography.bodySmall)
            if (saved) Text("Hedeflerin kaydedildi.", color = colors.primary, style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = {
                    error = onSave(tytValue, aytValue, obpValue, dailyValue)
                    saved = error == null
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Hedefleri kaydet") }
        }

        DashboardPanel(Modifier.fillMaxWidth()) {
            Text("03  /  VERİ YÖNETİMİ", style = MaterialTheme.typography.labelSmall, color = colors.secondary, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text("Verilerin", color = colors.onSurface,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Deneme arşivini JSON dosyası olarak yedekle veya geri yükle.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onExport, modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)) { Text("Yedek oluştur · JSON") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)) { Text("Yedekten içe aktar") }
        }

        Text("GELİŞTİRİCİ", color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        Text(
            "01010011 01100101 01101110 01101001 00100000 11000011 10100111\n" +
                "01101111 01101011 00100000 01110011 01100101 01110110 01101001\n" +
                "01111001 01101111 01110010 01110101 01101101 00100000 01000101\n" +
                "01101100 01101001 01100110",
            fontFamily = FontFamily.Monospace, fontSize = 9.sp,
            color = colors.onSurfaceVariant.copy(alpha = .45f), lineHeight = 15.sp
        )
        Text("<3", color = colors.onSurfaceVariant.copy(alpha = .45f))
        Text("Developed by Deniz", color = colors.onSurfaceVariant.copy(alpha = .45f))
    }
}
