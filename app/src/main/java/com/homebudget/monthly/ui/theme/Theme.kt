package com.homebudget.monthly.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Color(0xFFE8E7FF),
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = Color(0xFFB2F5F0),
    background = Background,
    surface = Surface,
    onBackground = OnBackground,
    onSurface = OnSurface,
    error = Error,
    onError = OnError,
    surfaceVariant = Color(0xFFF1F0FF),
    outline = Color(0xFFB0AFCF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8B83FF),
    onPrimary = Color(0xFF1A1A2E),
    primaryContainer = Color(0xFF4C46B5),
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = DarkCard
)

@Composable
fun HomeBudgetTheme(
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
