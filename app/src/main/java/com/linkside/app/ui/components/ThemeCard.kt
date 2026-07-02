package com.linkside.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun Modifier.themeCardShape(cornerRadius: androidx.compose.ui.unit.Dp = 14.dp): Modifier {
    val border = LinksideColors.CardBorder
    return this
        .clip(RoundedCornerShape(cornerRadius))
        .then(
            if (border.alpha > 0f) {
                Modifier.border(1.dp, border, RoundedCornerShape(cornerRadius))
            } else {
                Modifier
            },
        )
}
