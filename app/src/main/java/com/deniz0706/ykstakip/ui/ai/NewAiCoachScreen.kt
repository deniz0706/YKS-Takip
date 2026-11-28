package com.deniz0706.ykstakip.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.ui.components.YksSection
import com.deniz0706.ykstakip.ui.theme.YksSpacing
import com.deniz0706.ykstakip.viewmodel.AiCoachViewModel

/** Screen-level data class replaced by ViewModel-backed CoachMessage. */
data class CoachLine(val role: String, val text: String)

@Composable
fun NewAiCoachScreen(
    viewModel: AiCoachViewModel,
    onApiKeyMissing: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val c = MaterialTheme.colorScheme

    var draft by remember { mutableStateOf(uiState.draft) }

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

        if (!uiState.hasApiKey) {
            item {
                Surface(
                    color = c.errorContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Sohbet için Ayarlar bölümünden NVIDIA API anahtarını eklemelisiniz.",
                        Modifier.padding(12.dp),
                        color = c.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        uiState.error?.let { error ->
            item {
                Surface(
                    color = c.errorContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Uyarı $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.onErrorContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(text = "Kapat", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        itemsIndexed(items = uiState.messages) { index, message ->
            MessageBubble(
                role = message.role,
                text = message.content,
                isUser = message.role == "user",
                isError = message.isError,
                isStreaming = message.isStreaming
            )
        }

        if (uiState.isStreaming) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.titleMedium,
                        color = c.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = c.primary
                    )
                }
            }
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
                onValueChange = { viewModel.onDraftChange(it); draft = it },
                label = { Text("Mesajınızı yazın") },
                placeholder = { Text("Sorunuzu buraya yazın...") },
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.weight(1f)
                    .heightIn(48.dp, 120.dp)
                    .clip(RoundedCornerShape(20.dp)),
                enabled = uiState.hasApiKey && !uiState.isSending
            )
            androidx.compose.material3.IconButton(
                onClick = { viewModel.onSend() },
                enabled = uiState.hasApiKey && !uiState.isSending && draft.isNotBlank()
            ) {
                androidx.compose.material3.Text(
                    text = "Gönder",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        if (uiState.isSending && !uiState.isStreaming) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                strokeWidth = 2.dp,
                color = c.primary
            )
        }
    }
}

@Composable
private fun MessageBubble(
    role: String,
    text: String,
    isUser: Boolean,
    isError: Boolean = false,
    isStreaming: Boolean = false
) {
    val c = MaterialTheme.colorScheme
    val bg = if (isUser) c.primaryContainer else if (isError) c.errorContainer else c.surfaceContainer
    val fg = if (isUser) c.onPrimaryContainer else c.onSurface

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Text(
                text = "AI",
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
                .widthIn(max = if (isUser) 260.dp else Dp.Unspecified)
        ) {
            Text(
                text = text + (if (isStreaming) "…" else ""),
                style = MaterialTheme.typography.bodyMedium,
                color = fg
            )
        }
        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Sen",
                style = MaterialTheme.typography.titleMedium,
                color = c.primary
            )
        }
    }
}
