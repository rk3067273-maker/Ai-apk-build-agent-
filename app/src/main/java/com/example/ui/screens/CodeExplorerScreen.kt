package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectFile
import com.example.ui.components.CodeHighlighterView
import com.example.ui.components.InteractiveAppPreview
import com.example.ui.components.VirtualDeviceSimulator
import com.example.ui.theme.*
import com.example.viewmodel.StudioUiState
import com.example.viewmodel.StudioViewModel

@Composable
fun CodeExplorerScreen(
    viewModel: StudioViewModel,
    uiState: StudioUiState,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(1) } // 1: Live Preview (Default), 0: Code

    if (uiState.showVirtualDeviceModal && uiState.currentApp != null) {
        VirtualDeviceSimulator(
            app = uiState.currentApp,
            onDismiss = { viewModel.closeVirtualDevice() }
        )
    }

    val app = uiState.currentApp

    if (app == null) {
        // Empty State
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NeonCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "এখনও কোনো অ্যাপ তৈরি করা হয়নি",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "'Studio' ট্যাবে গিয়ে আপনার অ্যাপের বিবরণ লিখুন বা মুখে বলে 'সম্পূর্ণ অ্যাপ বিল্ড করুন' বাটনে চাপুন।",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.selectTab(0) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BackgroundDark)
                        Spacer(Modifier.width(6.dp))
                        Text("স্টুডিওতে যান", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // App Title & Mode Toggle Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = app.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${app.files.size} টি সোর্স ফাইল • ${app.packageName}",
                    fontSize = 11.sp,
                    color = NeonCyan
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Virtual Device Launcher Button
                IconButton(
                    onClick = { viewModel.openVirtualDevice() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.PhoneAndroid,
                        contentDescription = "Virtual Device",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // ZIP Export Button
                IconButton(
                    onClick = { viewModel.exportCurrentProjectAsZip() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.FolderZip,
                        contentDescription = "Export ZIP",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                // Mode Selector: Preview vs Code
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceDark
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (viewMode == 1) CardBackground else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewMode = 1 }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = if (viewMode == 1) NeonCyan else TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("প্রিভিউ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (viewMode == 1) TextPrimary else TextSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (viewMode == 0) CardBackground else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewMode = 0 }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = if (viewMode == 0) NeonCyan else TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("কোড", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (viewMode == 0) TextPrimary else TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Quick Push to GitHub trigger button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioPurple.copy(alpha = 0.15f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurple.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = VioletGlow, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "গিটহাবে পুশ করে এপিকে পেতে চান?",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
                TextButton(
                    onClick = { viewModel.selectTab(2) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("GitHub ডিপ্লয় ট্যাব →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                }
            }
        }

        if (viewMode == 0) {
            // File tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                app.files.forEachIndexed { index, file ->
                    val isSelected = index == uiState.selectedFileIndex
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else SurfaceDark,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectFile(index) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (file.language) {
                                    "kotlin" -> "kt"
                                    "xml" -> "xml"
                                    "gradle" -> "gradle"
                                    "yaml" -> "ci"
                                    else -> "doc"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonCyan else TextTertiary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = file.path.substringAfterLast('/'),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }

            // Code Display / Editor
            val currentFile = app.files.getOrNull(uiState.selectedFileIndex) ?: app.files.first()
            CodeHighlighterView(
                file = currentFile,
                isEditing = uiState.isEditingFile,
                editedContent = uiState.editedFileContent,
                onContentChange = { viewModel.onEditedFileContentChange(it) },
                onSaveEdit = { viewModel.saveEditedFile() },
                onStartEdit = { viewModel.startEditingFile() },
                modifier = Modifier.weight(1f)
            )
        } else {
            // Live Interactive Preview
            InteractiveAppPreview(
                app = app,
                onOpenFullScreen = { viewModel.openVirtualDevice() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
