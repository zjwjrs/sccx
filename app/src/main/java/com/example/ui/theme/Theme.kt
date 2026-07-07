package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PinkPrimary,
    secondary = PinkSecondary,
    tertiary = PinkTertiary,
    background = SoftWhite,
    surface = SurfaceWhite,
    onPrimary = BackgroundWhite,
    onSecondary = BackgroundWhite,
    onTertiary = BackgroundWhite,
    onBackground = TextDarkGray,
    onSurface = TextDarkGray
)

private val LightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkSecondary,
    tertiary = PinkTertiary,
    background = SoftWhite,
    surface = SurfaceWhite,
    onPrimary = BackgroundWhite,
    onSecondary = BackgroundWhite,
    onTertiary = BackgroundWhite,
    onBackground = TextDarkGray,
    onSurface = TextDarkGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false to ensure the app always keeps the romantic Pink & White theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
