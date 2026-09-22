package com.hamdan.retroarcade.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Accent options exposed to the rest of the app ────────────────────────────
enum class AccentColor(
    val label: String,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color
) {
    NEON_GREEN(
        label = "Neon Green",
        primary = NeonGreen,
        onPrimary = Color(0xFF002200),
        primaryContainer = NeonGreenDark,
        onPrimaryContainer = NeonGreenLight
    ),
    ELECTRIC_BLUE(
        label = "Electric Blue",
        primary = ElectricBlue,
        onPrimary = Color(0xFF003344),
        primaryContainer = ElectricBlueDark,
        onPrimaryContainer = ElectricBlueLight
    ),
    RETRO_PURPLE(
        label = "Retro Purple",
        primary = RetroPurple,
        onPrimary = Color(0xFF1A0033),
        primaryContainer = RetroPurpleDark,
        onPrimaryContainer = RetroPurpleLight
    ),
    RETRO_AMBER(
        label = "Amber",
        primary = RetroAmber,
        onPrimary = Color(0xFF332200),
        primaryContainer = RetroAmberDark,
        onPrimaryContainer = RetroAmberLight
    ),
    HOT_PINK(
        label = "Hot Pink",
        primary = HotPink,
        onPrimary = Color(0xFF330010),
        primaryContainer = HotPinkDark,
        onPrimaryContainer = HotPinkLight
    )
}

private fun buildDarkScheme(accent: AccentColor): ColorScheme = darkColorScheme(
    primary             = accent.primary,
    onPrimary           = accent.onPrimary,
    primaryContainer    = accent.primaryContainer,
    onPrimaryContainer  = accent.onPrimaryContainer,
    secondary           = accent.primaryContainer,
    onSecondary         = accent.onPrimary,
    background          = DarkBackground,
    onBackground        = DarkOnBackground,
    surface             = DarkSurface,
    onSurface           = DarkOnSurface,
    surfaceVariant      = DarkSurfaceVar,
    onSurfaceVariant    = DarkOnSurface.copy(alpha = 0.7f),
    error               = ErrorRed,
    onError             = Color.White
)

private fun buildLightScheme(accent: AccentColor): ColorScheme = lightColorScheme(
    primary             = accent.primary,
    onPrimary           = accent.onPrimary,
    primaryContainer    = accent.primaryContainer,
    onPrimaryContainer  = accent.onPrimaryContainer,
    secondary           = accent.primaryContainer,
    onSecondary         = accent.onPrimary,
    background          = LightBackground,
    onBackground        = LightOnBackground,
    surface             = LightSurface,
    onSurface           = LightOnSurface,
    surfaceVariant      = LightSurfaceVar,
    onSurfaceVariant    = LightOnSurface.copy(alpha = 0.7f),
    error               = ErrorRed,
    onError             = Color.White
)

@Composable
fun RetroArcadeTheme(
    darkTheme: Boolean = true,
    accent: AccentColor = AccentColor.NEON_GREEN,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) buildDarkScheme(accent) else buildLightScheme(accent)

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = RetroTypography,
        content     = content
    )
}
