package com.pesoflow.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Minimalist Obsidian Black & Sage Green Palette
val ObsidianBg = Color(0xFF0B0F0D)
val CharcoalSurface = Color(0xFF121815)
val ElevatedCard = Color(0xFF171F1B)
val SubtleBorder = Color(0xFF232F29)

val SagePrimary = Color(0xFF9CAF88)
val SageBright = Color(0xFFC2D4B2)
val SageMuted = Color(0xFF687A61)
val SageContainer = Color(0xFF1E2922)

val TextPrimary = Color(0xFFF3F6F4)
val TextSecondary = Color(0xFF8E9B94)
val TerracottaRose = Color(0xFFE07A5F)
val WarmSand = Color(0xFFD9B468)

// Backwards-compatible aliases used across screens
val GCashBlue = SagePrimary
val EmeraldGreen = SageBright
val AmberWarning = WarmSand
val RoseExpense = TerracottaRose

private val ObsidianSageColorScheme = darkColorScheme(
    primary = SagePrimary,
    onPrimary = ObsidianBg,
    primaryContainer = SageContainer,
    onPrimaryContainer = SageBright,
    secondary = SageBright,
    onSecondary = ObsidianBg,
    tertiary = WarmSand,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedCard,
    onSurfaceVariant = TextSecondary,
    outline = SubtleBorder
)

@Composable
fun PesoFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ObsidianSageColorScheme,
        content = content
    )
}
