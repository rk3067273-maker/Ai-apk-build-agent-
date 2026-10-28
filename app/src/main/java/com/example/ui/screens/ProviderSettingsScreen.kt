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
                            AiProviderType.AGENT_ROUTER -> "https://agentrouter.org"
                        }
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("API Key কীভাবে পাবেন? ↗", fontSize = 11.sp, color = NeonCyan)
                }
            }
        }

        // Custom Base URL (especially for Agent Router or proxy)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "API Base URL (কাস্টম বা প্রক্সি এন্ডপয়েন্ট)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentSetting.customBaseUrl.ifBlank { currentSetting.providerType.defaultBaseUrl },
                    onValueChange = { viewModel.updateProviderBaseUrl(selectedProviderType, it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
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

        // Test Connection Ping
        Button(
            onClick = { viewModel.testActiveProviderConnection() },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.connectionTestResult.startsWith("✅")) EmeraldGreen.copy(alpha = 0.15f) else CrimsonError.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = uiState.connectionTestResult,
                    fontSize = 13.sp,
                    color = if (uiState.connectionTestResult.startsWith("✅")) EmeraldGreen else CrimsonError,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
