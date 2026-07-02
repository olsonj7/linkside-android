package com.linkside.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware colors. Always read from composables — values change in light vs dark mode.
 */
object LinksideColors {
    private val p: LinksidePalette
        @Composable get() = linksidePalette()

    val Primary: Color @Composable get() = p.background
    val Card: Color @Composable get() = p.card
    val Muted: Color @Composable get() = p.muted
    val TextPrimary: Color @Composable get() = p.textPrimary
    val TextSecondary: Color @Composable get() = p.textSecondary
    val TextTertiary: Color @Composable get() = p.textTertiary
    val Accent: Color @Composable get() = p.accent
    val AccentLabel: Color @Composable get() = p.accentLabel
    val AccentLabelLight: Color @Composable get() = LinksideLight.accentLabel
    val AccentChipBackground: Color @Composable get() = p.accentChipBackground
    val GoldenBg: Color @Composable get() = p.goldenBg
    val GoldenText: Color @Composable get() = p.goldenText
    val Gold: Color @Composable get() = p.gold
    val OnGold: Color @Composable get() = p.onGold
    val Danger: Color @Composable get() = p.danger
    val Terracotta: Color @Composable get() = p.terracotta
    val Success: Color @Composable get() = p.success
    val CardBorder: Color @Composable get() = p.cardBorder
    val RainBlue: Color @Composable get() = p.rainBlue
}
