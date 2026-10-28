package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TokenUsageBar
import com.example.ui.components.VirtualDeviceSimulator
import com.example.ui.components.VoiceInputDialog
import com.example.ui.theme.*
import com.example.viewmodel.StudioUiState
import com.example.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    uiState: StudioUiState,
    modifier: Modifier = Modifier
) {
    var showVoiceDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(uiState.chatMessages.size, uiState.isAgentThinking) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    if (showVoiceDialog) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialog = false },
            onSpeechRecognized = { text ->
                viewModel.appendVoiceText(text)
                showVoiceDialog = false
            }
        )
    }

    // Virtual Device Simulator Modal Dialog
    if (uiState.showVirtualDeviceModal && uiState.currentApp != null) {
        VirtualDeviceSimulator(
            app = uiState.currentApp,
            onDismiss = { viewModel.closeVirtualDevice() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Compiler Progress Bar (Visible while building the app)
        if (uiState.isGenerating) {
            Surface(
                color = CardBackground,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = uiState.generationProgress,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                }
            }
        }

        // Virtual Device Quick Launcher Bar (If an app is ready)
        if (uiState.currentApp != null && !uiState.isGenerating) {
            Surface(
                color = SurfaceDark,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectTab(1) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(15.dp))
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${uiState.currentApp.name} লাইভ প্রস্তুত",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "ট্যাপ করে প্রিভিউ ও কোড দেখুন",
                                fontSize = 10.sp,
                                color = NeonCyan
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.selectTab(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("প্রিভিউ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                        }

                        OutlinedButton(
                            onClick = { viewModel.openVirtualDevice() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("ফুলস্ক্রিন", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Clean Chat Thread (Discussion with Agent before APK build)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(uiState.chatMessages, key = { it.id }) { message ->
                if (message.isUser) {
                    // User Message (Right Aligned)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(CardHighlight)
                                .border(1.dp, BorderDark, RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    // Agent Message (Left Aligned with AI Studio Avatar)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NeonCyan.copy(alpha = 0.15f),
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.Top)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✦", fontSize = 14.sp, color = NeonCyan)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.widthIn(max = 320.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, BorderDark, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = message.text,
                                        fontSize = 13.5.sp,
                                        color = TextPrimary,
                                        lineHeight = 20.sp
                                    )

                                    // Action buttons inside chat
                                    if (message.canTriggerBuild && !uiState.isGenerating) {
                                        Spacer(Modifier.height(12.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    viewModel.generateApp(message.suggestedAppPrompt)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.RocketLaunch,
                                                    contentDescription = null,
                                                    tint = BackgroundDark,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = "🚀 অ্যাপ বিল্ড করুন",
                                                    color = BackgroundDark,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }

                                            if (uiState.currentApp != null) {
                                                OutlinedButton(
                                                    onClick = { viewModel.selectTab(1) },
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(36.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                                ) {
                                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("📱 প্রিভিউ", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }

                                    // Quick shortcut on welcome message
                                    if (message == uiState.chatMessages.firstOrNull() && uiState.currentApp != null) {
                                        Spacer(Modifier.height(10.dp))
                                        FilledTonalButton(
                                            onClick = { viewModel.selectTab(1) },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = NeonCyan.copy(alpha = 0.2f),
                                                contentColor = NeonCyan
                                            )
                                        ) {
                                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("📱 লাইভ অ্যাপ প্রিভিউ দেখুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Thinking Indicator
            if (uiState.isAgentThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NeonCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✦", fontSize = 12.sp, color = NeonCyan)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "AI চিন্তা করছে...",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        // Real-time Token & Quota Usage Telemetry Bar
        TokenUsageBar(
            providerType = uiState.activeProvider,
            modelName = uiState.providerSettings[uiState.activeProvider]?.activeModel.orEmpty().ifBlank { "Gemini 2.0" },
            lastTaskTokens = uiState.lastTaskTokens,
            promptTokens = uiState.promptTokens,
            completionTokens = uiState.completionTokens,
            sessionTotalTokens = uiState.sessionTotalTokens,
            contextLimit = uiState.contextLimit,
            latencyMs = uiState.lastLatencyMs
        )

        // Clean Bottom Chat Input Bar (Supporting Bangla, Banglish, English)
        Surface(
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Mic Trigger
                    Surface(
                        shape = CircleShape,
                        color = CardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.promptText.isNotBlank()) BorderDark else NeonCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { showVoiceDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Text Input
                    TextField(
                        value = uiState.promptText,
                        onValueChange = { viewModel.onPromptChange(it) },
                        placeholder = {
                            Text(
                                text = "অ্যাপ আলোচনা করুন বা প্রম্পট দিন... (Bangla/Banglish)",
                                fontSize = 12.sp,
                                color = TextTertiary
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonCyan
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (uiState.promptText.isNotBlank()) {
                                    viewModel.sendChatMessage()
                                }
                            }
                        ),
                        maxLines = 3
                    )

                    Spacer(Modifier.width(8.dp))

                    // Send Button
                    Surface(
                        shape = CircleShape,
                        color = if (uiState.promptText.isNotBlank()) NeonCyan else CardBackground,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable(enabled = uiState.promptText.isNotBlank() && !uiState.isAgentThinking) {
                                viewModel.sendChatMessage()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (uiState.promptText.isNotBlank()) BackgroundDark else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
