package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// AgentStudio Ultra-Premium Deep Obsidian & Neon Palette
val BackgroundDark = Color(0xFF070B14)
val SurfaceDark = Color(0xFF0E1626)
val SurfaceVariantDark = Color(0xFF162035)
val CardBackground = Color(0xFF121B2F)
val CardHighlight = Color(0xFF1A2642)
val BorderDark = Color(0xFF22314E)
val GlassBorder = Color(0x3300F2FE)
val GlassBackground = Color(0x990E1626)

// Neon & Cosmic Accents
val NeonCyan = Color(0xFF00F2FE)
val CyanGlow = Color(0xFF4FACFE)
val StudioPurple = Color(0xFF7C3AED)
val VioletGlow = Color(0xFFA855F7)
val ElectricBlue = Color(0xFF3B82F6)
val EmeraldGreen = Color(0xFF10B981)
val MintGlow = Color(0xFF34D399)
val AmberWarning = Color(0xFFF59E0B)
val CrimsonError = Color(0xFFEF4444)

// Text & Metadata
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)

// Provider Tokens
val OpenAiGreen = Color(0xFF10A37F)
val GeminiBlue = Color(0xFF1A73E8)
val OpenRouterOrange = Color(0xFFFF6422)
val AgentRouterPurple = Color(0xFF8B5CF6)
val GitHubBlack = Color(0xFF181717)

// Premium Gradient Brushes
val CyberpunkGradient = Brush.linearGradient(
    colors = listOf(NeonCyan, VioletGlow)
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(
        NeonCyan.copy(alpha = 0.08f),
        StudioPurple.copy(alpha = 0.08f),
        Color.Transparent
    )
)

val GitHubCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF24292E),
        Color(0xFF111827)
    )
)
