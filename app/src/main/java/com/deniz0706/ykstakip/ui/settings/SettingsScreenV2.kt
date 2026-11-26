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
import com.deniz0706.ykstakip.ui.components.DashboardPageHeader
import com.deniz0706.ykstakip.ui.components.DashboardRing
import com.deniz0706.ykstakip.ui.components.DashboardProgress
import com.deniz0706.ykstakip.ui.components.DashboardSectionHeading

@Composable
fun SettingsScreenV2(
    selectedTheme: String,
    onThemeChange: (String) -> Unit,
    tyt: String,
    ayt: String,
    obp: String,
    daily: String,
    onSave: (String, String, String, String) -> String?,
    aiBaseUrl: String,
    aiModel: String,
    hasAiKey: Boolean,
    onSaveAi: (String, String, String) -> String?,
    onTestAi: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onImportStudy: () -> Unit,
    onExportStudy: () -> Unit,
    onUndoStudyImport: () -> Unit,
    learningZipBusy: Boolean,
    onExportLearningZip: () -> Unit,
    onImportLearningZip: () -> Unit,
    fullBackupBusy: Boolean,
    onExportFullBackup: () -> Unit,
    onImportFullBackup: () -> Unit
) {
    var tytValue by remember { mutableStateOf(tyt) }
    var aytValue by remember { mutableStateOf(ayt) }
    var obpValue by remember { mutableStateOf(obp) }
    var dailyValue by remember { mutableStateOf(daily) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }
    var aiKey by remember { mutableStateOf("") }
    var aiBase by remember { mutableStateOf(aiBaseUrl) }
    var aiModelValue by remember { mutableStateOf(aiModel) }
    var aiError by remember { mutableStateOf<String?>(null) }
    var aiSaved by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Column(
        Modifier.fillMaxSize().background(colors.background).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 26.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        DashboardPageHeader(
            "KİŞİSELLEŞTİRME",
            "Ayarlar",
            "Hedeflerini, görünümü ve verilerini tek yerden yönet."
        )

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
            DashboardSectionHeading("Görünüm", detail = "Uygulamanın renk temasını seç.")
            Spacer(Modifier.height(14.dp))
            ThemePreviewSelector(
                selected = selectedTheme,
                onSelect = onThemeChange,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text("Açık, sıcak ve ferah bir yüzey kullanır. Koyu nötr grafit; Aurora ise derin lacivert, canlı mint, lila ve elektrik mavisi vurgularıyla hazırlanmıştır.",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
        }

        DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
            Text("02  /  HEDEFLER", style = MaterialTheme.typography.labelSmall, color = colors.secondary, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            DashboardSectionHeading("Hedeflerin", detail = "Sınav ve günlük çalışma hedeflerini düzenle.")
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

        DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
            Text("03  /  YAPAY ZEKÂ KOÇU", style = MaterialTheme.typography.labelSmall, color = colors.secondary, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            DashboardSectionHeading("AI koç bağlantısı", detail = "Kendi sağlayıcı ayarlarını güvenle yönet.")
            Text("Kendi sağlayıcı anahtarını kullan. Anahtar Android Keystore ile şifrelenerek bu cihazda saklanır. Uygulama açılışında otomatik istek gönderilmez.", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = aiKey, onValueChange = { aiKey = it; aiError = null; aiSaved = false }, label = { Text(if (hasAiKey) "Yeni API anahtarı (boş bırak: mevcut kalsın)" else "NVIDIA API anahtarı") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), shape = RoundedCornerShape(16.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = aiBase, onValueChange = { aiBase = it; aiError = null }, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = aiModelValue, onValueChange = { aiModelValue = it; aiError = null }, label = { Text("Model adı") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp))
            Text(if (hasAiKey) "API anahtarı bu cihazda kayıtlı." else "Henüz API anahtarı eklenmedi.", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            if (aiError != null) Text(aiError.orEmpty(), color = colors.error, style = MaterialTheme.typography.bodySmall)
            if (aiSaved) Text("AI ayarları kaydedildi.", color = colors.primary, style = MaterialTheme.typography.bodySmall)
            Button(onClick = { aiError = onSaveAi(aiKey, aiBase, aiModelValue); if (aiError == null) { aiKey = ""; aiSaved = true } }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) { Text("AI ayarlarını kaydet") }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = onTestAi, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) { Text("Bağlantıyı test et") }
        }

        DashboardPanel(Modifier.fillMaxWidth()) {
            Text("04  /  VERİ YÖNETİMİ", style = MaterialTheme.typography.labelSmall, color = colors.secondary, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            DashboardSectionHeading("Verilerin", detail = "Yedekleme ve geri yükleme araçları.")
            Text("Tam yedek · ZIP", color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Denemeler, çalışma geçmişi, kazanımlar ve soru fotoğrafları tek dosyada. İçe aktarma mevcut kayıtları silmez; eksikleri birleştirir. API anahtarı dahil edilmez.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onExportFullBackup, enabled = !fullBackupBusy,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)) { Text("Tüm verileri yedekle · ZIP") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onImportFullBackup, enabled = !fullBackupBusy,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)) { Text("Tam yedeği güvenli biçimde birleştir") }
            if (fullBackupBusy) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Tam yedek hazırlanıyor…", color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = colors.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Text("Ayrı yedekler", color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("İstersen yalnızca tek bir veri grubunu da dışa aktarabilirsin.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Text("Deneme arşivini JSON dosyası olarak yedekle veya geri yükle.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onExport, modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)) { Text("Yedek oluştur · JSON") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)) { Text("Yedekten içe aktar") }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = colors.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Text("Çalışma geçmişi · JSON", color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Günlük çalışma saati ve soru sayısını yedekle ya da içe aktar. " +
                "Tahmini veriler işaretli kalır, mevcut günler varsayılan olarak korunur.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onExportStudy, modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)) { Text("Çalışma geçmişini yedekle · JSON") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onImportStudy, modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)) { Text("Çalışma geçmişini içe aktar") }
            TextButton(onClick = onUndoStudyImport) {
                Text("Son çalışma geçmişi içe aktarmasını geri al")
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = colors.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Text("Öğrenme arşivi · ZIP", color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Kazanım ilerlemeni, tekrar tarihlerini ve fotoğraflı soru defterini yedekler. " +
                "Denemeler bu ZIP'e dahil değildir; onlar için yukarıdaki JSON yedeğini de oluştur.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onExportLearningZip, enabled = !learningZipBusy,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)) {
                Text("Öğrenme arşivi oluştur · ZIP")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onImportLearningZip, enabled = !learningZipBusy,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)) {
                Text("Öğrenme ZIP'ini birleştir")
            }
            if (learningZipBusy) {
                Spacer(Modifier.height(9.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Fotoğraflar işleniyor…", color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
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
