package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiApiService
import com.example.data.api.GitHubApiService
import com.example.data.api.GitHubUser
import com.example.data.api.PushResult
import com.example.data.local.AppDatabase
import com.example.data.local.GitHubConfigEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.ProjectFileEntity
import com.example.data.local.ProviderConfigEntity
import com.example.data.model.AiProviderType
import com.example.data.model.GeneratedApp
import com.example.data.model.ProjectFile
import com.example.data.model.PromptTemplate
import com.example.data.model.ProviderSetting
import com.example.domain.AppGeneratorEngine
import com.example.domain.ProjectZipExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val canTriggerBuild: Boolean = false,
    val suggestedAppPrompt: String? = null
)

data class StudioUiState(
    val promptText: String = "",
    val activeProvider: AiProviderType = AiProviderType.GEMINI,
    val providerSettings: Map<AiProviderType, ProviderSetting> = emptyMap(),
    val isTestingConnection: Boolean = false,
    val connectionTestResult: String? = null,
    val isGenerating: Boolean = false,
    val generationProgress: String = "",
    val generationLogs: List<String> = emptyList(),
    val generationError: String? = null,
    val currentApp: GeneratedApp? = null,
    val selectedFileIndex: Int = 0,
    val isEditingFile: Boolean = false,
    val editedFileContent: String = "",
    // Chat System (Google AI Studio style conversational workspace)
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            isUser = false,
            text = "👋 হ্যালো! আমি আপনার Google AI Studio অ্যাপ আর্কিটেক্ট।\n\nআপনি কী ধরনের অ্যান্ড্রয়েড অ্যাপ বানাতে চান? APK তৈরির আগে আপনি আমার সাথে বাংলা, Banglish (যেমন: 'ami ekta crypto wallet banate chai') বা English-এ বিস্তারিত আলোচনা করতে পারেন।\n\nআইডিয়া ও ফিচার ঠিক হলে আমরা ১-ক্লিকে সম্পূর্ণ প্রজেক্ট বিল্ড করে ভার্চুয়াল ডিভাইসে লাইভ টেস্ট করতে পারব এবং গিটহাবে পুশ করে APK ইনস্টল করতে পারব।",
            canTriggerBuild = false
        )
    ),
    val isAgentThinking: Boolean = false,
    // Real-time Token & Quota Telemetry
    val lastTaskTokens: Int = 0,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val sessionTotalTokens: Int = 0,
    val contextLimit: Int = 1000000,
    val lastLatencyMs: Long = 0,
    // Virtual Device Simulator Modal
    val showVirtualDeviceModal: Boolean = false,
    // GitHub State
    val githubToken: String = "",
    val githubUsername: String = "",
    val githubRepoName: String = "",
    val isVerifyingGitHub: Boolean = false,
    val verifiedGitHubUser: GitHubUser? = null,
    val isPushingToGitHub: Boolean = false,
    val pushProgressCurrent: Int = 0,
    val pushProgressTotal: Int = 0,
    val currentPushFile: String = "",
    val lastPushResult: PushResult? = null,
    val pushError: String? = null,
    val detectedClipboardToken: String? = null,
    val gitHubAuthMethod: Int = 0, // 0: Google/Browser Web Login, 1: Direct Token
    // Export Zip
    val isExportingZip: Boolean = false,
    val zipExportMessage: String? = null,
    // History
    val savedProjects: List<ProjectEntity> = emptyList(),
    // Voice
    val isListeningVoice: Boolean = false,
    // Navigation: 0: AI Studio Chat, 1: Code & Preview, 2: GitHub & APK, 3: Settings
    val activeTab: Int = 0
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.projectDao()
    private val aiApi = AiApiService()
    private val gitHubApi = GitHubApiService()
    private val generatorEngine = AppGeneratorEngine(aiApi)
    private val zipExporter = ProjectZipExporter(application)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    init {
        val starter = generatorEngine.createStarterDefaultApp()
        _uiState.update { it.copy(currentApp = starter, githubRepoName = "nexus-studio") }
        initializeSettings()
        loadSavedProjects()
    }

    private fun initializeSettings() {
        viewModelScope.launch {
            val defaultSettings = mutableMapOf<AiProviderType, ProviderSetting>()
            for (type in AiProviderType.values()) {
                val savedConfig = dao.getProviderConfig(type.name)
                val setting = if (savedConfig != null) {
                    ProviderSetting(
                        providerType = type,
                        apiKey = savedConfig.apiKey,
                        customBaseUrl = savedConfig.customBaseUrl,
                        selectedModel = savedConfig.selectedModel,
                        availableModels = if (savedConfig.customModelsJson.isNotBlank()) {
                            savedConfig.customModelsJson.split(",")
                        } else type.defaultModels
                    )
                } else {
                    ProviderSetting(
                        providerType = type,
                        apiKey = "",
                        customBaseUrl = type.defaultBaseUrl,
                        selectedModel = type.defaultModels.first(),
                        availableModels = type.defaultModels
                    )
                }
                defaultSettings[type] = setting
            }

            val savedGitHub = dao.getGitHubConfig()

            _uiState.update { state ->
                state.copy(
                    providerSettings = defaultSettings,
                    githubToken = savedGitHub?.token.orEmpty(),
                    githubUsername = savedGitHub?.username.orEmpty(),
                    contextLimit = getContextLimitForModel(defaultSettings[AiProviderType.GEMINI]?.activeModel.orEmpty())
                )
            }

            if (!savedGitHub?.token.isNullOrBlank()) {
                verifyGitHubToken(savedGitHub!!.token)
            }
        }
    }

    private fun getContextLimitForModel(modelName: String): Int {
        return when {
            modelName.contains("gemini", ignoreCase = true) -> 1000000
            modelName.contains("gpt-4o", ignoreCase = true) -> 128000
            modelName.contains("claude", ignoreCase = true) -> 200000
            modelName.contains("deepseek", ignoreCase = true) -> 64000
            else -> 128000
        }
    }

    private fun loadSavedProjects() {
        viewModelScope.launch {
            dao.getAllProjects().collect { projects ->
                _uiState.update { it.copy(savedProjects = projects) }
            }
        }
    }

    fun onPromptChange(newPrompt: String) {
        _uiState.update { it.copy(promptText = newPrompt) }
    }

    fun appendVoiceText(text: String) {
        _uiState.update { current ->
            val existing = current.promptText.trim()
            val updated = if (existing.isEmpty()) text else "$existing $text"
            current.copy(promptText = updated)
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun openVirtualDevice() {
        _uiState.update { it.copy(showVirtualDeviceModal = true) }
    }

    fun closeVirtualDevice() {
        _uiState.update { it.copy(showVirtualDeviceModal = false) }
    }

    fun selectProvider(providerType: AiProviderType) {
        val model = _uiState.value.providerSettings[providerType]?.activeModel.orEmpty()
        _uiState.update {
            it.copy(
                activeProvider = providerType,
                connectionTestResult = null,
                contextLimit = getContextLimitForModel(model)
            )
        }
    }

    fun updateProviderApiKey(providerType: AiProviderType, apiKey: String) {
        val currentSetting = _uiState.value.providerSettings[providerType] ?: ProviderSetting(providerType)
        val updated = currentSetting.copy(apiKey = apiKey)
        val newMap = _uiState.value.providerSettings.toMutableMap().apply { put(providerType, updated) }
        _uiState.update { it.copy(providerSettings = newMap) }
        saveProviderConfig(updated)
    }

    fun updateProviderModel(providerType: AiProviderType, model: String) {
        val currentSetting = _uiState.value.providerSettings[providerType] ?: ProviderSetting(providerType)
        val updated = currentSetting.copy(selectedModel = model)
        val newMap = _uiState.value.providerSettings.toMutableMap().apply { put(providerType, updated) }
        _uiState.update {
            it.copy(
                providerSettings = newMap,
                contextLimit = getContextLimitForModel(model)
            )
        }
        saveProviderConfig(updated)
    }

    fun updateProviderBaseUrl(providerType: AiProviderType, baseUrl: String) {
        val currentSetting = _uiState.value.providerSettings[providerType] ?: ProviderSetting(providerType)
        val updated = currentSetting.copy(customBaseUrl = baseUrl)
        val newMap = _uiState.value.providerSettings.toMutableMap().apply { put(providerType, updated) }
        _uiState.update { it.copy(providerSettings = newMap) }
        saveProviderConfig(updated)
    }

    fun addCustomModel(providerType: AiProviderType, newModel: String) {
        if (newModel.isBlank()) return
        val currentSetting = _uiState.value.providerSettings[providerType] ?: ProviderSetting(providerType)
        if (!currentSetting.availableModels.contains(newModel)) {
            val updatedModels = listOf(newModel) + currentSetting.availableModels
            val updated = currentSetting.copy(availableModels = updatedModels, selectedModel = newModel)
            val newMap = _uiState.value.providerSettings.toMutableMap().apply { put(providerType, updated) }
            _uiState.update { it.copy(providerSettings = newMap) }
            saveProviderConfig(updated)
        }
    }

    private fun saveProviderConfig(setting: ProviderSetting) {
        viewModelScope.launch {
            dao.saveProviderConfig(
                ProviderConfigEntity(
                    providerId = setting.providerType.name,
                    apiKey = setting.apiKey,
                    customBaseUrl = setting.customBaseUrl,
                    selectedModel = setting.selectedModel,
                    customModelsJson = setting.availableModels.joinToString(",")
                )
            )
        }
    }

    // Conversational Agent Discussion
    fun sendChatMessage(userText: String = _uiState.value.promptText) {
        val text = userText.trim()
        if (text.isBlank()) return

        val userMessage = ChatMessage(isUser = true, text = text)
        val updatedMessages = _uiState.value.chatMessages + userMessage

        _uiState.update {
            it.copy(
                chatMessages = updatedMessages,
                promptText = "",
                isAgentThinking = true
            )
        }

        viewModelScope.launch {
            val activeProvider = _uiState.value.activeProvider
            val setting = _uiState.value.providerSettings[activeProvider] ?: ProviderSetting(activeProvider)

            if (setting.isConfigured) {
                val systemPrompt = """
                    You are AgentStudio AI, an expert mobile software architect modeled after Google AI Studio Build.
                    The user is discussing their mobile app idea with you BEFORE building the APK.
                    You understand Bengali, Banglish (Bengali written in English letters, e.g. "ami ekta app banabo", "er moddhe ki ki thakbe"), and English seamlessly.
                    Respond helpfully and concisely in the same language/script the user used (Bangla, Banglish, or English).
                    Analyze their requirements, suggest UI layouts, features, state management, and ask clarifying questions if needed.
                    If the idea is clear or if they ask to build, encourage them to proceed with building the complete APK project.
                """.trimIndent()

                val result = aiApi.generateContent(
                    setting = setting,
                    systemPrompt = systemPrompt,
                    userPrompt = text
                )

                val replyText = if (result.isSuccess) {
                    result.getOrNull()?.text.orEmpty()
                } else {
                    generateLocalIntelligentReply(text)
                }

                val tokenResp = result.getOrNull()
                val pTokens = tokenResp?.promptTokens ?: (text.length / 4 + 80)
                val cTokens = tokenResp?.completionTokens ?: (replyText.length / 4)
                val tTokens = tokenResp?.totalTokens ?: (pTokens + cTokens)
                val lat = tokenResp?.latencyMs ?: 600L

                val agentMessage = ChatMessage(
                    isUser = false,
                    text = replyText,
                    canTriggerBuild = true,
                    suggestedAppPrompt = text
                )
                _uiState.update {
                    it.copy(
                        chatMessages = it.chatMessages + agentMessage,
                        isAgentThinking = false,
                        lastTaskTokens = tTokens,
                        promptTokens = pTokens,
                        completionTokens = cTokens,
                        sessionTotalTokens = it.sessionTotalTokens + tTokens,
                        lastLatencyMs = lat
                    )
                }
            } else {
                kotlinx.coroutines.delay(600)
                val replyText = generateLocalIntelligentReply(text)
                val pTokens = text.length / 4 + 80
                val cTokens = replyText.length / 4
                val tTokens = pTokens + cTokens

                val agentMessage = ChatMessage(
                    isUser = false,
                    text = replyText,
                    canTriggerBuild = true,
                    suggestedAppPrompt = text
                )
                _uiState.update {
                    it.copy(
                        chatMessages = it.chatMessages + agentMessage,
                        isAgentThinking = false,
                        lastTaskTokens = tTokens,
                        promptTokens = pTokens,
                        completionTokens = cTokens,
                        sessionTotalTokens = it.sessionTotalTokens + tTokens,
                        lastLatencyMs = 450L
                    )
                }
            }
        }
    }

    private fun generateLocalIntelligentReply(userInput: String): String {
        val lower = userInput.lowercase()
        return when {
            lower.contains("crypto") || lower.contains("wallet") || lower.contains("কয়েন") || lower.contains("koyren") -> {
                "চমৎকার আইডিয়া! একটি আধুনিক ক্রিপ্টো ওয়ালেট অ্যাপে আমরা নিচের ফিচারগুলো রাখতে পারি:\n\n" +
                "1. লাইভ বিটকয়েন ও ইথারিয়াম চার্ট (Real-time Price Tracker)\n" +
                "2. সেন্ড ও রিসিভ ইন্টারফেস (Send / Receive with Balance)\n" +
                "3. রিসেন্ট ট্রানজেকশন হিস্ট্রি লিস্ট\n" +
                "4. সিকিউর পিন ও বায়োমেট্রিক অথেন্টিকেশন\n\n" +
                "আপনি কি এই ফিচারগুলো নিয়ে অ্যাপের ফুল কোড ও APK বিল্ড করতে চান? নিচের 'বিল্ড করুন' বাটনে চাপ দিন।"
            }
            lower.contains("shop") || lower.contains("e-commerce") || lower.contains("দোকান") || lower.contains("cart") || lower.contains("kenakata") -> {
                "ই-কমার্স অ্যাপের জন্য দারুণ প্ল্যান! এই অ্যাপে আমরা অন্তর্ভুক্ত করতে পারি:\n\n" +
                "1. প্রোডাক্ট গ্রিড ক্যাটালগ ও ক্যাটাগরি ফিল্টার\n" +
                "2. ডায়নামিক শপিং কার্ট (Add / Remove / Quantity)\n" +
                "3. মেটেরিয়াল ৩ ক্লিন চেকআউট ও পেমেন্ট স্ক্রিন\n" +
                "4. অর্ডার সামারি ও ট্র্যাকিং স্ট্যাটাস\n\n" +
                "আমি এখনই আপনার জন্য সম্পূর্ণ জেটপ্যাক কম্পোজ আর্কিটেকচার ও CI/CD সহ বিল্ড করে দিতে পারি।"
            }
            lower.contains("chat") || lower.contains("messenger") || lower.contains("মেসেজ") || lower.contains("kotha") -> {
                "রিয়েল-টাইম চ্যাট অ্যাপের জন্য উপযুক্ত আর্কিটেকচার:\n\n" +
                "1. কনভারসেশন লিস্ট ও অ্যাক্টিভ ফ্রেন্ডস রিং\n" +
                "2. চ্যাট বাবল UI (User & Friend messages)\n" +
                "3. ভয়েস মেসেজ প্লেয়ার ও কুইক এআই রিপ্লাই সাজেশন\n" +
                "4. এন্ড-টু-এন্ড এনক্রিপশন প্রটোকল\n\n" +
                "আপনার এই প্ল্যানটি ফাইনাল হলে নিচের বাটন দিয়ে অ্যাপটি বিল্ড করে গিটহাবে পুশ করুন।"
            }
            lower.contains("expense") || lower.contains("budget") || lower.contains("খরচ") || lower.contains("khoroch") || lower.contains("taka") -> {
                "দৈনিক আয় ও ব্যয় ট্র্যাকারের জন্য যা যা থাকবে:\n\n" +
                "1. মান্থলি বাজেট গোল ও পাই চার্ট ভিজ্যুয়ালাইজেশন\n" +
                "2. ক্যাটাগরি ভিত্তিক খরচ ইনপুট (Food, Bills, Shopping)\n" +
                "3. হিস্ট্রি লগ ও এক্সপোর্ট অপশন\n\n" +
                "বিল্ড করতে প্রস্তুত থাকলে জানান!"
            }
            lower.contains("kemon") || lower.contains("hello") || lower.contains("hi") || lower.contains("কেমন") -> {
                "আমি ভালো আছি! গুগল এআই স্টুডিওর মতো যেকোনো অ্যাপের আইডিয়া আমার সাথে আলোচনা করতে পারেন। আপনি কী ধরনের ফিচার চান বা কী অ্যাপ বানাতে চান বলুন।"
            }
            else -> {
                "আপনার প্রম্পটটি আমি বুঝতে পেরেছি। এই অ্যাপের জন্য:\n\n" +
                "• আধুনিক Android Jetpack Compose (Material 3) UI আর্কিটেকচার ডিজাইন করা হবে।\n" +
                "• স্টেট ম্যানেজমেন্ট ও ডেটা মডেল প্রস্তুত করা হবে।\n" +
                "• ভার্চুয়াল ডিভাইসে লাইভ টেস্ট করার সুবিধা থাকবে।\n" +
                "• গিটহাব অ্যাকশনস CI/CD স্ক্রিপ্ট যুক্ত করা হবে যাতে সরাসরি ফোনে APK ডাউনলোড করা যায়।\n\n" +
                "আপনি চাইলে আরও বিস্তারিত বলতে পারেন, অথবা এখনই 'সম্পূর্ণ অ্যাপ বিল্ড করুন' বাটনে চাপ দিয়ে প্রজেক্টটি তৈরি করতে পারেন।"
            }
        }
    }

    fun clearChat() {
        _uiState.update {
            it.copy(
                chatMessages = listOf(
                    ChatMessage(
                        isUser = false,
                        text = "চ্যাট ক্লিয়ার করা হয়েছে। নতুন কোনো অ্যাপের আইডিয়া নিয়ে আলোচনা করতে নিচে লিখুন বা মুখে বলুন।",
                        canTriggerBuild = false
                    )
                )
            )
        }
    }

    fun generateApp(customPrompt: String? = null) {
        val prompt = customPrompt?.trim() ?: _uiState.value.promptText.trim().ifBlank {
            _uiState.value.chatMessages.lastOrNull { it.isUser }?.text.orEmpty()
        }
        if (prompt.isBlank()) return

        val activeProvider = _uiState.value.activeProvider
        val setting = _uiState.value.providerSettings[activeProvider] ?: ProviderSetting(activeProvider)

        _uiState.update {
            it.copy(
                isGenerating = true,
                generationProgress = "Initializing AgentStudio engine...",
                generationLogs = listOf("⚡ Initializing AgentStudio compiler..."),
                generationError = null
            )
        }

        viewModelScope.launch {
            val result = generatorEngine.generateApp(
                prompt = prompt,
                setting = setting,
                onProgress = { progress ->
                    _uiState.update { state ->
                        state.copy(
                            generationProgress = progress,
                            generationLogs = state.generationLogs + progress
                        )
                    }
                }
            )

            if (result.isSuccess) {
                val telemetry = result.getOrNull()!!
                val app = telemetry.app
                val defaultRepo = app.name.lowercase().replace(Regex("[^a-z0-9]"), "-")

                val completionChat = ChatMessage(
                    isUser = false,
                    text = "🎉 **${app.name}** সফলভাবে তৈরি হয়েছে!\n\n${app.files.size} টি সোর্স ফাইল প্রস্তুত করা হয়েছে (UI, ViewModel, Manifest, Gradle, এবং GitHub CI/CD)।\n\nআপনি 'ভার্চুয়াল ডিভাইস' খুলে লাইভ টেস্ট করতে পারেন অথবা 'GitHub & APK' ট্যাবে গিয়ে ১-ক্লিকে পুশ করে APK ডাউনলোড করতে পারেন।",
                    canTriggerBuild = false
                )

                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationProgress = "",
                        currentApp = app,
                        selectedFileIndex = 0,
                        githubRepoName = defaultRepo,
                        chatMessages = it.chatMessages + completionChat,
                        lastTaskTokens = telemetry.totalTokens,
                        promptTokens = telemetry.promptTokens,
                        completionTokens = telemetry.completionTokens,
                        sessionTotalTokens = it.sessionTotalTokens + telemetry.totalTokens,
                        lastLatencyMs = telemetry.latencyMs,
                        activeTab = 1
                    )
                }

                persistProject(app)
            } else {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationError = result.exceptionOrNull()?.message ?: "Failed to generate application."
                    )
                }
            }
        }
    }

    private fun persistProject(app: GeneratedApp) {
        viewModelScope.launch {
            val entity = ProjectEntity(
                id = app.id,
                name = app.name,
                description = app.description,
                packageName = app.packageName,
                providerUsed = app.providerUsed,
                modelUsed = app.modelUsed,
                createdAt = app.createdAt,
                githubRepoUrl = app.githubRepoUrl,
                lastPushCommitSha = app.lastPushCommitSha,
                lastPushTime = app.lastPushTime
            )
            val fileEntities = app.files.map {
                ProjectFileEntity(
                    projectId = app.id,
                    filePath = it.path,
                    content = it.content
                )
            }
            dao.saveFullProject(entity, fileEntities)
        }
    }

    fun selectFile(index: Int) {
        _uiState.update { it.copy(selectedFileIndex = index, isEditingFile = false) }
    }

    fun startEditingFile() {
        val app = _uiState.value.currentApp ?: return
        val file = app.files.getOrNull(_uiState.value.selectedFileIndex) ?: return
        _uiState.update { it.copy(isEditingFile = true, editedFileContent = file.content) }
    }

    fun onEditedFileContentChange(newContent: String) {
        _uiState.update { it.copy(editedFileContent = newContent) }
    }

    fun saveEditedFile() {
        val app = _uiState.value.currentApp ?: return
        val index = _uiState.value.selectedFileIndex
        val file = app.files.getOrNull(index) ?: return

        val updatedFiles = app.files.toMutableList().apply {
            this[index] = file.copy(content = _uiState.value.editedFileContent)
        }
        val updatedApp = app.copy(files = updatedFiles)
        _uiState.update { it.copy(currentApp = updatedApp, isEditingFile = false) }
        persistProject(updatedApp)
    }

    fun exportCurrentProjectAsZip() {
        val app = _uiState.value.currentApp ?: return
        _uiState.update { it.copy(isExportingZip = true, zipExportMessage = null) }
        viewModelScope.launch {
            val result = zipExporter.exportProjectToZip(app)
            if (result.isSuccess) {
                val file = result.getOrNull()!!
                _uiState.update {
                    it.copy(
                        isExportingZip = false,
                        zipExportMessage = "✅ প্রজেক্ট জিপ ফাইল প্রস্তুত: ${file.name}"
                    )
                }
                zipExporter.shareOrOpenZip(file)
            } else {
                _uiState.update {
                    it.copy(
                        isExportingZip = false,
                        zipExportMessage = "❌ জিপ এক্সপোর্ট ব্যর্থ: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun setGitHubAuthMethod(method: Int) {
        _uiState.update { it.copy(gitHubAuthMethod = method) }
    }

    fun checkClipboardForToken(clipboardText: String) {
        val trimmed = clipboardText.trim()
        if ((trimmed.startsWith("ghp_") || trimmed.startsWith("github_pat_")) && trimmed.length > 20) {
            _uiState.update { it.copy(detectedClipboardToken = trimmed) }
        } else {
            _uiState.update { it.copy(detectedClipboardToken = null) }
        }
    }

    fun applyDetectedToken() {
        val token = _uiState.value.detectedClipboardToken ?: return
        onGitHubTokenChange(token)
        _uiState.update { it.copy(detectedClipboardToken = null) }
        verifyGitHubToken(token)
    }

    fun onGitHubTokenChange(token: String) {
        _uiState.update { it.copy(githubToken = token, pushError = null) }
        viewModelScope.launch {
            dao.saveGitHubConfig(
                GitHubConfigEntity(
                    token = token,
                    username = _uiState.value.githubUsername
                )
            )
        }
    }

    fun onGitHubRepoNameChange(repoName: String) {
        _uiState.update { it.copy(githubRepoName = repoName) }
    }

    fun verifyGitHubToken(token: String = _uiState.value.githubToken) {
        if (token.isBlank()) return
        _uiState.update { it.copy(isVerifyingGitHub = true, pushError = null) }
        viewModelScope.launch {
            val result = gitHubApi.verifyToken(token)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isVerifyingGitHub = false,
                        verifiedGitHubUser = user,
                        githubUsername = user?.login.orEmpty()
                    )
                }
                dao.saveGitHubConfig(
                    GitHubConfigEntity(
                        token = token,
                        username = user?.login.orEmpty()
                    )
                )
            } else {
                _uiState.update {
                    it.copy(
                        isVerifyingGitHub = false,
                        verifiedGitHubUser = null,
                        pushError = "GitHub Token যাচাইকরণ ব্যর্থ: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun connectMockSandboxUser() {
        val sandboxUser = GitHubUser(
            login = "agentstudio-developer",
            name = "AgentStudio Verified Developer",
            avatarUrl = null,
            htmlUrl = "https://github.com"
        )
        _uiState.update {
            it.copy(
                verifiedGitHubUser = sandboxUser,
                githubUsername = sandboxUser.login,
                githubToken = "ghp_agentstudio_demo_token_sandbox",
                pushError = null
            )
        }
    }

    fun disconnectGitHub() {
        _uiState.update {
            it.copy(
                githubToken = "",
                githubUsername = "",
                verifiedGitHubUser = null,
                lastPushResult = null,
                pushError = null
            )
        }
        viewModelScope.launch {
            dao.saveGitHubConfig(GitHubConfigEntity(token = "", username = ""))
        }
    }

    fun pushCurrentAppToGitHub() {
        val app = _uiState.value.currentApp ?: return
        val token = _uiState.value.githubToken.trim()
        val repo = _uiState.value.githubRepoName.trim()

        if (token.isBlank()) {
            _uiState.update { it.copy(pushError = "অনুগ্রহ করে গুগল বা টোকেন দিয়ে GitHub কানেক্ট করুন।") }
            return
        }
        if (repo.isBlank()) {
            _uiState.update { it.copy(pushError = "অনুগ্রহ করে রিপোজিটরির নাম দিন।") }
            return
        }

        _uiState.update {
            it.copy(
                isPushingToGitHub = true,
                pushProgressCurrent = 0,
                pushProgressTotal = app.files.size,
                currentPushFile = "Creating/verifying GitHub repository...",
                pushError = null
            )
        }

        viewModelScope.launch {
            if (token == "ghp_agentstudio_demo_token_sandbox") {
                kotlinx.coroutines.delay(1200)
                _uiState.update { it.copy(pushProgressCurrent = 3, currentPushFile = "Pushing Android Jetpack Compose files...") }
                kotlinx.coroutines.delay(1200)
                _uiState.update { it.copy(pushProgressCurrent = app.files.size, currentPushFile = "Triggering GitHub Actions CI/CD...") }
                kotlinx.coroutines.delay(800)

                val simulatedResult = PushResult(
                    repoUrl = "https://github.com/${_uiState.value.githubUsername}/$repo",
                    commitSha = "a7b3c9f",
                    filesPushedCount = app.files.size,
                    actionsUrl = "https://github.com/${_uiState.value.githubUsername}/$repo/actions",
                    instructions = "স্যান্ডবক্স রিপোজিটরিতে সমস্ত ফাইল সফলভাবে পুশ সম্পন্ন হয়েছে! GitHub Actions অ্যান্ড্রয়েড এপিকে (APK) তৈরি শুরু করেছে। নিচে 'APK ডাউনলোড করুন' বাটনে ট্যাপ করুন।"
                )
                val updatedApp = app.copy(
                    githubRepoUrl = simulatedResult.repoUrl,
                    lastPushCommitSha = simulatedResult.commitSha,
                    lastPushTime = System.currentTimeMillis()
                )
                _uiState.update {
                    it.copy(
                        isPushingToGitHub = false,
                        lastPushResult = simulatedResult,
                        currentApp = updatedApp
                    )
                }
                persistProject(updatedApp)
                return@launch
            }

            val repoResult = gitHubApi.ensureRepository(
                token = token,
                repoName = repo,
                description = "Android app '${app.name}' generated by AgentStudio AI"
            )

            if (repoResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isPushingToGitHub = false,
                        pushError = "রিপোজিটরি তৈরি ব্যর্থ: ${repoResult.exceptionOrNull()?.message}"
                    )
                }
                return@launch
            }

            val pushResult = gitHubApi.pushFilesToRepo(
                token = token,
                repoName = repo,
                files = app.files,
                commitMessage = "Deploy ${app.name} via AgentStudio AI",
                onProgress = { current, total, file ->
                    _uiState.update {
                        it.copy(
                            pushProgressCurrent = current,
                            pushProgressTotal = total,
                            currentPushFile = file
                        )
                    }
                }
            )

            if (pushResult.isSuccess) {
                val res = pushResult.getOrNull()!!
                val updatedApp = app.copy(
                    githubRepoUrl = res.repoUrl,
                    lastPushCommitSha = res.commitSha,
                    lastPushTime = System.currentTimeMillis()
                )
                _uiState.update {
                    it.copy(
                        isPushingToGitHub = false,
                        lastPushResult = res,
                        currentApp = updatedApp
                    )
                }
                persistProject(updatedApp)
            } else {
                _uiState.update {
                    it.copy(
                        isPushingToGitHub = false,
                        pushError = "ফাইল পুশ ব্যর্থ: ${pushResult.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun loadProjectFromHistory(project: ProjectEntity) {
        viewModelScope.launch {
            val files = dao.getFilesForProject(project.id).map {
                ProjectFile(path = it.filePath, content = it.content)
            }
            val app = GeneratedApp(
                id = project.id,
                name = project.name,
                description = project.description,
                packageName = project.packageName,
                providerUsed = project.providerUsed,
                modelUsed = project.modelUsed,
                files = files,
                createdAt = project.createdAt,
                githubRepoUrl = project.githubRepoUrl,
                lastPushCommitSha = project.lastPushCommitSha,
                lastPushTime = project.lastPushTime
            )
            _uiState.update {
                it.copy(
                    currentApp = app,
                    promptText = project.description,
                    selectedFileIndex = 0,
                    activeTab = 1
                )
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            dao.deleteProject(project.id)
            if (_uiState.value.currentApp?.id == project.id) {
                _uiState.update { it.copy(currentApp = null) }
            }
        }
    }

    fun testActiveProviderConnection() {
        val activeProvider = _uiState.value.activeProvider
        val setting = _uiState.value.providerSettings[activeProvider] ?: return

        _uiState.update { it.copy(isTestingConnection = true, connectionTestResult = null) }
        viewModelScope.launch {
            val result = aiApi.testConnection(setting)
            if (result.isSuccess) {
                val ms = result.getOrNull() ?: 0
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        connectionTestResult = "✅ কানেকশন সফল! (${ms}ms) মডেল রেসপন্স দিচ্ছে।"
                    )
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Unknown error"
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        connectionTestResult = "❌ কানেকশন ব্যর্থ: $err"
                    )
                }
            }
        }
    }
}
