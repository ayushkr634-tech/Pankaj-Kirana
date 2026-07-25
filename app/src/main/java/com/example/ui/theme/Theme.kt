package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PastelOrganicGreen,
    secondary = PastelWarmAmber,
    tertiary = PastelWarmAmber,
    background = DarkForestBg,
    surface = DarkCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = LightGrey,
    onSurface = LightGrey,
    outline = DarkOutline,
    secondaryContainer = DarkOutline,
    onSecondaryContainer = PastelOrganicGreen
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPrimary,
    secondary = HighDensitySecondary,
    tertiary = HighDensityTertiary,
    background = HighDensityBg,
    surface = HighDensitySurface,
    onPrimary = Color.White,
    onSecondary = HighDensityText,
    onTertiary = HighDensityPrimary,
    onBackground = HighDensityText,
    onSurface = HighDensityText,
    outline = HighDensityOutline,
    secondaryContainer = HighDensityTertiary,
    onSecondaryContainer = HighDensityPrimary
)

@Composable
fun MyApplicationTheme(
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
