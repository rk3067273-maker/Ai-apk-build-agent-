package com.example.data.model

enum class AiProviderType(
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultModels: List<String>,
    val description: String
) {
    OPEN_AI(
        displayName = "OpenAI",
        defaultBaseUrl = "https://api.openai.com/v1",
        defaultModels = listOf("gpt-4o", "gpt-4o-mini", "o3-mini", "gpt-4-turbo"),
        description = "Industry standard OpenAI models with code generation capability"
    ),
    GEMINI(
        displayName = "Google Gemini",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta",
        defaultModels = listOf("gemini-2.0-flash", "gemini-1.5-pro", "gemini-1.5-flash"),
        description = "Google DeepMind frontier multimodal & code generation engine"
    ),
    OPEN_ROUTER(
        displayName = "OpenRouter",
        defaultBaseUrl = "https://openrouter.ai/api/v1",
        defaultModels = listOf(
            "anthropic/claude-3.5-sonnet",
            "deepseek/deepseek-r1",
            "meta-llama/llama-3.3-70b-instruct",
            "google/gemini-2.0-flash-001",
            "openai/gpt-4o"
        ),
        description = "Unified gateway to 200+ top open & proprietary LLMs"
    ),
    AGENT_ROUTER(
        displayName = "Agent Router / Custom",
        defaultBaseUrl = "https://api.agentrouter.org/v1",
        defaultModels = listOf("claude-3-5-sonnet-20241022", "gpt-4o", "deepseek-coder", "custom-model"),
        description = "Custom agent proxy, Claude, or local OpenAI-compatible endpoint"
    )
}

data class ProviderSetting(
    val providerType: AiProviderType,
    val apiKey: String = "",
    val customBaseUrl: String = "",
    val selectedModel: String = "",
    val availableModels: List<String> = emptyList()
) {
    val activeBaseUrl: String
        get() = if (customBaseUrl.isNotBlank()) customBaseUrl.trimEnd('/') else providerType.defaultBaseUrl

    val activeModel: String
        get() = if (selectedModel.isNotBlank()) selectedModel else (availableModels.firstOrNull() ?: providerType.defaultModels.first())

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()
}
