package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectFile
import com.example.ui.theme.*

@Composable
fun CodeHighlighterView(
    file: ProjectFile,
    isEditing: Boolean,
    editedContent: String,
    onContentChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onStartEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CardBackground, RoundedCornerShape(12.dp))
    ) {
        // Code Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (file.language) {
                        "kotlin" -> StudioPurple.copy(alpha = 0.25f)
                        "xml" -> EmeraldGreen.copy(alpha = 0.25f)
                        "yaml" -> OpenRouterOrange.copy(alpha = 0.25f)
                        else -> NeonCyan.copy(alpha = 0.25f)
                    }
                ) {
                    Text(
                        text = file.language.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (file.language) {
                            "kotlin" -> VioletGlow
                            "xml" -> EmeraldGreen
                            "yaml" -> OpenRouterOrange
                            else -> NeonCyan
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = file.path.substringAfterLast('/'),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEditing) {
                    Button(
                        onClick = onSaveEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Save", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save", fontSize = 12.sp)
                    }
                } else {
                    IconButton(
                        onClick = onStartEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit File",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("code", if (isEditing) editedContent else file.content)
                        clipboard.setPrimaryClip(clip)
                        copied = true
                        Toast.makeText(context, "কোড কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (copied) Icons.Default.Done else Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = if (copied) EmeraldGreen else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Divider(color = BorderDark, thickness = 1.dp)

        if (isEditing) {
            TextField(
                value = editedContent,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonCyan
                ),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )
        } else {
            // View Mode with line numbers & syntax styling
            val lines = remember(file.content) { file.content.lines() }
            val vScrollState = rememberScrollState()
            val hScrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(vScrollState)
            ) {
                // Line numbers
                Column(
                    modifier = Modifier
                        .background(SurfaceDark)
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.forEachIndexed { i, _ ->
                        Text(
                            text = "${i + 1}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = TextTertiary
                        )
                    }
                }

                // Code text
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(hScrollState)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    lines.forEach { line ->
                        Text(
                            text = highlightSyntax(line, file.language),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

private fun highlightSyntax(line: String, language: String): AnnotatedString {
    return buildAnnotatedString {
        val trimmed = line.trimStart()
        if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("<!--")) {
            append(line)
            addStyle(SpanStyle(color = Color(0xFF6B7280)), 0, line.length)
            return@buildAnnotatedString
        }

        append(line)

        val keywords = listOf(
            "package", "import", "class", "fun", "val", "var", "override",
            "if", "else", "when", "return", "data", "sealed", "interface",
            "object", "companion", "private", "public", "internal", "plugins",
            "dependencies", "implementation", "android", "defaultConfig", "name",
            "uses-permission", "application", "activity"
        )

        for (kw in keywords) {
            val regex = Regex("\\b$kw\\b")
            regex.findAll(line).forEach { match ->
                addStyle(
                    SpanStyle(color = VioletGlow, fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // Highlight strings
        val stringRegex = Regex("\".*?\"")
        stringRegex.findAll(line).forEach { match ->
            addStyle(
                SpanStyle(color = EmeraldGreen),
                match.range.first,
                match.range.last + 1
            )
        }

        // Highlight annotations
        val annotationRegex = Regex("@[A-Za-z0-9_]+")
        annotationRegex.findAll(line).forEach { match ->
            addStyle(
                SpanStyle(color = NeonCyan),
                match.range.first,
                match.range.last + 1
            )
        }
    }
}
