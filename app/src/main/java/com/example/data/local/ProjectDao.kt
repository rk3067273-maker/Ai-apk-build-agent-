package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class ProjectWithFiles(
    val project: ProjectEntity,
    val files: List<ProjectFileEntity>
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM project_files WHERE projectId = :projectId")
    suspend fun getFilesForProject(projectId: String): List<ProjectFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ProjectFileEntity>)

    @Query("DELETE FROM project_files WHERE projectId = :projectId")
    suspend fun deleteFilesForProject(projectId: String)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: String)

    @Transaction
    suspend fun saveFullProject(project: ProjectEntity, files: List<ProjectFileEntity>) {
        insertProject(project)
        deleteFilesForProject(project.id)
        insertFiles(files)
    }

    // Provider Configs
    @Query("SELECT * FROM provider_configs")
    suspend fun getAllProviderConfigs(): List<ProviderConfigEntity>

    @Query("SELECT * FROM provider_configs WHERE providerId = :providerId")
    suspend fun getProviderConfig(providerId: String): ProviderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProviderConfig(config: ProviderConfigEntity)

    // GitHub Config
    @Query("SELECT * FROM github_config WHERE id = 1")
    suspend fun getGitHubConfig(): GitHubConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGitHubConfig(config: GitHubConfigEntity)
}
