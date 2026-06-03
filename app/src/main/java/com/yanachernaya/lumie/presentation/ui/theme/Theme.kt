package com.yanachernaya.lumie.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val ColorScheme.affirmationBackground: Color
    get() = Grey900

private val DarkColorScheme = darkColorScheme(
    primary = Lavender200,
    onPrimary = Grey900,
    secondary = Lavender200,
    onSecondary = White,
    primaryContainer = Lavender200,
    onPrimaryContainer = Grey900,
    surfaceVariant = WhiteAlpha10,
    onSurfaceVariant = Grey50,
    secondaryContainer = GreyAlpha20,
    onSecondaryContainer = White,
    background = Grey900,
    onBackground = Grey50,
    surface = Grey900,
    onSurface = Grey50,
    outline = WhiteAlpha20,
    outlineVariant = Grey500,
    error = Red
)

private val LightColorScheme = lightColorScheme(
    primary = Lavender400,
    onPrimary = White,
    secondary = Lavender400,
    onSecondary = Grey900,
    primaryContainer = Lavender400,
    onPrimaryContainer = White,
    surfaceVariant = WhiteAlpha10,
    onSurfaceVariant = Grey900,
    secondaryContainer = GreyAlpha20,
    onSecondaryContainer = White,
    background = Grey50,
    onBackground = Grey900,
    surface = White,
    onSurface = Grey900,
    outline = WhiteAlpha20,
    outlineVariant = Grey100,
    error = Red
)

val LocalAppDarkTheme = staticCompositionLocalOf { false }

@Composable
fun LumieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    CompositionLocalProvider(
        LocalAppDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}