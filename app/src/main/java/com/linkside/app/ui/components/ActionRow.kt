package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun ActionRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Accent.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = LinksideColors.AccentLabel)
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            color = LinksideColors.AccentLabel,
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LinksideColors.TextTertiary,
        )
    }
}

@Composable
fun LinkButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.clickable(onClick = onClick),
        color = LinksideColors.AccentLabel,
        fontWeight = FontWeight.Medium,
        style = MaterialTheme.typography.bodyMedium,
    )
}
