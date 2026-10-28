package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GeneratedApp
import com.example.ui.theme.*

data class VirtualAppItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val value: String,
    val isComplete: Boolean = false
)

@Composable
fun VirtualDeviceSimulator(
    app: GeneratedApp,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(horizontal = 12.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            VirtualDevicePhone(
                app = app,
                isEmbedded = false,
                onDismiss = onDismiss,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualDevicePhone(
    app: GeneratedApp,
    modifier: Modifier = Modifier,
    isEmbedded: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    onOpenFullScreen: (() -> Unit)? = null
) {
    // Simulator states
    var isDarkTheme by remember { mutableStateOf(true) }
    var isOnline by remember { mutableStateOf(true) }
    var activeTab by remember { mutableStateOf(0) }
    var simulatedItems by remember(app.id) {
        mutableStateOf(
            listOf(
                VirtualAppItem("1", "স্মার্ট ফিচার মডিউল", "সরাসরি ইন্টারেক্টিভভাবে টেস্ট হচ্ছে", "Active", false),
                VirtualAppItem("2", "ডাটাবেজ সিঙ্ক ও ক্যাশ", "লোকাল মেমোরিতে রিয়েলটাইম সেভ", "Synced", true),
                VirtualAppItem("3", "এপিআই ও নেটওয়ার্ক ইঞ্জিন", "ক্লাউড কানেক্টর সক্রিয়", "200 OK", false)
            )
        )
    }
    var inputText by remember { mutableStateOf("") }
    var statusNotification by remember { mutableStateOf<String?>(null) }
    var showDiagPanel by remember { mutableStateOf(false) }

    // Color tokens for simulated phone
    val phoneBg = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val phoneSurface = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val phoneTextPrimary = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val phoneTextSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Top Simulator Controls Bar
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldGreen,
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isEmbedded) "লাইভ ভার্চুয়াল ডিভাইস" else "ভার্চুয়াল ডিভাইস (ফুল-স্ক্রিন)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Toggle Theme in virtual phone
                    IconButton(
                        onClick = { isDarkTheme = !isDarkTheme },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkTheme) AmberWarning else NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Toggle Network
                    IconButton(
                        onClick = {
                            isOnline = !isOnline
                            statusNotification = if (isOnline) "নেটওয়ার্ক কানেক্টেড (Online)" else "অফলাইন মোড (Offline)"
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = "Network",
                            tint = if (isOnline) EmeraldGreen else CrimsonError,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Diagnostic toggle
                    IconButton(
                        onClick = { showDiagPanel = !showDiagPanel },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = "Diagnostics",
                            tint = if (showDiagPanel) NeonCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (onOpenFullScreen != null) {
                        IconButton(onClick = onOpenFullScreen, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Full Screen", tint = NeonCyan, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (onDismiss != null) {
                        Spacer(Modifier.width(2.dp))
                        IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

                // The Simulated Android Phone Bezel
                Box(
                    modifier = Modifier
                        .widthIn(max = 370.dp)
                        .weight(1f)
                        .border(3.dp, Color(0xFF334155), RoundedCornerShape(36.dp))
                        .clip(RoundedCornerShape(36.dp))
                        .background(phoneBg)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Phone Status Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "12:45",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = phoneTextPrimary
                            )

                            // Dynamic Camera Punch Hole
                            Box(
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isOnline) {
                                    Icon(Icons.Default.Wifi, contentDescription = null, tint = phoneTextPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text("5G", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = phoneTextPrimary)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.BatteryFull, contentDescription = null, tint = phoneTextPrimary, modifier = Modifier.size(14.dp))
                            }
                        }

                        // App Top Bar
                        Surface(
                            color = phoneSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = NeonCyan.copy(alpha = 0.2f),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = app.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = phoneTextPrimary
                                        )
                                        Text(
                                            text = if (isOnline) "● Live Virtual Mode" else "○ Offline Cached",
                                            fontSize = 10.sp,
                                            color = if (isOnline) EmeraldGreen else CrimsonError
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        simulatedItems = listOf(
                                            VirtualAppItem(
                                                id = System.currentTimeMillis().toString(),
                                                title = "নতুন আইটেম #${simulatedItems.size + 1}",
                                                subtitle = "ভার্চুয়াল ডিভাইসে লাইভ ক্লিক করে যোগ করা হয়েছে",
                                                value = "Added",
                                                isComplete = false
                                            )
                                        ) + simulatedItems
                                        statusNotification = "আইটেম সফলভাবে তৈরি হয়েছে!"
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = "Add Item", tint = NeonCyan)
                                }
                            }
                        }

                        // Toast Notification inside phone
                        AnimatedVisibility(visible = statusNotification != null) {
                            Surface(
                                color = EmeraldGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(text = statusNotification.orEmpty(), fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // App Interactive Content (List & Testing Actions)
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                // Interactive Input Test Form
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = phoneSurface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "🧪 টেস্ট ইনপুট ফিল্ড:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = phoneTextSecondary
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextField(
                                                value = inputText,
                                                onValueChange = { inputText = it },
                                                placeholder = { Text("এখানে লিখে টেস্ট করুন...", fontSize = 12.sp, color = phoneTextSecondary) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = phoneBg,
                                                    unfocusedContainerColor = phoneBg,
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent,
                                                    focusedTextColor = phoneTextPrimary,
                                                    unfocusedTextColor = phoneTextPrimary
                                                ),
                                                singleLine = true
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    if (inputText.isNotBlank()) {
                                                        simulatedItems = listOf(
                                                            VirtualAppItem(
                                                                id = System.currentTimeMillis().toString(),
                                                                title = inputText.trim(),
                                                                subtitle = "ব্যবহারকারীর ইনপুট থেকে ডাটাবেজে সংরক্ষিত",
                                                                value = "User Entry",
                                                                isComplete = false
                                                            )
                                                        ) + simulatedItems
                                                        inputText = ""
                                                        statusNotification = "ডাটা সফলভাবে যুক্ত হয়েছে!"
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text("যোগ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "মডিউল ও ফিচারসমূহ (${simulatedItems.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = phoneTextSecondary
                                    )
                                    Text(
                                        text = "ট্যাপ করে টগল করুন",
                                        fontSize = 10.sp,
                                        color = phoneTextSecondary
                                    )
                                }
                            }

                            items(simulatedItems, key = { it.id }) { item ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = phoneSurface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            simulatedItems = simulatedItems.map {
                                                if (it.id == item.id) it.copy(isComplete = !it.isComplete) else it
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = item.isComplete,
                                            onCheckedChange = { isChecked ->
                                                simulatedItems = simulatedItems.map {
                                                    if (it.id == item.id) it.copy(isComplete = isChecked) else it
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = NeonCyan)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = phoneTextPrimary
                                            )
                                            Text(
                                                text = item.subtitle,
                                                fontSize = 11.sp,
                                                color = phoneTextSecondary
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (item.isComplete) EmeraldGreen.copy(alpha = 0.15f) else NeonCyan.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (item.isComplete) "সম্পন্ন" else item.value,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isComplete) EmeraldGreen else NeonCyan,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // App Bottom Navigation Bar inside virtual phone
                        Surface(
                            color = phoneSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                IconButton(onClick = { activeTab = 0 }) {
                                    Icon(Icons.Default.Home, contentDescription = "Home", tint = if (activeTab == 0) NeonCyan else phoneTextSecondary)
                                }
                                IconButton(onClick = { activeTab = 1 }) {
                                    Icon(Icons.Default.Dashboard, contentDescription = "Modules", tint = if (activeTab == 1) NeonCyan else phoneTextSecondary)
                                }
                                IconButton(onClick = { activeTab = 2 }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = if (activeTab == 2) NeonCyan else phoneTextSecondary)
                                }
                            }
                        }

                        // Android Bottom Gesture Pill Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(phoneTextSecondary.copy(alpha = 0.5f))
                            )
                        }
                    }
                }

                // Diagnostic Panel (Collapsible)
                if (showDiagPanel) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF050811),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("VIRTUAL DEVICE TELEMETRY", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                                Text("FPS: 60.0", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = EmeraldGreen)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Package: ${app.packageName}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                            Text("Engine: Jetpack Compose M3 • State: RESUMED • RAM: 48.2 MB", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
    }
}
