package com.deniz0706.ykstakip.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.ui.components.YksCard

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
        Text("Hedeflerini ve uygulamanın görünümünü yönet.",
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)

        YksCard(Modifier.fillMaxWidth()) {
            Text("Görünüm", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("light" to "Açık", "dark" to "Koyu", "system" to "Sistem").forEachIndexed { index, (value, label) ->
                    SegmentedButton(
                        selected = selectedTheme == value,
                        onClick = { onThemeChange(value) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                        label = { Text(label, maxLines = 1) }
                    )
                }
            }
        }

        YksCard(Modifier.fillMaxWidth(), highlighted = true) {
            Text("Hedeflerin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
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

        YksCard(Modifier.fillMaxWidth()) {
            Text("Verilerin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
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
