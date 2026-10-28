package com.example.data.model

data class ProjectFile(
    val path: String,
    val content: String,
    val language: String = inferLanguage(path),
    val isWorkflow: Boolean = path.contains(".github/workflows")
) {
    companion object {
        fun inferLanguage(path: String): String {
            return when {
                path.endsWith(".kt") -> "kotlin"
                path.endsWith(".xml") -> "xml"
                path.endsWith(".gradle.kts") || path.endsWith(".gradle") -> "gradle"
                path.endsWith(".yml") || path.endsWith(".yaml") -> "yaml"
                path.endsWith(".json") -> "json"
                path.endsWith(".md") -> "markdown"
                path.endsWith(".properties") -> "properties"
                path.endsWith(".pro") -> "proguard"
                else -> "text"
            }
        }
    }
}

data class GeneratedApp(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val packageName: String = "com.aistudio.${name.lowercase().replace(Regex("[^a-z0-9]"), "")}.app",
    val providerUsed: String,
    val modelUsed: String,
    val files: List<ProjectFile>,
    val createdAt: Long = System.currentTimeMillis(),
    val githubRepoUrl: String? = null,
    val lastPushCommitSha: String? = null,
    val lastPushTime: Long? = null
)

data class GeneratedAppWithTelemetry(
    val app: GeneratedApp,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val latencyMs: Long
)

data class PromptTemplate(
    val id: String,
    val title: String,
    val category: String,
    val iconEmoji: String,
    val prompt: String
)

sealed interface GenerationStep {
    object Initializing : GenerationStep
    data class GeneratingCode(val progressMessage: String) : GenerationStep
    object ParsingStructure : GenerationStep
    object InjectingCiCd : GenerationStep
    object Finalizing : GenerationStep
}
