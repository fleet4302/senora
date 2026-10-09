package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SonoraRed,
    onPrimary = Color.White,
    primaryContainer = SonoraDarkCard,
    onPrimaryContainer = SonoraRed,
    secondary = SonoraPink,
    onSecondary = Color.White,
    tertiary = SonoraCoral,
    background = SonoraDarkBackground,
    onBackground = SonoraDarkTextPrimary,
    surface = SonoraDarkSurface,
    onSurface = SonoraDarkTextPrimary,
    surfaceVariant = SonoraDarkCard,
    onSurfaceVariant = SonoraDarkTextSecondary,
    outline = SonoraDarkBorder,
    outlineVariant = Color(0xFF242426)
)

private val LightColorScheme = lightColorScheme(
    primary = SonoraRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECEF),
    onPrimaryContainer = SonoraRed,
    secondary = SonoraPink,
    onSecondary = Color.White,
    tertiary = SonoraCoral,
    background = SonoraLightBackground,
    onBackground = SonoraLightTextPrimary,
    surface = SonoraLightSurface,
    onSurface = SonoraLightTextPrimary,
    surfaceVariant = SonoraLightCard,
    onSurfaceVariant = SonoraLightTextSecondary,
    outline = SonoraLightBorder,
    outlineVariant = Color(0xFFEBEBF0)
)

@Composable
fun SonoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SonoraTheme(darkTheme = darkTheme, content = content)
}
