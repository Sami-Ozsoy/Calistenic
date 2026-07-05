package com.example.calistenic.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = Color(0xFF0A3D1F),
    secondary = GreenSecondary,
    onSecondary = Color.White,
    tertiary = GreenTertiary,
    background = LightBackground,
    onBackground = Color(0xFF14180F),
    surface = LightSurface,
    onSurface = Color(0xFF14180F),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError,
    outline = Color(0xFF7A8A7C)
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = Color(0xFF0A3D1F),
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = Color(0xFFD8F5DD),
    secondary = Color(0xFFB6C7B8),
    onSecondary = Color(0xFF22301F),
    tertiary = Color(0xFF8ADBA0),
    background = DarkBackground,
    onBackground = Color(0xFFE8EFE9),
    surface = DarkSurface,
    onSurface = Color(0xFFE8EFE9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    outline = Color(0xFF6B7A6D)
)

@Composable
fun CalistenicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.surface.toArgb()
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}