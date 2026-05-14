package com.light.medication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BlackAndWhiteColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Gray,
    onSecondary = White,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = DarkGray,
    onSurfaceVariant = White,
    outline = Gray
)

@Composable
fun LIghtMedicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlackAndWhiteColorScheme,
        typography = Typography,
        content = content
    )
}
