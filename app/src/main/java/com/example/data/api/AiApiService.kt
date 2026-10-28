package com.example.data.api

import android.util.Log
import com.example.data.model.AiProviderType
import com.example.data.model.ProviderSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class AiGenerationResponse(
    val text: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val latencyMs: Long
)

class AiApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "AiApiService"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }

    suspend fun testConnection(setting: ProviderSetting): Result<Long> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val response = generateContent(
                setting = setting,
                systemPrompt = "You are a test ping responder.",
                userPrompt = "Reply with 'PONG' only."
            )
            if (response.isSuccess) {
                Result.success(System.currentTimeMillis() - startTime)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateContent(
        setting: ProviderSetting,
        systemPrompt: String,
        userPrompt: String
    ): Result<AiGenerationResponse> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            when (setting.providerType) {
                AiProviderType.OPEN_AI,
                AiProviderType.OPEN_ROUTER,
                AiProviderType.AGENT_ROUTER -> {
                    callOpenAiCompatible(setting, systemPrompt, userPrompt, startTime)
                }
                AiProviderType.GEMINI -> {
                    callGemini(setting, systemPrompt, userPrompt, startTime)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content", e)
            Result.failure(e)
        }
    }

    private fun callOpenAiCompatible(
        setting: ProviderSetting,
        systemPrompt: String,
        userPrompt: String,
        startTime: Long
    ): Result<AiGenerationResponse> {
        val baseUrl = setting.activeBaseUrl
        val endpoint = if (baseUrl.endsWith("/chat/completions")) {
            baseUrl
        } else {
            "${baseUrl.trimEnd('/')}/chat/completions"
        }

        val requestJson = JSONObject().apply {
            put("model", setting.activeModel)
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            }
            put("messages", messages)
            put("temperature", 0.3)
        }

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA))
            .header("Content-Type", "application/json")

        if (setting.apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer ${setting.apiKey.trim()}")
        }

        if (setting.providerType == AiProviderType.OPEN_ROUTER) {
            requestBuilder.header("HTTP-Referer", "https://agentstudio.ai")
            requestBuilder.header("X-Title", "AgentStudio Android")
        }

        val response = client.newCall(requestBuilder.build()).execute()
        val responseBody = response.body?.string().orEmpty()
        val latency = System.currentTimeMillis() - startTime

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errorJson = JSONObject(responseBody)
                errorJson.optJSONObject("error")?.optString("message")
                    ?: errorJson.optString("message", responseBody)
            } catch (e: Exception) {
                responseBody.ifBlank { "HTTP ${response.code}: ${response.message}" }
            }
            return Result.failure(IOException("API Error [${response.code}]: $errorMsg"))
        }

        return try {
            val root = JSONObject(responseBody)
            val choices = root.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content")

                val usage = root.optJSONObject("usage")
                val promptTokens = usage?.optInt("prompt_tokens") ?: ((systemPrompt.length + userPrompt.length) / 4)
                val compTokens = usage?.optInt("completion_tokens") ?: (content.length / 4)
                val totalTokens = usage?.optInt("total_tokens") ?: (promptTokens + compTokens)

                Result.success(
                    AiGenerationResponse(
                        text = content,
                        promptTokens = promptTokens,
                        completionTokens = compTokens,
                        totalTokens = totalTokens,
                        latencyMs = latency
                    )
                )
            } else {
                Result.failure(IOException("No choices returned from model"))
            }
        } catch (e: Exception) {
            Result.failure(IOException("Failed to parse API response: ${e.message}"))
        }
    }

    private fun callGemini(
        setting: ProviderSetting,
        systemPrompt: String,
        userPrompt: String,
        startTime: Long
    ): Result<AiGenerationResponse> {
        val model = setting.activeModel.removePrefix("models/")
        val baseUrl = setting.activeBaseUrl.trimEnd('/')
        val endpoint = "$baseUrl/models/$model:generateContent?key=${setting.apiKey.trim()}"

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    val parts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemPrompt\n\nUser Request: $userPrompt")
                        })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
            val generationConfig = JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 8192)
            }
            put("generationConfig", generationConfig)
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA))
            .header("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()
        val latency = System.currentTimeMillis() - startTime

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errorJson = JSONObject(responseBody)
                errorJson.optJSONObject("error")?.optString("message")
                    ?: responseBody
            } catch (e: Exception) {
                responseBody.ifBlank { "HTTP ${response.code}: ${response.message}" }
            }
            return Result.failure(IOException("Gemini Error [${response.code}]: $errorMsg"))
        }

        return try {
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val contentObj = candidate.getJSONObject("content")
                val parts = contentObj.getJSONArray("parts")
                val textBuilder = StringBuilder()
                for (i in 0 until parts.length()) {
                    textBuilder.append(parts.getJSONObject(i).optString("text"))
                }
                val content = textBuilder.toString()

                val usage = root.optJSONObject("usageMetadata")
                val promptTokens = usage?.optInt("promptTokenCount") ?: ((systemPrompt.length + userPrompt.length) / 4)
                val compTokens = usage?.optInt("candidatesTokenCount") ?: (content.length / 4)
                val totalTokens = usage?.optInt("totalTokenCount") ?: (promptTokens + compTokens)

                Result.success(
                    AiGenerationResponse(
                        text = content,
                        promptTokens = promptTokens,
                        completionTokens = compTokens,
                        totalTokens = totalTokens,
                        latencyMs = latency
                    )
                )
            } else {
                Result.failure(IOException("Empty response from Gemini"))
            }
        } catch (e: Exception) {
            Result.failure(IOException("Failed to parse Gemini response: ${e.message}"))
        }
    }
}
