package com.grimoire.dnd.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    secondary = GoldMuted,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = TextDark,
    onBackground = TextParchment,
    onSurface = TextParchment
)

@Composable
fun Grimoire5ETheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
