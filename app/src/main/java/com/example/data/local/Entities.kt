package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val packageName: String,
    val providerUsed: String,
    val modelUsed: String,
    val createdAt: Long,
    val githubRepoUrl: String? = null,
    val lastPushCommitSha: String? = null,
    val lastPushTime: Long? = null
)

@Entity(
    tableName = "project_files",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class ProjectFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: String,
    val filePath: String,
    val content: String
)

@Entity(tableName = "provider_configs")
data class ProviderConfigEntity(
    @PrimaryKey val providerId: String,
    val apiKey: String,
    val customBaseUrl: String,
    val selectedModel: String,
    val customModelsJson: String = ""
)

@Entity(tableName = "github_config")
data class GitHubConfigEntity(
    @PrimaryKey val id: Int = 1,
    val token: String,
    val username: String,
    val defaultBranch: String = "main"
)
