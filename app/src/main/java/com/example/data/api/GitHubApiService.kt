package com.example.data.api

import android.util.Base64
import android.util.Log
import com.example.data.model.ProjectFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets

data class GitHubUser(
    val login: String,
    val name: String?,
    val avatarUrl: String?,
    val htmlUrl: String
)

data class PushResult(
    val repoUrl: String,
    val commitSha: String,
    val filesPushedCount: Int,
    val actionsUrl: String,
    val instructions: String
)

class GitHubApiService(
    private val client: OkHttpClient = OkHttpClient()
) {
    companion object {
        private const val TAG = "GitHubApiService"
        private const val GITHUB_API = "https://api.github.com"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }

    suspend fun verifyToken(token: String): Result<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$GITHUB_API/user")
                .header("Authorization", "Bearer ${token.trim()}")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "AgentStudio-Android")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("GitHub Auth Failed [${response.code}]: $body"))
            }

            val json = JSONObject(body)
            val user = GitHubUser(
                login = json.getString("login"),
                name = json.optString("name", null),
                avatarUrl = json.optString("avatar_url", null),
                htmlUrl = json.getString("html_url")
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureRepository(
        token: String,
        repoName: String,
        description: String,
        isPrivate: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // First check if repo already exists
            val userRes = verifyToken(token)
            val username = userRes.getOrNull()?.login ?: return@withContext Result.failure(IOException("Invalid token"))

            val checkRequest = Request.Builder()
                .url("$GITHUB_API/repos/$username/$repoName")
                .header("Authorization", "Bearer ${token.trim()}")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "AgentStudio-Android")
                .get()
                .build()

            val checkResponse = client.newCall(checkRequest).execute()
            if (checkResponse.isSuccessful) {
                val json = JSONObject(checkResponse.body?.string().orEmpty())
                return@withContext Result.success(json.getString("html_url"))
            }

            // Create new repository
            val payload = JSONObject().apply {
                put("name", repoName)
                put("description", description)
                put("private", isPrivate)
                put("auto_init", true)
            }

            val createRequest = Request.Builder()
                .url("$GITHUB_API/user/repos")
                .header("Authorization", "Bearer ${token.trim()}")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "AgentStudio-Android")
                .post(payload.toString().toRequestBody(JSON_MEDIA))
                .build()

            val createResponse = client.newCall(createRequest).execute()
            val createBody = createResponse.body?.string().orEmpty()

            if (!createResponse.isSuccessful) {
                return@withContext Result.failure(IOException("Failed to create repository [${createResponse.code}]: $createBody"))
            }

            val createdJson = JSONObject(createBody)
            Result.success(createdJson.getString("html_url"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushFilesToRepo(
        token: String,
        repoName: String,
        files: List<ProjectFile>,
        commitMessage: String = "Build project with AgentStudio AI",
        onProgress: (current: Int, total: Int, currentFile: String) -> Unit = { _, _, _ -> }
    ): Result<PushResult> = withContext(Dispatchers.IO) {
        try {
            val userRes = verifyToken(token)
            val username = userRes.getOrNull()?.login ?: return@withContext Result.failure(IOException("Token verification failed"))

            var lastCommitSha = "main"
            var pushedCount = 0

            for ((index, file) in files.withIndex()) {
                onProgress(index + 1, files.size, file.path)

                // 1. Check if file already exists to get its sha
                val fileUrl = "$GITHUB_API/repos/$username/$repoName/contents/${file.path}"
                val getRequest = Request.Builder()
                    .url(fileUrl)
                    .header("Authorization", "Bearer ${token.trim()}")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "AgentStudio-Android")
                    .get()
                    .build()

                val getResponse = client.newCall(getRequest).execute()
                var existingSha: String? = null
                if (getResponse.isSuccessful) {
                    val getBody = getResponse.body?.string().orEmpty()
                    if (getBody.isNotBlank()) {
                        existingSha = JSONObject(getBody).optString("sha")
                    }
                }

                // 2. Encode content to Base64
                val base64Content = Base64.encodeToString(
                    file.content.toByteArray(StandardCharsets.UTF_8),
                    Base64.NO_WRAP
                )

                // 3. Put / Update file
                val putPayload = JSONObject().apply {
                    put("message", "$commitMessage: ${file.path}")
                    put("content", base64Content)
                    if (!existingSha.isNullOrBlank()) {
                        put("sha", existingSha)
                    }
                }

                val putRequest = Request.Builder()
                    .url(fileUrl)
                    .header("Authorization", "Bearer ${token.trim()}")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "AgentStudio-Android")
                    .put(putPayload.toString().toRequestBody(JSON_MEDIA))
                    .build()

                val putResponse = client.newCall(putRequest).execute()
                val putBody = putResponse.body?.string().orEmpty()

                if (putResponse.isSuccessful) {
                    pushedCount++
                    try {
                        val putJson = JSONObject(putBody)
                        lastCommitSha = putJson.getJSONObject("commit").getString("sha")
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not parse commit sha from response", e)
                    }
                } else {
                    Log.e(TAG, "Failed pushing file: ${file.path}: $putBody")
                }
            }

            val repoUrl = "https://github.com/$username/$repoName"
            val actionsUrl = "$repoUrl/actions"
            val instructions = "গিটহাবে পুশ সম্পন্ন হয়েছে! GitHub Actions ওয়ার্কফ্লো স্বয়ংক্রিয়ভাবে APK বিল্ড করা শুরু করেছে। Actions ট্যাব থেকে বিল্ড শেষ হলে সরাসরি APK ডাউনলোড করতে পারবেন।"

            Result.success(
                PushResult(
                    repoUrl = repoUrl,
                    commitSha = lastCommitSha,
                    filesPushedCount = pushedCount,
                    actionsUrl = actionsUrl,
                    instructions = instructions
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun installWorkflowToRepo(
        token: String,
        repoName: String,
        workflowContent: String = DEFAULT_WORKFLOW_YML
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userRes = verifyToken(token)
            val username = userRes.getOrNull()?.login ?: return@withContext Result.failure(IOException("Invalid token or authentication failed"))

            val filePath = ".github/workflows/build-apk.yml"
            val fileUrl = "$GITHUB_API/repos/$username/$repoName/contents/$filePath"

            // Check if file already exists to get its sha
            val getRequest = Request.Builder()
                .url(fileUrl)
                .header("Authorization", "Bearer ${token.trim()}")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "AgentStudio-Android")
                .get()
                .build()

            val getResponse = client.newCall(getRequest).execute()
            var existingSha: String? = null
            if (getResponse.isSuccessful) {
                val getBody = getResponse.body?.string().orEmpty()
                if (getBody.isNotBlank()) {
                    existingSha = JSONObject(getBody).optString("sha")
                }
            }

            val base64Content = Base64.encodeToString(
                workflowContent.toByteArray(StandardCharsets.UTF_8),
                Base64.NO_WRAP
            )

            val putPayload = JSONObject().apply {
                put("message", "Add GitHub Actions APK Builder CI/CD workflow")
                put("content", base64Content)
                if (!existingSha.isNullOrBlank()) {
                    put("sha", existingSha)
                }
            }

            val putRequest = Request.Builder()
                .url(fileUrl)
                .header("Authorization", "Bearer ${token.trim()}")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "AgentStudio-Android")
                .put(putPayload.toString().toRequestBody(JSON_MEDIA))
                .build()

            val putResponse = client.newCall(putRequest).execute()
            val putBody = putResponse.body?.string().orEmpty()
            if (!putResponse.isSuccessful) {
                return@withContext Result.failure(IOException("Failed to install workflow [${putResponse.code}]: $putBody"))
            }

            Result.success("https://github.com/$username/$repoName/actions")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val defaultWorkflowYml: String
        get() = DEFAULT_WORKFLOW_YML
}

const val DEFAULT_WORKFLOW_YML: String = """name: Build Android APK

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build:
    name: Build & Release Android APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Prepare Keystore and Environment
        run: |
          if [ ! -f ".env" ] && [ -f ".env.example" ]; then
            cp .env.example .env
          elif [ ! -f ".env" ]; then
            touch .env
          fi

          if [ -f "debug.keystore.base64" ] && [ ! -f "debug.keystore" ]; then
            base64 -d debug.keystore.base64 > debug.keystore || true
          fi
          if [ ! -f "debug.keystore" ]; then
            keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" || true
          fi

      - name: Ensure Gradle Wrapper Exists
        run: |
          if [ ! -f "./gradlew" ]; then
            gradle wrapper || true
          fi
          if [ -f "./gradlew" ]; then
            chmod +x gradlew
          fi

      - name: Build Debug APK
        run: |
          if [ -f "./gradlew" ]; then
            ./gradlew assembleDebug --stacktrace
          else
            gradle assembleDebug --stacktrace
          fi

      - name: Upload Debug APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-debug-apk
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 30

      - name: Create GitHub Release with APK
        if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master'
        uses: softprops/action-gh-release@v2
        with:
          files: app/build/outputs/apk/debug/*.apk
          tag_name: apk-v${"$"}{{ github.run_number }}
          name: Android APK v${"$"}{{ github.run_number }}
          body: |
            ### 📱 Android APK Successfully Built!
            
            **Installation Instructions:**
            1. Download the `.apk` file below directly to your Android device.
            2. Tap on the downloaded file to install.
            3. If prompted, allow **Install from Unknown Sources** in Settings.
            4. Launch and enjoy!
        env:
          GITHUB_TOKEN: ${"$"}{{ secrets.GITHUB_TOKEN }}
        continue-on-error: true
"""

