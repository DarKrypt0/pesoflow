package com.pesoflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class PesoFlowPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val overviewHeroBg: Color,
    val overviewHeroText: Color,
    val overviewHeroSubtext: Color,
    val budgetsHeroBg: Color,
    val budgetsHeroText: Color,
    val budgetsHeroSubtext: Color,
    val budgetsHeroPillBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val divider: Color,
    val iconCircleBg: Color,
    val iconTint: Color,
    val progressTrack: Color,
    val progressFill: Color,
    val navBarBg: Color,
    val navIndicatorPill: Color,
    val navIconActive: Color,
    val navIconInactive: Color,
    val searchBarBg: Color,
    val chipActiveBg: Color,
    val chipActiveText: Color,
    val positiveGreen: Color
)

val LightPesoFlowPalette = PesoFlowPalette(
    isDark = false,
    background = Color(0xFFFAFBF9),
    surface = Color(0xFFFFFFFF),
    overviewHeroBg = Color(0xFFD2DFC9),      // Soft Sage card on Light Overview
    overviewHeroText = Color(0xFF141915),
    overviewHeroSubtext = Color(0xFF4A584D),
    budgetsHeroBg = Color(0xFF171D19),       // Deep Obsidian card on Light Budgets
    budgetsHeroText = Color(0xFFF5F7F5),
    budgetsHeroSubtext = Color(0xFF9AA69D),
    budgetsHeroPillBg = Color(0xFF2B352E),
    textPrimary = Color(0xFF141915),
    textSecondary = Color(0xFF636E66),
    textMuted = Color(0xFF8B968E),
    divider = Color(0xFFE9ECE8),
    iconCircleBg = Color(0xFFE8EFE4),
    iconTint = Color(0xFF39493C),
    progressTrack = Color(0xFFE6EBE4),
    progressFill = Color(0xFF3E5243),
    navBarBg = Color(0xFFFAFBF9),
    navIndicatorPill = Color(0xFFD2DFC9),
    navIconActive = Color(0xFF141915),
    navIconInactive = Color(0xFF7D8780),
    searchBarBg = Color(0xFFEEF2EC),
    chipActiveBg = Color(0xFF171D19),
    chipActiveText = Color(0xFFFFFFFF),
    positiveGreen = Color(0xFF3B6E4C)
)

val DarkPesoFlowPalette = PesoFlowPalette(
    isDark = true,
    background = Color(0xFF111512),          // Deep Obsidian-Forest background
    surface = Color(0xFF181E1A),
    overviewHeroBg = Color(0xFF28322B),      // Muted Dark Sage card on Dark Overview
    overviewHeroText = Color(0xFFF2F5F2),
    overviewHeroSubtext = Color(0xFF9CA99F),
    budgetsHeroBg = Color(0xFF1D241F),       // Elevated Dark Card on Dark Budgets
    budgetsHeroText = Color(0xFFF2F5F2),
    budgetsHeroSubtext = Color(0xFF8E9B91),
    budgetsHeroPillBg = Color(0xFF2F3A33),
    textPrimary = Color(0xFFF2F5F2),
    textSecondary = Color(0xFF96A299),
    textMuted = Color(0xFF6E7A71),
    divider = Color(0xFF222A24),
    iconCircleBg = Color(0xFF212B24),
    iconTint = Color(0xFFB7C9AE),
    progressTrack = Color(0xFF242D27),
    progressFill = Color(0xFFC5D5BC),
    navBarBg = Color(0xFF111512),
    navIndicatorPill = Color(0xFFC5D5BC),
    navIconActive = Color(0xFF141915),
    navIconInactive = Color(0xFF7E8A81),
    searchBarBg = Color(0xFF1B221D),
    chipActiveBg = Color(0xFFC5D5BC),
    chipActiveText = Color(0xFF141915),
    positiveGreen = Color(0xFF9CC4A7)
)

val LocalPesoFlowColors = staticCompositionLocalOf { DarkPesoFlowPalette }

// Legacy constants kept for compatibility
val ObsidianBg = Color(0xFF111512)
val CharcoalSurface = Color(0xFF181E1A)
val ElevatedCard = Color(0xFF1D241F)
val SubtleBorder = Color(0xFF242E28)
val SagePrimary = Color(0xFFC5D5BC)
val SageBright = Color(0xFFD4E0CD)
val SageMuted = Color(0xFF8E9B91)
val SageContainer = Color(0xFF28322B)
val TextPrimary = Color(0xFFF2F5F2)
val TextSecondary = Color(0xFF96A299)
val TerracottaRose = Color(0xFFE07A5F)
val WarmSand = Color(0xFFD9B468)

private val DarkMaterialColors = darkColorScheme(
    primary = Color(0xFFC5D5BC),
    onPrimary = Color(0xFF141915),
    background = Color(0xFF111512),
    onBackground = Color(0xFFF2F5F2),
    surface = Color(0xFF181E1A),
    onSurface = Color(0xFFF2F5F2),
    outline = Color(0xFF263029)
)

private val LightMaterialColors = lightColorScheme(
    primary = Color(0xFF3E5243),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFAFBF9),
    onBackground = Color(0xFF141915),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF141915),
    outline = Color(0xFFE2E7E0)
)

@Composable
fun PesoFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPesoFlowPalette else LightPesoFlowPalette
    CompositionLocalProvider(LocalPesoFlowColors provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkMaterialColors else LightMaterialColors,
            content = content
        )
    }
}
