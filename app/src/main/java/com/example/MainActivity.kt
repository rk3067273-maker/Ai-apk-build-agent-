package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HeaderBar
import com.example.ui.components.ProjectHistoryDialog
import com.example.ui.screens.CodeExplorerScreen
import com.example.ui.screens.GitHubDeployScreen
import com.example.ui.screens.ProviderSettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AgentStudioMainApp()
            }
        }
    }
}

@Composable
fun AgentStudioMainApp(
    viewModel: StudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showHistoryDialog by remember { mutableStateOf(false) }

    // Intercept back button if on sub-screens
    BackHandler(enabled = uiState.activeTab != 0) {
        viewModel.selectTab(0)
    }

    if (showHistoryDialog) {
        ProjectHistoryDialog(
            projects = uiState.savedProjects,
            onSelectProject = { project ->
                viewModel.loadProjectFromHistory(project)
            },
            onDeleteProject = { project ->
                viewModel.deleteProject(project)
            },
            onDismiss = { showHistoryDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("agent_studio_scaffold"),
        containerColor = BackgroundDark,
        topBar = {
            HeaderBar(
                activeProvider = uiState.activeProvider,
                activeModel = uiState.providerSettings[uiState.activeProvider]?.activeModel.orEmpty().ifBlank { "Gemini 2.0" },
                onOpenSettings = { viewModel.selectTab(3) },
                onOpenHistory = { showHistoryDialog = true },
                onNewChat = { viewModel.clearChat() },
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("studio_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = "Studio") },
                    label = { Text("Studio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0B0F19),
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.currentApp != null) {
                                    Badge(containerColor = EmeraldGreen) {
                                        Text("Live")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = "Preview & Code")
                        }
                    },
                    label = { Text("Preview & Code", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0B0F19),
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.lastPushResult != null) {
                                    Badge(containerColor = EmeraldGreen) {
                                        Text("✓")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "GitHub & APK")
                        }
                    },
                    label = { Text("GitHub & APK", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0B0F19),
                        selectedTextColor = VioletGlow,
                        indicatorColor = VioletGlow,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "AI Providers") },
                    label = { Text("AI Providers", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0B0F19),
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                0 -> StudioScreen(viewModel = viewModel, uiState = uiState)
                1 -> CodeExplorerScreen(viewModel = viewModel, uiState = uiState)
                2 -> GitHubDeployScreen(viewModel = viewModel, uiState = uiState)
                3 -> ProviderSettingsScreen(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}
