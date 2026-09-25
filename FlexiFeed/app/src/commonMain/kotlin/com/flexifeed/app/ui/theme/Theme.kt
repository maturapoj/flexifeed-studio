package com.flexifeed.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = IndigoDark,
    onPrimaryContainer = IndigoAccent,
    secondary = CoralSale,
    onSecondary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = IndigoDark,
    secondary = CoralSale,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
)

@Composable
fun FlexiFeedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    primaryColor: Color? = null,
    accentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val effectivePrimary = primaryColor ?: baseScheme.primary
    val effectiveSecondary = accentColor ?: baseScheme.secondary

    // Calculate luminance for contrast: 0.299*R + 0.587*G + 0.114*B
    val primaryLum = 0.299f * effectivePrimary.red + 0.587f * effectivePrimary.green + 0.114f * effectivePrimary.blue
    val secondaryLum = 0.299f * effectiveSecondary.red + 0.587f * effectiveSecondary.green + 0.114f * effectiveSecondary.blue

    val onPrimaryColor = if (primaryLum > 0.55f) Color(0xFF0F172A) else Color.White
    val onSecondaryColor = if (secondaryLum > 0.55f) Color(0xFF0F172A) else Color.White

    val colorScheme = baseScheme.copy(
        primary = effectivePrimary,
        onPrimary = onPrimaryColor,
        secondary = effectiveSecondary,
        onSecondary = onSecondaryColor,
        primaryContainer = effectivePrimary.copy(alpha = if (darkTheme) 0.25f else 0.15f),
        onPrimaryContainer = if (darkTheme && primaryLum < 0.55f) effectivePrimary else onPrimaryColor
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
