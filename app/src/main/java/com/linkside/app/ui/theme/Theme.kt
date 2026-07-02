package com.linkside.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private fun darkScheme() = darkColorScheme(
    primary = LinksideDark.accentLabel,
    onPrimary = LinksideDark.background,
    secondary = LinksideDark.gold,
    onSecondary = LinksideDark.onGold,
    background = LinksideDark.background,
    surface = LinksideDark.card,
    surfaceVariant = LinksideDark.muted,
    onBackground = LinksideDark.textPrimary,
    onSurface = LinksideDark.textPrimary,
    onSurfaceVariant = LinksideDark.textSecondary,
    outline = LinksideDark.cardBorder,
    error = LinksideDark.danger,
)

private fun lightScheme() = lightColorScheme(
    primary = LinksideLight.accentLabel,
    onPrimary = LinksideLight.card,
    secondary = LinksideLight.gold,
    onSecondary = LinksideLight.onGold,
    background = LinksideLight.background,
    surface = LinksideLight.card,
    surfaceVariant = LinksideLight.muted,
    onBackground = LinksideLight.textPrimary,
    onSurface = LinksideLight.textPrimary,
    onSurfaceVariant = LinksideLight.textSecondary,
    outline = LinksideLight.cardBorder,
    error = LinksideLight.danger,
)

@Composable
fun LinksideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    ProvideLinksidePalette(darkTheme = darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkScheme() else lightScheme(),
            content = content,
        )
    }
}
