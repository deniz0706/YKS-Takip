package com.deniz0706.ykstakip.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.ui.theme.YksTokens
import com.deniz0706.ykstakip.viewmodel.AiCoachViewModel

/** Premium AI Coach screen for YKS Takip 4.0. */
@Composable
fun PremiumAiCoachScreen(
    viewModel: AiCoachViewModel,
    onApiKeyMissing: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val c = MaterialTheme.colorScheme
    var draft by remember { mutableStateOf(uiState.draft) }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.draft) {
        draft = uiState.draft
    }

    LaunchedEffect(
        uiState.messages.size,
        uiState.isStreaming,
        uiState.hasApiKey,
        uiState.error
    ) {
        if (uiState.messages.isNotEmpty()) {
            val fixedItems = 1 +
                (if (!uiState.hasApiKey) 1 else 0) +
                (if (uiState.error != null) 1 else 0)
            val targetIndex = if (uiState.isStreaming) {
                fixedItems + uiState.messages.size
            } else {
                fixedItems + uiState.messages.lastIndex
            }
            listState.animateScrollToItem(targetIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = YksTokens.Space.pageHorizontal,
                vertical = YksTokens.Space.pageVertical
            ),
            verticalArrangement = Arrangement.spacedBy(YksTokens.Space.sm)
        ) {
            item { PremiumAiHeader(colorScheme = c) }

            if (!uiState.hasApiKey) {
                item {
                    PremiumApiKeyMissingCard(
                        colorScheme = c,
                        onApiKeyMissing = onApiKeyMissing
                    )
                }
            }

            uiState.error?.let { error ->
                item {
                    PremiumErrorCard(
                        message = error,
                        onDismiss = { viewModel.clearError() },
                        colorScheme = c
                    )
                }
            }

            itemsIndexed(items = uiState.messages) { _, message ->
                PremiumMessageBubble(
                    role = message.role,
                    text = message.content,
                    isUser = message.role == "user",
                    isError = message.isError,
                    isStreaming = message.isStreaming,
                    colorScheme = c
                )
            }

            if (uiState.isStreaming) {
                item { PremiumStreamingIndicator(colorScheme = c) }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = c.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = YksTokens.Space.pageHorizontal,
                        vertical = 8.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.sm)
                ) {
                    androidx.compose.material3.TextField(
                        value = draft,
                        onValueChange = { newDraft ->
                            viewModel.onDraftChange(newDraft)
                            draft = newDraft
                        },
                        label = { Text("Mesajınızı yazın") },
                        placeholder = { Text("Sorunuzu buraya yazın...") },
                        singleLine = false,
                        maxLines = 4,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(48.dp, 120.dp)
                            .clip(RoundedCornerShape(YksTokens.Shape.input)),
                        enabled = uiState.hasApiKey && !uiState.isSending
                    )
                    IconButton(
                        onClick = { viewModel.onSend() },
                        enabled = uiState.hasApiKey && !uiState.isSending && draft.isNotBlank()
                    ) {
                        Text(
                            text = "➤",
                            color = if (uiState.hasApiKey && !uiState.isSending && draft.isNotBlank()) {
                                c.primary
                            } else {
                                c.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                if (uiState.isSending && !uiState.isStreaming) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = c.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumAiHeader(colorScheme: ColorScheme) {
    val c = colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AI Koçun",
                style = MaterialTheme.typography.labelMedium,
                color = c.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "YKS sürecini birlikte yönetelim",
                style = MaterialTheme.typography.headlineSmall,
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
            shape = RoundedCornerShape(YksTokens.Shape.pill),
            color = c.secondaryContainer
        ) {
            Text(
                text = "NVIDIA AI",
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = c.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PremiumApiKeyMissingCard(colorScheme: ColorScheme, onApiKeyMissing: () -> Unit) {
    val c = colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(YksTokens.Shape.input),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = c.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(YksTokens.Space.cardContent),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "API Anahtarı Gerekli",
                style = MaterialTheme.typography.titleMedium,
                color = c.onErrorContainer,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Sohbet için Ayarlar bölümünden NVIDIA API anahtarını eklemelisiniz.",
                style = MaterialTheme.typography.bodySmall,
                color = c.onErrorContainer
            )
            androidx.compose.material3.TextButton(
                onClick = onApiKeyMissing
            ) {
                Text("Ayarları aç")
            }
        }
    }
}

@Composable
private fun PremiumErrorCard(
    message: String,
    onDismiss: () -> Unit,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(YksTokens.Shape.input),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = c.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️ $message",
                style = MaterialTheme.typography.bodySmall,
                color = c.onErrorContainer
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text(text = "Kapat", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PremiumMessageBubble(
    role: String,
    text: String,
    isUser: Boolean,
    isError: Boolean = false,
    isStreaming: Boolean = false,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    val bg = if (isUser) c.primaryContainer else if (isError) c.errorContainer else c.surfaceContainer
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
                .clip(RoundedCornerShape(YksTokens.Shape.control))
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
                text = "👤",
                style = MaterialTheme.typography.titleMedium,
                color = c.primary
            )
        }
    }
}

@Composable
private fun PremiumStreamingIndicator(colorScheme: ColorScheme) {
    val c = colorScheme
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "🤖",
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