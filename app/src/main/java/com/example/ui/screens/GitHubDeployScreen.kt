package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.StudioUiState
import com.example.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubDeployScreen(
    viewModel: StudioViewModel,
    uiState: StudioUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isTokenVisible by remember { mutableStateOf(false) }
    var isWorkflowCodeExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Auto check clipboard for token on screen entry
    LaunchedEffect(Unit) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
            viewModel.checkClipboardForToken(clipText)
        } catch (_: Exception) {}
    }

    val currentApp = uiState.currentApp

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ultra-Premium Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardGlowGradient)
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = BackgroundDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "GitHub অটো-ডিপ্লয় ও APK রিলিজ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "গুগল একাউন্ট বা টোকেন দিয়ে অতি সহজে কানেক্ট করুন",
                                fontSize = 11.sp,
                                color = NeonCyan
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "গিটহাবে পুশ করার সাথে সাথে ক্লাউডে GitHub Actions স্বয়ংক্রিয়ভাবে আপনার অ্যাপের Android APK বিল্ড করে রেডি করে ফেলবে।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Auto-Detected Clipboard Token Banner (Instant 1-Tap Connect)
        if (uiState.detectedClipboardToken != null && uiState.verifiedGitHubUser == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NeonCyan.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ক্লিপবোর্ডে গিটহাব টোকেন পাওয়া গেছে!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                text = uiState.detectedClipboardToken.take(16) + "...",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.applyDetectedToken() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("কানেক্ট করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                    }
                }
            }
        }

        // GitHub Account Connection Status
        if (uiState.verifiedGitHubUser != null) {
            // Already Connected Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreen),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(26.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@${uiState.verifiedGitHubUser.login}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmeraldGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "CONNECTED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "GitHub পুশ করার জন্য প্রস্তুত",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    TextButton(
                        onClick = { viewModel.disconnectGitHub() }
                    ) {
                        Text("পরিবর্তন", fontSize = 12.sp, color = CrimsonError)
                    }
                }
            }
        } else {
            // Dual Login Selector: Google/Browser vs Direct Token
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GitHub কানেক্ট করার মাধ্যম বেছে নিন:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(12.dp))

                    // Option Selector Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (uiState.gitHubAuthMethod == 0) NeonCyan.copy(alpha = 0.15f) else CardBackground,
                            border = if (uiState.gitHubAuthMethod == 0) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setGitHubAuthMethod(0) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌐 গুগল / ব্রাউজার", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (uiState.gitHubAuthMethod == 0) NeonCyan else TextSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (uiState.gitHubAuthMethod == 1) NeonCyan.copy(alpha = 0.15f) else CardBackground,
                            border = if (uiState.gitHubAuthMethod == 1) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setGitHubAuthMethod(1) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔑 টোকেন (PAT)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (uiState.gitHubAuthMethod == 1) NeonCyan else TextSecondary)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (uiState.gitHubAuthMethod == 0) {
                        // Method 0: Google / Browser Sign-in with pre-selected scopes
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "গুগল অ্যাকাউন্ট দিয়ে গিটহাব লগইন",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "নিচের বাটনে ট্যাপ করলে গিটহাবের পেজ খুলবে যেখানে আপনি সরাসরি গুগল দিয়ে লগইন করতে পারবেন। টোকেন জেনারেট করে কপি করলেই এই অ্যাপ তা নিজে থেকেই ডিটেক্ট করে নেবে।",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 17.sp
                                )
                                Spacer(Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://github.com/settings/tokens/new?description=AgentStudio-Mobile&scopes=repo,workflow")
                                        )
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("গুগল দিয়ে গিটহাবে যান ও টোকেন আনুন ↗", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                                }

                                Spacer(Modifier.height(10.dp))

                                // Quick Instant Sandbox Connect Option
                                OutlinedButton(
                                    onClick = { viewModel.connectMockSandboxUser() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("টোকেন ছাড়া দ্রুত টেস্ট করতে চান? স্যান্ডবক্স ব্যবহার করুন", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        // Method 1: Direct Personal Access Token (PAT)
                        Column {
                            OutlinedTextField(
                                value = uiState.githubToken,
                                onValueChange = { viewModel.onGitHubTokenChange(it) },
                                label = { Text("Personal Access Token (ghp_...)") },
                                placeholder = { Text("ghp_xxxxxxxxxxxxxxxxxxxx") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                        Icon(
                                            if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        try {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                                            if (text.isNotBlank()) {
                                                viewModel.onGitHubTokenChange(text.trim())
                                                viewModel.verifyGitHubToken(text.trim())
                                            }
                                        } catch (_: Exception) {}
                                    }
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("ক্লিপবোর্ড থেকে পেস্ট", fontSize = 12.sp, color = NeonCyan)
                                }

                                Button(
                                    onClick = { viewModel.verifyGitHubToken() },
                                    enabled = uiState.githubToken.isNotBlank() && !uiState.isVerifyingGitHub,
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    if (uiState.isVerifyingGitHub) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = BackgroundDark)
                                    } else {
                                        Text("যাচাই করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Repository & Pre-check Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "টার্গেট রিপোজিটরি নাম:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.githubRepoName,
                    onValueChange = { viewModel.onGitHubRepoNameChange(it) },
                    placeholder = { Text("my-agent-app") },
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

                if (currentApp != null) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "প্রজেক্ট: ${currentApp.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${currentApp.files.size} টি সোর্স ফাইল",
                                    fontSize = 11.sp,
                                    color = NeonCyan
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "✅ GitHub Actions CI/CD অটো-বিল্ডার অন্তর্ভুক্ত রয়েছে। পুশ করলেই স্বয়ংক্রিয়ভাবে APK তৈরি হবে।",
                                fontSize = 11.sp,
                                color = EmeraldGreen,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // GitHub Actions CI/CD Workflow Setup & Fix Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.workflowInstallMessage != null) EmeraldGreen else AmberWarning.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (uiState.workflowInstallMessage != null) EmeraldGreen.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (uiState.workflowInstallMessage != null) Icons.Default.CheckCircle else Icons.Default.Build,
                                contentDescription = null,
                                tint = if (uiState.workflowInstallMessage != null) EmeraldGreen else AmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GitHub Actions APK বিল্ডার সেটআপ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (uiState.workflowInstallMessage != null) "ওয়ার্কফ্লো ইনস্টল সফল" else "Actions ট্যাবে APK বিল্ড চালু করার সমাধান",
                            fontSize = 11.sp,
                            color = if (uiState.workflowInstallMessage != null) EmeraldGreen else AmberWarning
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "GitHub Actions-এ 'Get started with GitHub Actions' দেখার কারণ হলো রিপোজিটরিতে '.github/workflows/build-apk.yml' ফাইলটি নেই। নিচের বাটনে ট্যাপ করলে স্বয়ংক্রিয়ভাবে আপনার রিপোজিটরিতে এটি পুশ হয়ে যাবে এবং সাথে সাথে ক্লাউডে APK বিল্ড রান করবে।",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )

                if (uiState.workflowInstallMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = uiState.workflowInstallMessage,
                                fontSize = 12.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // 1-Click Install Workflow to Repo Button
                Button(
                    onClick = { viewModel.installActionsWorkflow() },
                    enabled = !uiState.isInstallingWorkflow && uiState.githubToken.isNotBlank() && uiState.githubRepoName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.workflowInstallMessage != null) EmeraldGreen else NeonCyan,
                        contentColor = BackgroundDark
                    )
                ) {
                    if (uiState.isInstallingWorkflow) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundDark, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Actions বিল্ডার পুশ করা হচ্ছে...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (uiState.workflowInstallMessage != null) "পুনরায় Workflow আপডেট করুন" else "⚡ ১-ক্লিকে Actions APK বিল্ডার ইনস্টল করুন",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Copy YAML & Manual Browser Paste Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("GitHub Actions Workflow YAML", viewModel.getWorkflowYaml())
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "✅ Workflow YAML ক্লিপবোর্ডে কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "কপি ব্যর্থ: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("কপি YAML", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val user = uiState.githubUsername.ifBlank { "rk3067273-maker" }
                            val repo = uiState.githubRepoName.ifBlank { "Ai-apk-builder" }
                            val url = "https://github.com/$user/$repo/new/main?filename=.github/workflows/build-apk.yml"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("GitHub এডিটরে যান ↗", fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = { isWorkflowCodeExpanded = !isWorkflowCodeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (isWorkflowCodeExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isWorkflowCodeExpanded) "Workflow কোড লুকান" else "সম্পূর্ণ Workflow YAML কোড প্রিভিউ দেখুন",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                if (isWorkflowCodeExpanded) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BackgroundDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = viewModel.getWorkflowYaml(),
                            fontSize = 10.sp,
                            color = NeonCyan,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }


        // Push Progress Indicator
        if (uiState.isPushingToGitHub) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🚀 ক্লাউডে ফাইল পুশ করা হচ্ছে...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "${uiState.pushProgressCurrent}/${uiState.pushProgressTotal}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    val progressFloat = if (uiState.pushProgressTotal > 0) {
                        uiState.pushProgressCurrent.toFloat() / uiState.pushProgressTotal.toFloat()
                    } else 0f
                    LinearProgressIndicator(
                        progress = { progressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan,
                        trackColor = BorderDark
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.currentPushFile,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        // Push Error Message
        if (uiState.pushError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CrimsonError.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = CrimsonError)
                    Spacer(Modifier.width(10.dp))
                    Text(text = uiState.pushError, fontSize = 13.sp, color = CrimsonError)
                }
            }
        }

        // Main Action Button: 1-Click Push to GitHub
        Button(
            onClick = { viewModel.pushCurrentAppToGitHub() },
            enabled = currentApp != null && !uiState.isPushingToGitHub,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioPurple,
                disabledContainerColor = StudioPurple.copy(alpha = 0.3f)
            )
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (uiState.isPushingToGitHub) "পুশ করা হচ্ছে..." else "🚀 গিটহাবে পুশ করুন এবং APK বিল্ড শুরু করুন",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Direct ZIP Export Option (Alternate route to APK)
        if (currentApp != null) {
            OutlinedButton(
                onClick = { viewModel.exportCurrentProjectAsZip() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.FolderZip, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("📦 ফোনে সম্পূর্ণ প্রজেক্ট ZIP হিসেবে ডাউনলোড করুন", fontSize = 12.sp)
            }
        }

        if (uiState.zipExportMessage != null) {
            Text(
                text = uiState.zipExportMessage,
                fontSize = 12.sp,
                color = if (uiState.zipExportMessage.startsWith("✅")) EmeraldGreen else CrimsonError
            )
        }

        // Post-Push APK Download Center
        if (uiState.lastPushResult != null) {
            val result = uiState.lastPushResult
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreen)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldGreen,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "সফলভাবে গিটহাবে ডিপ্লয় হয়েছে!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                            Text(
                                text = "GitHub Actions ক্লাউডে APK বিল্ড করছে",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "📱 ফোনে APK ইনস্টল করার সহজ ৩ ধাপ:\n1. নিচের 'APK ডাউনলোড করুন' বাটনে ক্লিক করে GitHub Actions পেজে যান।\n2. 'Build and Release Android APK' রানটিতে ক্লিক করুন।\n3. নিচে 'Artifacts' থেকে সরাসরি .apk ফাইলটি ডাউনলোড করে ফোনে ইনস্টল করুন!",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.actionsUrl))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("APK ডাউনলোড করুন", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.repoUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("রিপোজিটরি", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
