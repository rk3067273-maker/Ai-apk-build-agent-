package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiProviderType
import com.example.ui.theme.*

@Composable
fun TokenUsageBar(
    providerType: AiProviderType,
    modelName: String,
    lastTaskTokens: Int,
    promptTokens: Int,
    completionTokens: Int,
    sessionTotalTokens: Int,
    contextLimit: Int,
    latencyMs: Long,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Calculate percent of context window used
    val usedPercent = if (contextLimit > 0) {
        ((lastTaskTokens.toFloat() / contextLimit.toFloat()) * 100f).coerceIn(0f, 100f)
    } else 0f

    Surface(
        color = Color(0xFF090E1A),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Main Telemetry Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Provider & Model tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                when (providerType) {
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
                        text = modelName.substringAfterLast('/'),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (lastTaskTokens > 0) "টাস্ক টোকেন: $lastTaskTokens" else "টোকেন ট্র্যাকার সক্রিয়",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                }

                // Mini Percentage & expand icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (lastTaskTokens > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${String.format("%.2f", usedPercent)}% উইন্ডো",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                    }

                    Icon(
                        if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Context Window Progress Bar
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (usedPercent / 100f).coerceIn(0.01f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = if (usedPercent > 80f) CrimsonError else if (usedPercent > 40f) AmberWarning else NeonCyan,
                trackColor = BorderDark
            )

            // Expanded Detailed Analytics Panel
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Divider(color = BorderDark, thickness = 0.5.dp)
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("প্রম্পট ইনপুট টোকেন:", fontSize = 10.sp, color = TextSecondary)
                            Text("$promptTokens tokens", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("কোড জেনারেশন টোকেন:", fontSize = 10.sp, color = TextSecondary)
                            Text("$completionTokens tokens", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("রেসপন্স লেটেন্সি:", fontSize = 10.sp, color = TextSecondary)
                            Text("${latencyMs}ms", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সেশনের মোট টোকেন ব্যবহার: $sessionTotalTokens / $contextLimit max",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = VioletGlow
                        )

                        Text(
                            text = "অনুকূলিত মেমরি",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
