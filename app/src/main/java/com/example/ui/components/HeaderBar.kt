package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiProviderType
import com.example.ui.theme.*

@Composable
fun HeaderBar(
    activeProvider: AiProviderType,
    activeModel: String,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDark,
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Google AI Studio Style Brand
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.15f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✦",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Google AI Studio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Android App Builder",
                        fontSize = 10.sp,
                        color = NeonCyan
                    )
                }
            }

            // Model Indicator & Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Model pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenSettings() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (activeProvider) {
                                        AiProviderType.OPEN_AI -> OpenAiGreen
                                        AiProviderType.GEMINI -> GeminiBlue
                                        AiProviderType.OPEN_ROUTER -> OpenRouterOrange
                                        AiProviderType.AGENT_ROUTER -> AgentRouterPurple
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = activeModel.substringAfterLast('/'),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                // New Chat action
                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // History action
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "Saved Apps",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
