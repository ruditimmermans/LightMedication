package com.light.medication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.light.medication.util.ThemePreferences

val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = Black,
    onPrimaryContainer = White,
    secondary = White,
    onSecondary = Black,
    secondaryContainer = Black,
    onSecondaryContainer = White,
    tertiary = White,
    onTertiary = Black,
    tertiaryContainer = Black,
    onTertiaryContainer = White,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = Black,
    onSurfaceVariant = White,
    error = White,
    onError = Black,
    errorContainer = Black,
    onErrorContainer = White,
    outline = White,
    outlineVariant = Gray,
    scrim = Black
)

val EInkColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = White,
    onPrimaryContainer = Black,
    secondary = Black,
    onSecondary = White,
    secondaryContainer = White,
    onSecondaryContainer = Black,
    tertiary = Black,
    onTertiary = White,
    tertiaryContainer = White,
    onTertiaryContainer = Black,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = White,
    onSurfaceVariant = Black,
    error = Black,
    onError = White,
    errorContainer = White,
    onErrorContainer = Black,
    outline = Black,
    outlineVariant = DarkGray,
    scrim = White
)

val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = White,
    onPrimaryContainer = Black,
    secondary = Black,
    onSecondary = White,
    secondaryContainer = White,
    onSecondaryContainer = Black,
    tertiary = Black,
    onTertiary = White,
    tertiaryContainer = White,
    onTertiaryContainer = Black,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = White,
    onSurfaceVariant = Black,
    error = Black,
    onError = White,
    errorContainer = White,
    onErrorContainer = Black,
    outline = DarkGray,
    outlineVariant = Gray,
    scrim = Black
)

@Composable
fun LIghtMedicationTheme(
    themeSetting: String = ThemePreferences.THEME_DARK,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeSetting) {
        ThemePreferences.THEME_EINK -> EInkColorScheme
        ThemePreferences.THEME_LIGHT -> LightColorScheme
        ThemePreferences.THEME_SYSTEM -> if (darkTheme) DarkColorScheme else EInkColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
