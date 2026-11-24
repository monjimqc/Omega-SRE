package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF003640),
    primaryContainer = Color(0xFF004E5D),
    onPrimaryContainer = Color(0xFFBCEBFF),
    secondary = EmeraldReliable,
    onSecondary = Color(0xFF003918),
    secondaryContainer = Color(0xFF005326),
    onSecondaryContainer = Color(0xFF6CFFA0),
    tertiary = WarningAmber,
    background = ObsidianBg,
    onBackground = Color(0xFFE2E7F0),
    surface = ObsidianSurface,
    onSurface = Color(0xFFE2E7F0),
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = Color(0xFFC0CAD8),
    error = CriticalRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC2E8FF),
    onPrimaryContainer = Color(0xFF001E2B),
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB4F1F9),
    onSecondaryContainer = Color(0xFF002023),
    tertiary = LightTertiary,
    background = LightCanvasBg,
    onBackground = Color(0xFF191C20),
    surface = LightSurface,
    onSurface = Color(0xFF191C20),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF41484D),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted high-tech palette by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
