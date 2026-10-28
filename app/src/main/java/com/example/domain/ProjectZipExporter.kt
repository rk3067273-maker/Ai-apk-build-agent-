package com.example.domain

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.GeneratedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ProjectZipExporter(private val context: Context) {

    suspend fun exportProjectToZip(app: GeneratedApp): Result<File> = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val zipFileName = "${app.name.lowercase().replace(Regex("[^a-z0-9]"), "_")}_project.zip"
            val zipFile = File(exportDir, zipFileName)

            if (zipFile.exists()) {
                zipFile.delete()
            }

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                for (file in app.files) {
                    val entryName = "${app.name}/${file.path}"
                    val entry = ZipEntry(entryName)
                    zos.putNextEntry(entry)
                    zos.write(file.content.toByteArray(StandardCharsets.UTF_8))
                    zos.closeEntry()
                }
            }

            Result.success(zipFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareOrOpenZip(zipFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Project Export: ${zipFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "প্রজেক্ট জিপ ফাইল সেভ বা শেয়ার করুন").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            // Fallback to general view intent
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/zip")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
