package com.deniz0706.ykstakip.ui.ai

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.ui.components.YksSection
import com.deniz0706.ykstakip.ui.theme.YksSpacing

data class CoachLine(val role: String, val text: String)

@Composable
fun NewAiCoachScreen(
    lines: List<CoachLine>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    sending: Boolean = false,
    hasApiKey: Boolean = false,
    onApiKeyMissing: () -> Unit
) {
    val c = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp, vertical = 12.dp
        ),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "AI Koçun",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "YKS sürecini birlikte yönetelim",
                        style = MaterialTheme.typography.titleLarge,
                        color = c.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Denemeleriniz ve çalışma kayıtlarınız bağlam sağlar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.onSurfaceVariant
                    )
                }
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    color = c.secondaryContainer
                ) {
                    Text(
                        text = "NVIDIA AI",
                        Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = c.onSecondaryContainer
                    )
                }
            }
        }

        if (!hasApiKey) {
            item {
                Surface(
                    color = c.errorContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Sohbet için Ayarlar bölümünden NVIDIA API anahtarını eklemelisin.",
                        Modifier.padding(12.dp),
                        color = c.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        itemsIndexed(
            items = lines,
            key = { index -> index.toString() }
        ) { index, line ->
            MessageBubble(
                role = line.role,
                text = line.text,
                isUser = line.role == "user"
            )
        }
    }

    // Input area
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.TextField(
                value = draft,
                onValueChange = { onDraftChange(it) },
                label = { Text("Mesajınızı yazın") },
                placeholder = { Text("Sorunuzu buraya yazın...") },
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.weight(1f)
                    .heightIn(48.dp, 120.dp)
                    .clip(RoundedCornerShape(20.dp)),
                enabled = hasApiKey && !sending
            )
            androidx.compose.material3.IconButton(
                onClick = { onSend() },
                enabled = hasApiKey && !sending && draft.isNotBlank()
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material3.Icons.Default.Send,
                    contentDescription = "Gönder"
                )
            }
        }
        if (sending) {
            androidx.compose.material3.CircularProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MessageBubble(
    role: String,
    text: String,
    isUser: Boolean
) {
    val c = MaterialTheme.colorScheme
    val bg = if (isUser) c.primaryContainer else c.surfaceContainer
    val fg = if (isUser) c.onPrimaryContainer else c.onSurface

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Text(
                text = "🤖",
                style = MaterialTheme.typography.titleMedium,
                color = c.primary
            )
            Spacer(Modifier.width(8.dp))
        }
        Column(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .contentWidth(if (isUser) 0.85f else 1f)
                .align(if (isUser) Alignment.End else Alignment.Start)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = fg,
                lineHeight = 22.sp
            )
        }
        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = "👤",
                style = MaterialTheme.typography.titleMedium,
                color = c.primary
            )
        }
    }
}
