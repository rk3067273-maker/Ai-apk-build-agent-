package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun VoiceInputDialog(
    onDismiss: () -> Unit,
    onSpeechRecognized: (String) -> Unit
) {
    val context = LocalContext.current
    var spokenText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("শুনছি... আপনার অ্যাপের বর্ণনা মুখে বলুন") }

    // Pulsing animation for microphone
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Standard Android Recognizer intent launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                spokenText = spoken
                statusMessage = "ভয়েস রেকর্ড সফল!"
            } else {
                statusMessage = "কিছু শোনা যায়নি, আবার চেষ্টা করুন"
            }
        } else {
            statusMessage = "ভয়েস ইনপুট বাতিল হয়েছে"
        }
        isListening = false
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "আপনার অ্যাপের বর্ণনা বলুন...")
            }
            try {
                isListening = true
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                // Fallback to default locale
                try {
                    val fallbackIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your app requirements...")
                    }
                    speechLauncher.launch(fallbackIntent)
                } catch (e2: Exception) {
                    Toast.makeText(context, "Speech Recognition পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                    isListening = false
                }
            }
        } else {
            Toast.makeText(context, "Microphone অনুমতি প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ভয়েস ইনপুট (Speech to Text)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Pulsing Mic Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(pulseScale)
                                .background(NeonCyan.copy(alpha = 0.2f), CircleShape)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isListening) NeonCyan else CardBackground,
                        modifier = Modifier
                            .size(72.dp)
                            .border(2.dp, if (isListening) NeonCyan else BorderDark, CircleShape),
                        onClick = {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Microphone",
                                tint = if (isListening) Color(0xFF0B0F19) else NeonCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = statusMessage,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isListening) NeonCyan else TextSecondary,
                    textAlign = TextAlign.Center
                )

                if (spokenText.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Text(
                            text = "\"$spokenText\"",
                            fontSize = 14.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Text("পুনরায় বলুন")
                    }

                    Button(
                        onClick = {
                            if (spokenText.isNotBlank()) {
                                onSpeechRecognized(spokenText)
                            }
                            onDismiss()
                        },
                        enabled = spokenText.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF0B0F19), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ইনসার্ট করুন", color = Color(0xFF0B0F19), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
