package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun FormatChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(LinksideColors.Accent.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = LinksideColors.AccentLabel,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
fun FullBadge(modifier: Modifier = Modifier) {
    FormatChip(text = "FULL", modifier = modifier)
}

@Composable
fun HostingBadge(modifier: Modifier = Modifier) {
    Text(
        text = "Hosting",
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(LinksideColors.Accent.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = LinksideColors.AccentLabel,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        softWrap = false,
    )
}
