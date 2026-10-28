package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiProviderType
import com.example.data.model.ProviderSetting
import com.example.ui.theme.*
import com.example.viewmodel.StudioUiState
import com.example.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsScreen(
    viewModel: StudioViewModel,
    uiState: StudioUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedProviderType by remember { mutableStateOf(uiState.activeProvider) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var newCustomModelName by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val currentSetting = uiState.providerSettings[selectedProviderType] ?: ProviderSetting(selectedProviderType)
    val isActiveProvider = uiState.activeProvider == selectedProviderType

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = NeonCyan.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "এআই প্রোভাইডার ও মডেল কনফিগ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "OpenAI • Gemini • OpenRouter • Agent Router",
                            fontSize = 11.sp,
                            color = NeonCyan
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "যেকোনো এআই প্রোভাইডারের API Key এবং মডেল কনফিগার করে অ্যাপ বিল্ড করতে পারেন।",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // Provider Selector Horizontal Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiProviderType.values().forEach { type ->
                val isSelected = selectedProviderType == type
                val providerColor = when (type) {
                    AiProviderType.OPEN_AI -> OpenAiGreen
                    AiProviderType.GEMINI -> GeminiBlue
                    AiProviderType.OPEN_ROUTER -> OpenRouterOrange
                    AiProviderType.AGENT_ROUTER -> AgentRouterPurple
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) SurfaceDark else CardBackground,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, providerColor) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedProviderType = type }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(providerColor, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = type.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        // Active Provider Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = selectedProviderType.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isActiveProvider) "✅ বর্তমান সক্রিয় প্রোভাইডার" else "ট্যাপ করে সক্রিয় করুন",
                        fontSize = 12.sp,
                        color = if (isActiveProvider) EmeraldGreen else TextSecondary
                    )
                }

                Button(
                    onClick = { viewModel.selectProvider(selectedProviderType) },
                    enabled = !isActiveProvider,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        disabledContainerColor = EmeraldGreen.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isActiveProvider) "সক্রিয় আছে" else "সক্রিয় করুন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActiveProvider) EmeraldGreen else Color(0xFF0B0F19)
                    )
                }
            }
        }

        // API Key Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${selectedProviderType.displayName} API Key",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentSetting.apiKey,
                    onValueChange = { viewModel.updateProviderApiKey(selectedProviderType, it) },
                    placeholder = { Text("sk-xxxxxxxxxxxxxxxx", fontSize = 12.sp, color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                            Icon(
                                if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                TextButton(
                    onClick = {
                        val url = when (selectedProviderType) {
                            AiProviderType.OPEN_AI -> "https://platform.openai.com/api-keys"
                            AiProviderType.GEMINI -> "https://aistudio.google.com/app/apikey"
                            AiProviderType.OPEN_ROUTER -> "https://openrouter.ai/keys"
                            AiProviderType.AGENT_ROUTER -> "https://openrouter.ai/keys"
                        }
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("API Key কীভাবে পাবেন? ↗", fontSize = 11.sp, color = NeonCyan)
                }
            }
        }

        // Custom Base URL with 1-Click Presets & Auto-detection
        val isInvalidAgentRouterDomain = currentSetting.customBaseUrl.contains("agentrouter.org")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "API Base URL (কাস্টম বা প্রক্সি এন্ডপয়েন্ট)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (isInvalidAgentRouterDomain) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CrimsonError.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ভুল হোস্ট!",
                                fontSize = 10.sp,
                                color = CrimsonError,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentSetting.customBaseUrl.ifBlank { currentSetting.providerType.defaultBaseUrl },
                    onValueChange = { viewModel.updateProviderBaseUrl(selectedProviderType, it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isInvalidAgentRouterDomain) CrimsonError else NeonCyan,
                        unfocusedBorderColor = if (isInvalidAgentRouterDomain) CrimsonError.copy(alpha = 0.7f) else BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                // Warning & 1-Click Fix Banner if domain contains agentrouter.org
                if (isInvalidAgentRouterDomain) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CrimsonError.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "সতর্কবার্তা: 'api.agentrouter.org' ইন্টারনেটে সক্রিয় নয়!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonError
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Claude, DeepSeek বা GPT ব্যবহার করতে OpenRouter এন্ডপয়েন্ট সিলেক্ট করুন:",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.fixAgentRouterUrl(selectedProviderType) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF0B0F19), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("🛠️ ঠিক করুন: OpenRouter URL বসান", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B0F19))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Quick Preset Chips
                Text(
                    text = "জনপ্রিয় প্রোভাইডার প্রিসেট (১-ট্যাপে সেট করুন):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // OpenRouter Preset
                    FilterChip(
                        selected = currentSetting.activeBaseUrl.contains("openrouter.ai"),
                        onClick = {
                            viewModel.applyUrlPreset(
                                providerType = selectedProviderType,
                                baseUrl = "https://openrouter.ai/api/v1",
                                models = listOf(
                                    "anthropic/claude-3.5-sonnet",
                                    "deepseek/deepseek-r1",
                                    "openai/gpt-4o",
                                    "google/gemini-2.0-flash-001"
                                ),
                                defaultModel = "anthropic/claude-3.5-sonnet"
                            )
                        },
                        label = { Text("🌐 OpenRouter (Claude/R1)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OpenRouterOrange.copy(alpha = 0.25f),
                            selectedLabelColor = TextPrimary,
                            containerColor = CardBackground,
                            labelColor = TextSecondary
                        )
                    )

                    // DeepSeek Preset
                    FilterChip(
                        selected = currentSetting.activeBaseUrl.contains("deepseek.com"),
                        onClick = {
                            viewModel.applyUrlPreset(
                                providerType = selectedProviderType,
                                baseUrl = "https://api.deepseek.com/v1",
                                models = listOf("deepseek-chat", "deepseek-coder", "deepseek-reasoner"),
                                defaultModel = "deepseek-chat"
                            )
                        },
                        label = { Text("⚡ DeepSeek Direct", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                            selectedLabelColor = TextPrimary,
                            containerColor = CardBackground,
                            labelColor = TextSecondary
                        )
                    )

                    // Groq Preset
                    FilterChip(
                        selected = currentSetting.activeBaseUrl.contains("groq.com"),
                        onClick = {
                            viewModel.applyUrlPreset(
                                providerType = selectedProviderType,
                                baseUrl = "https://api.groq.com/openai/v1",
                                models = listOf("llama-3.3-70b-versatile", "mixtral-8x7b-32768", "gemma2-9b-it"),
                                defaultModel = "llama-3.3-70b-versatile"
                            )
                        },
                        label = { Text("🚀 Groq (Ultra-Fast)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen.copy(alpha = 0.25f),
                            selectedLabelColor = TextPrimary,
                            containerColor = CardBackground,
                            labelColor = TextSecondary
                        )
                    )

                    // OpenAI Official Preset
                    FilterChip(
                        selected = currentSetting.activeBaseUrl.contains("openai.com"),
                        onClick = {
                            viewModel.applyUrlPreset(
                                providerType = selectedProviderType,
                                baseUrl = "https://api.openai.com/v1",
                                models = listOf("gpt-4o", "gpt-4o-mini", "o3-mini"),
                                defaultModel = "gpt-4o"
                            )
                        },
                        label = { Text("🤖 OpenAI Official", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OpenAiGreen.copy(alpha = 0.25f),
                            selectedLabelColor = TextPrimary,
                            containerColor = CardBackground,
                            labelColor = TextSecondary
                        )
                    )

                    // Local Emulator / Ollama Preset
                    FilterChip(
                        selected = currentSetting.activeBaseUrl.contains("10.0.2.2") || currentSetting.activeBaseUrl.contains("localhost"),
                        onClick = {
                            viewModel.applyUrlPreset(
                                providerType = selectedProviderType,
                                baseUrl = "http://10.0.2.2:11434/v1",
                                models = listOf("qwen2.5-coder", "deepseek-r1:8b", "llama3.2"),
                                defaultModel = "qwen2.5-coder"
                            )
                        },
                        label = { Text("💻 Local / Ollama", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CardBackground,
                            selectedLabelColor = TextPrimary,
                            containerColor = CardBackground,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        // Available Models Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "মডেল নির্বাচন করুন:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(10.dp))

                val models = currentSetting.availableModels.ifEmpty { selectedProviderType.defaultModels }

                models.forEach { model ->
                    val isModelSelected = currentSetting.activeModel == model
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isModelSelected) CardBackground else Color.Transparent,
                        border = if (isModelSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.updateProviderModel(selectedProviderType, model)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isModelSelected,
                                onClick = { viewModel.updateProviderModel(selectedProviderType, model) },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = model,
                                fontSize = 13.sp,
                                fontWeight = if (isModelSelected) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimary
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Spacer(Modifier.height(10.dp))

                // Add Custom Model Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCustomModelName,
                        onValueChange = { newCustomModelName = it },
                        placeholder = { Text("নতুন মডেল লিখুন...", fontSize = 12.sp, color = TextTertiary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (newCustomModelName.isNotBlank()) {
                                viewModel.addCustomModel(selectedProviderType, newCustomModelName.trim())
                                newCustomModelName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("যোগ করুন", fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }
        }

        // Test Connection Ping - Pass selectedProviderType directly
        Button(
            onClick = { viewModel.testActiveProviderConnection(selectedProviderType) },
            enabled = !uiState.isTestingConnection,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
        ) {
            if (uiState.isTestingConnection) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan)
            } else {
                Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("⚡ কানেকশন টেস্ট করুন (Test API Ping)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
        }

        if (uiState.connectionTestResult != null) {
            val isSuccess = uiState.connectionTestResult.startsWith("✅")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) EmeraldGreen.copy(alpha = 0.15f) else CrimsonError.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSuccess) EmeraldGreen.copy(alpha = 0.4f) else CrimsonError.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (isSuccess) EmeraldGreen else CrimsonError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isSuccess) "কানেকশন টেস্ট সম্পন্ন" else "কানেকশন সমস্যা দেখা দিয়েছে",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuccess) EmeraldGreen else CrimsonError
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = uiState.connectionTestResult,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )

                    // If error is related to host resolution or agentrouter.org, offer 1-click fix right in error card!
                    if (!isSuccess && (uiState.connectionTestResult.contains("হোস্ট") || uiState.connectionTestResult.contains("agentrouter") || uiState.connectionTestResult.contains("URL"))) {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.fixAgentRouterUrl(selectedProviderType)
                                viewModel.testActiveProviderConnection(selectedProviderType)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color(0xFF0B0F19), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("🛠️ OpenRouter URL দিয়ে এখনই ঠিক করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B0F19))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
