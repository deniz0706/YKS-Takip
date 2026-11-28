package com.deniz0706.ykstakip.ui.settings

import com.deniz0706.ykstakip.ui.theme.clayCanvas
import com.deniz0706.ykstakip.ui.theme.clayRaised
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.ui.theme.YksTokens

/** Premium settings screen for YKS Takip 4.0. */
@Composable
fun PremiumSettingsScreen(
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
    onImportStudy: () -> Unit,
    onExportStudy: () -> Unit,
    onUndoStudyImport: () -> Unit,
    learningZipBusy: Boolean,
    onExportLearningZip: () -> Unit,
    onImportLearningZip: () -> Unit,
    fullBackupBusy: Boolean,
    onExportFullBackup: () -> Unit,
    onImportFullBackup: () -> Unit,
    colorScheme: ColorScheme
) {
    val c = colorScheme

    LazyColumn(
        modifier = Modifier.clayCanvas(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = YksTokens.Space.pageHorizontal,
            vertical = YksTokens.Space.pageVertical
        ),
        verticalArrangement = Arrangement.spacedBy(YksTokens.Space.section)
    ) {
        // Header
        item {
            PremiumSectionHeader(
                title = "Ayarlar",
                subtitle = "Kişiselleştirme ve veri",
                colorScheme = c
            )
        }

        // Appearance section
        item {
            PremiumSettingsCard(
                title = "Görünüm",
                subtitle = "Uygulama renk temasını seçin.",
                colorScheme = c
            ) {
                Text(
                    text = "Tema galerisi",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                ThemePreviewSelector(
                    selected = selectedTheme,
                    onSelect = onThemeChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { Spacer(modifier = Modifier.height(YksTokens.Space.sm)) }

        // Goals section
        item {
            PremiumSettingsCard(
                title = "Hedefler",
                subtitle = "Sınav ve günlük çalışma hedeflerinizi ayarlayın.",
                colorScheme = c
            ) {
                PremiumGoalRow(
                    tyt = tyt,
                    ayt = ayt,
                    obp = obp,
                    daily = daily,
                    onSave = onSave,
                    colorScheme = c
                )
            }
        }

        item { Spacer(modifier = Modifier.height(YksTokens.Space.sm)) }

        // AI section
        item {
            PremiumSettingsCard(
                title = "AI Koç Ayarları",
                subtitle = "NVIDIA AI bağlantınızı yapılandırın.",
                colorScheme = c
            ) {
                PremiumAiRow(
                    aiBaseUrl = aiBaseUrl,
                    aiModel = aiModel,
                    hasAiKey = hasAiKey,
                    onSaveAi = onSaveAi,
                    onTestAi = onTestAi,
                    colorScheme = c
                )
            }
        }

        item { Spacer(modifier = Modifier.height(YksTokens.Space.sm)) }

        // Data management section
        item {
            PremiumSettingsCard(
                title = "Veri Yönetimi",
                subtitle = "Yedekleme ve geri yükleme araçları.",
                colorScheme = c
            ) {
                PremiumDataRow(
                    onImportStudy = onImportStudy,
                    onExportStudy = onExportStudy,
                    onUndoStudyImport = onUndoStudyImport,
                    learningZipBusy = learningZipBusy,
                    onExportLearningZip = onExportLearningZip,
                    onImportLearningZip = onImportLearningZip,
                    fullBackupBusy = fullBackupBusy,
                    onExportFullBackup = onExportFullBackup,
                    onImportFullBackup = onImportFullBackup,
                    colorScheme = c
                )
            }
        }

        item { Spacer(modifier = Modifier.height(120.dp)) }
    }
}

@Composable
private fun PremiumSectionHeader(
    title: String,
    subtitle: String,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = c.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PremiumSettingsCard(
    title: String,
    subtitle: String,
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    val c = colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clayRaised(shape = RoundedCornerShape(YksTokens.Shape.panel), depth = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(YksTokens.Shape.panel),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 6.dp),

        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(YksTokens.Space.cardContent)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = c.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun PremiumThemeRow(
    selected: String,
    onSelect: (String) -> Unit,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    val options = listOf(
        "light" to "Açık",
        "dark" to "Koyu",
        "aurora" to "Aurora",
        "system" to "Sistem"
    )
    options.chunked(2).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            row.forEach { (id, label) ->
                val isSelected = selected == id
                PremiumThemeChip(
                    id = id,
                    label = label,
                    isSelected = isSelected,
                    onSelect = { onSelect(id) },
                    colorScheme = c,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PremiumThemeChip(
    id: String,
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    val c = colorScheme
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .clayRaised(shape = RoundedCornerShape(YksTokens.Shape.input), depth = 5.dp,
                tint = if (isSelected) c.primaryContainer else c.surfaceContainer)
            .clip(RoundedCornerShape(YksTokens.Shape.input))
            .clickable { onSelect() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSelected) "✓ $label" else label,
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PremiumGoalRow(
    tyt: String,
    ayt: String,
    obp: String,
    daily: String,
    onSave: (String, String, String, String) -> String?,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    var tytValue by remember { mutableStateOf(tyt) }
    var aytValue by remember { mutableStateOf(ayt) }
    var obpValue by remember { mutableStateOf(obp) }
    var dailyValue by remember { mutableStateOf(daily) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        PremiumInputField(
            label = "TYT net hedefi · 0–120",
            value = tytValue,
            onValueChange = { tytValue = it },
            placeholder = "TYT",
            colorScheme = c
        )
        Spacer(modifier = Modifier.height(12.dp))
        PremiumInputField(
            label = "AYT net hedefi · 0–80",
            value = aytValue,
            onValueChange = { aytValue = it },
            placeholder = "AYT",
            colorScheme = c
        )
        Spacer(modifier = Modifier.height(12.dp))
        PremiumInputField(
            label = "OBP · 0–100",
            value = obpValue,
            onValueChange = { obpValue = it },
            placeholder = "OBP",
            colorScheme = c
        )
        Spacer(modifier = Modifier.height(12.dp))
        PremiumInputField(
            label = "Günlük soru hedefi",
            value = dailyValue,
            onValueChange = { dailyValue = it },
            placeholder = "Günlük",
            colorScheme = c
        )
        Spacer(modifier = Modifier.height(16.dp))
        val tytValid = tytValue.isBlank() || (tytValue.toFloatOrNull()?.let { it in 0f..120f } == true)
        val aytValid = aytValue.isBlank() || (aytValue.toFloatOrNull()?.let { it in 0f..80f } == true)
        val obpValid = obpValue.isBlank() || (obpValue.toFloatOrNull()?.let { it in 0f..100f } == true)
        val dailyValid = dailyValue.isBlank() || (dailyValue.toIntOrNull()?.let { it in 0..5000 } == true)
        val valuesValid = tytValid && aytValid && obpValid && dailyValid
        androidx.compose.material3.Button(
            onClick = {
                saveMessage = if (!valuesValid) {
                    "TYT 0–120, AYT 0–80, OBP 0–100 ve günlük hedef 0–5000 aralığında olmalı."
                } else {
                    onSave(tytValue, aytValue, obpValue, dailyValue) ?: "Hedefler kaydedildi."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kaydet")
        }
        saveMessage?.let { message ->
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = if (valuesValid) c.primary else c.error
            )
        }
    }
}

@Composable
private fun PremiumInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = c.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PremiumAiRow(
    aiBaseUrl: String,
    aiModel: String,
    hasAiKey: Boolean,
    onSaveAi: (String, String, String) -> String?,
    onTestAi: () -> Unit,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    var baseUrlValue by remember { mutableStateOf(aiBaseUrl) }
    var modelValue by remember { mutableStateOf(aiModel) }
    var keyValue by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Text(
            text = "API anahtarı ${if (hasAiKey) "bu cihazda kayıtlı." else "henüz eklenmedi."}",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = keyValue,
            onValueChange = { keyValue = it; saveMessage = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("API anahtarı") },
            placeholder = { Text(if (hasAiKey) "Değiştirmek için yeni anahtar girin" else "API anahtarınızı girin") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(12.dp))
        PremiumInputField(
            label = "Base URL",
            value = baseUrlValue,
            onValueChange = { baseUrlValue = it },
            placeholder = "https://",
            colorScheme = c
        )
        Spacer(modifier = Modifier.height(12.dp))
        PremiumInputField(
            label = "Model adı",
            value = modelValue,
            onValueChange = { modelValue = it },
            placeholder = "Model",
            colorScheme = c
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.Button(
                onClick = {
                    saveMessage = onSaveAi(keyValue, baseUrlValue, modelValue)
                    if (saveMessage == null) keyValue = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Kaydet")
            }
            androidx.compose.material3.OutlinedButton(
                onClick = onTestAi,
                modifier = Modifier.weight(1f)
            ) {
                Text("Test et")
            }
        }
        saveMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = c.error)
        }
    }
}

@Composable
private fun PremiumDataRow(
    onImportStudy: () -> Unit,
    onExportStudy: () -> Unit,
    onUndoStudyImport: () -> Unit,
    learningZipBusy: Boolean,
    onExportLearningZip: () -> Unit,
    onImportLearningZip: () -> Unit,
    fullBackupBusy: Boolean,
    onExportFullBackup: () -> Unit,
    onImportFullBackup: () -> Unit,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        // Full backup section
        Text(
            text = "Tam Yedek · ZIP",
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Denemeler, çalışma geçmişi, kazanımlar ve soru fotoğrafları tek dosyada.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        PremiumActionButton(
            label = "Tüm verileri yedekle · ZIP",
            onClick = onExportFullBackup,
            enabled = !fullBackupBusy,
            accent = c.primary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        PremiumOutlinedActionButton(
            label = "Tam yedeği birleştir",
            onClick = onImportFullBackup,
            enabled = !fullBackupBusy,
            accent = c.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
        androidx.compose.material3.HorizontalDivider(
            color = c.outlineVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Study history section
        Text(
            text = "Çalışma Geçmişi · JSON",
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        PremiumActionButton(
            label = "Çalışma geçmişini yedekle",
            onClick = onExportStudy,
            accent = c.primary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        PremiumOutlinedActionButton(
            label = "Çalışma geçmişini içe aktar",
            onClick = onImportStudy,
            accent = c.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
        androidx.compose.material3.HorizontalDivider(color = c.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Öğrenme Arşivi · ZIP",
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Kazanımlar, soru kartları ve bağlı fotoğrafları dışa aktarın veya geri yükleyin.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        PremiumActionButton(
            label = if (learningZipBusy) "Arşiv hazırlanıyor…" else "Öğrenme arşivini dışa aktar",
            onClick = onExportLearningZip,
            enabled = !learningZipBusy,
            accent = c.primary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        PremiumOutlinedActionButton(
            label = if (learningZipBusy) "İşlem sürüyor…" else "Öğrenme arşivini içe aktar",
            onClick = onImportLearningZip,
            enabled = !learningZipBusy,
            accent = c.primary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        PremiumOutlinedActionButton(
            label = "Son çalışma içe aktarımını geri al",
            onClick = onUndoStudyImport,
            accent = c.secondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PremiumActionButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Text(label)
    }
}

@Composable
private fun PremiumOutlinedActionButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Text(label)
    }
}