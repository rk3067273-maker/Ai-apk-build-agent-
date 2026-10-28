package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF03101C),
    primaryContainer = Color(0xFF0F3B57),
    onPrimaryContainer = Color(0xFFC7EFFF),
    secondary = VioletGlow,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3B1F69),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    error = CrimsonError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Developer studio defaults to high-contrast dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
