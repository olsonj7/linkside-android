package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    accentColor: Color = LinksideColors.Accent,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .background(accentColor, RoundedCornerShape(50)),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            ),
            color = LinksideColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    color = LinksideColors.AccentLabel,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun LinksideWordmark(
    modifier: Modifier = Modifier,
    fontSize: Int = 20,
    color: Color = LinksideColors.TextPrimary,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        text = "linkside",
        modifier = modifier.fillMaxWidth(),
        textAlign = textAlign,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}
