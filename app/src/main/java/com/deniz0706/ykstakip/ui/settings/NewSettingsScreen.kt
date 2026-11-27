package com.deniz0706.ykstakip.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.components.YksSection
import com.deniz0706.ykstakip.ui.theme.YksSpacing

@Composable
fun NewSettingsScreen(
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
    val c = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = YksSpacing.pageHorizontal,
            vertical = YksSpacing.pageVertical
        ),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(YksSpacing.section)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ayarlar",
                    style = MaterialTheme.typography.displayMedium,
                    color = c.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kişiselleştirme ve veri",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onSurfaceVariant
                )
            }
        }

        // Appearance section
        item {
            YksSection(title = "Görünüm", detail = "Uygulama renk temasını seçin.")
            Spacer(modifier = Modifier.height(YksSpacing.sm))
            ThemeRow(
                selected = selectedTheme,
                onSelect = onThemeChange,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Goals section
        item {
            YksSection(title = "Hedefler", detail = "Sınav ve günlük çalışma hedeflerinizi ayarlayın.")
            Spacer(modifier = Modifier.height(YksSpacing.sm))
            GoalRow(
                tyt = tyt,
                ayt = ayt,
                obp = obp,
                daily = daily,
                onSave = onSave,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // AI section
        item {
            YksSection(title = "AI Koç Ayarları", detail = "NVIDIA AI bağlantınızı yapılandırın.")
            Spacer(modifier = Modifier.height(YksSpacing.sm))
            AiRow(
                aiBaseUrl = aiBaseUrl,
                aiModel = aiModel,
                hasAiKey = hasAiKey,
                onSaveAi = onSaveAi,
                onTestAi = onTestAi,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Data management section
        item {
            YksSection(title = "Veri Yönetimi", detail = "Yedekleme ve geri yükleme araçları.")
            Spacer(modifier = Modifier.height(YksSpacing.sm))
            DataRow(
                onExport = onExport,
                onImport = onImport,
                onImportStudy = onImportStudy,
                onExportStudy = onExportStudy,
                onUndoStudyImport = onUndoStudyImport,
                learningZipBusy = learningZipBusy,
                onExportLearningZip = onExportLearningZip,
                onImportLearningZip = onImportLearningZip,
                fullBackupBusy = fullBackupBusy,
                onExportFullBackup = onExportFullBackup,
                onImportFullBackup = onImportFullBackup,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}

@Composable
private fun ThemeRow(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier
) {
    val c = MaterialTheme.colorScheme
    val options = listOf(
        "light" to "Açık",
        "dark" to "Koyu",
        "aurora" to "Aurora",
        "system" to "Sistem"
    )
    options.chunked(2).forEach { row ->
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            row.forEach { (id, label) ->
                val isSelected = selected == id
                androidx.compose.foundation.layout.Column(
                    Modifier.weight(1f)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .background(if (isSelected) c.primaryContainer else c.surfaceContainer)
                        .clickable { onSelect(id) }
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
        }
    }
}

@Composable
private fun GoalRow(
    tyt: String,
    ayt: String,
    obp: String,
    daily: String,
    onSave: (String, String, String, String) -> String?,
    modifier: Modifier
) {
    val c = MaterialTheme.colorScheme
    Column(modifier.padding(horizontal = 12.dp)) {
        Text(
            text = "TYT net hedefi · 0–120",
            style = MaterialTheme.typography.labelSmall,
            color = c.onSurfaceVariant
        )
        androidx.compose.material3.OutlinedTextField(
            value = tyt,
            onValueChange = {},
            label = { Text("TYT") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "AYT net hedefi · 0–80",
            style = MaterialTheme.typography.labelSmall,
            color = c.onSurfaceVariant
        )
        androidx.compose.material3.OutlinedTextField(
            value = ayt,
            onValueChange = {},
            label = { Text("AYT") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "OBP · 0–100",
            style = MaterialTheme.typography.labelSmall,
            color = c.onSurfaceVariant
        )
        androidx.compose.material3.OutlinedTextField(
            value = obp,
            onValueChange = {},
            label = { Text("OBP") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Günlük soru hedefi",
            style = MaterialTheme.typography.labelSmall,
            color = c.onSurfaceVariant
        )
        androidx.compose.material3.OutlinedTextField(
            value = daily,
            onValueChange = {},
            label = { Text("Günlük") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        androidx.compose.material3.Button(
            onClick = { onSave(tyt, ayt, obp, daily) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kaydet")
        }
    }
}

@Composable
private fun AiRow(
    aiBaseUrl: String,
    aiModel: String,
    hasAiKey: Boolean,
    onSaveAi: (String, String, String) -> String?,
    onTestAi: () -> Unit,
    modifier: Modifier
) {
    val c = MaterialTheme.colorScheme
    Column(modifier.padding(horizontal = 12.dp)) {
        Text(
            text = "API anahtarı ${if (hasAiKey) "bu cihazda kayıtlı." else "henüz eklenmedi."}",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.OutlinedTextField(
            value = aiBaseUrl,
            onValueChange = {},
            label = { Text("Base URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.OutlinedTextField(
            value = aiModel,
            onValueChange = {},
            label = { Text("Model adı") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.Button(
                onClick = { onSaveAi("", aiBaseUrl, aiModel) },
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
    }
}

@Composable
private fun DataRow(
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
    onImportFullBackup: () -> Unit,
    modifier: Modifier
) {
    val c = MaterialTheme.colorScheme
    Column(modifier.padding(horizontal = 12.dp)) {
        Text(
            text = "Tam yedek · ZIP",
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
        androidx.compose.material3.Button(
            onClick = onExportFullBackup,
            enabled = !fullBackupBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tüm verileri yedekle · ZIP")
        }
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = onImportFullBackup,
            enabled = !fullBackupBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tam yedeği birleştir")
        }

        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.HorizontalDivider(
            color = c.outlineVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Çalışma geçmişi · JSON",
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.Button(
            onClick = onExportStudy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Çalışma geçmişini yedekle")
        }
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = onImportStudy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Çalışma geçmişini içe aktar")
        }
    }
}
