package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun CourseHeroPhoto(
    url: String?,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 180.dp,
) {
    if (url.isNullOrBlank()) return
    SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp)),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(LinksideColors.Muted),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = LinksideColors.Accent, strokeWidth = 2.dp)
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(LinksideColors.Muted),
            )
        },
    )
}

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
    )
}
