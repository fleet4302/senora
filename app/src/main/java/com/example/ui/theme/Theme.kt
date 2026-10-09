package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SonoraWhite,
    onPrimary = SonoraBlack,
    primaryContainer = SonoraDarkCard,
    onPrimaryContainer = SonoraWhite,
    secondary = SonoraZinc300,
    onSecondary = SonoraBlack,
    tertiary = SonoraZinc400,
    background = SonoraDarkBackground,
    onBackground = SonoraDarkTextPrimary,
    surface = SonoraDarkSurface,
    onSurface = SonoraDarkTextPrimary,
    surfaceVariant = SonoraDarkCard,
    onSurfaceVariant = SonoraDarkTextSecondary,
    outline = SonoraDarkBorder,
    outlineVariant = SonoraZinc800
)

private val LightColorScheme = lightColorScheme(
    primary = SonoraBlack,
    onPrimary = SonoraWhite,
    primaryContainer = SonoraZinc100,
    onPrimaryContainer = SonoraBlack,
    secondary = SonoraZinc700,
    onSecondary = SonoraWhite,
    tertiary = SonoraZinc600,
    background = SonoraLightBackground,
    onBackground = SonoraLightTextPrimary,
    surface = SonoraLightSurface,
    onSurface = SonoraLightTextPrimary,
    surfaceVariant = SonoraLightCard,
    onSurfaceVariant = SonoraLightTextSecondary,
    outline = SonoraLightBorder,
    outlineVariant = SonoraZinc200
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
