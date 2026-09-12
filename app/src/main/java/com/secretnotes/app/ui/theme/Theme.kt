package com.secretnotes.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = darkColorScheme(
    primary = Gold,
    onPrimary = Ink,
    secondary = GoldDim,
    background = Ink,
    onBackground = Parchment,
    surface = InkSoft,
    onSurface = Parchment,
    surfaceVariant = Steel,
    onSurfaceVariant = ParchmentMuted,
    error = Ember,
    outline = SafeStroke
)

@Composable
fun SecretNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme.copy(scrim = Color(0xCC070504)),
        typography = Typography,
        content = content
    )
}
