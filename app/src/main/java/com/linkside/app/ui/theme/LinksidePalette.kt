package com.linkside.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class LinksidePalette(
    val background: Color,
    val card: Color,
    val muted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentLabel: Color,
    val accentChipBackground: Color,
    val goldenBg: Color,
    val goldenText: Color,
    val gold: Color,
    val danger: Color,
    val terracotta: Color,
    val success: Color,
    val cardBorder: Color,
    val onGold: Color,
    val rainBlue: Color = Color(0xFF60A5FA),
)

val LinksideDark = LinksidePalette(
    background = Color(0xFF0A1F16),
    card = Color(0xFF122A20),
    muted = Color(0xFF1A3A2C),
    textPrimary = Color(0xFFF5F7F6),
    textSecondary = Color(0xFFA8C5B5),
    textTertiary = Color(0xFF6B8478),
    accent = Color(0xFF6EE7A8),
    accentLabel = Color(0xFF6EE7A8),
    accentChipBackground = Color(0x336EE7A8),
    goldenBg = Color(0x33E0B050),
    goldenText = Color(0xFFE0B050),
    gold = Color(0xFFE0B050),
    danger = Color(0xFFFF5A5F),
    terracotta = Color(0xFFC07050),
    success = Color(0xFF34D399),
    cardBorder = Color.Transparent,
    onGold = Color(0xFF0A1F16),
)

val LinksideLight = LinksidePalette(
    background = Color(0xFFF7FAF8),
    card = Color(0xFFFFFFFF),
    muted = Color(0xFFE8F0EC),
    textPrimary = Color(0xFF102018),
    textSecondary = Color(0xFF3A6050),
    textTertiary = Color(0xFF567567),
    accent = Color(0xFF6EE7A8),
    accentLabel = Color(0xFF1A6B50),
    accentChipBackground = Color(0x476EE7A8),
    goldenBg = Color(0x40E0B050),
    goldenText = Color(0xFF9A6B10),
    gold = Color(0xFFE0B050),
    danger = Color(0xFFFF5A5F),
    terracotta = Color(0xFFC07050),
    success = Color(0xFF166534),
    cardBorder = Color(0xFFD1DDD6),
    onGold = Color(0xFF0A1F16),
)

val LocalLinksidePalette = staticCompositionLocalOf { LinksideDark }

@Composable
fun ProvideLinksidePalette(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLinksidePalette provides if (darkTheme) LinksideDark else LinksideLight,
        content = content,
    )
}

@Composable
fun linksidePalette(): LinksidePalette = LocalLinksidePalette.current
