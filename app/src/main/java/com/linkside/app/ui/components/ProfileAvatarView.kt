package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.linkside.app.ui.theme.LinksideColors
import kotlin.math.abs

@Composable
fun ProfileAvatarView(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    remoteUrl: String? = null,
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "?" }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarColor(name)),
        contentAlignment = Alignment.Center,
    ) {
        if (!remoteUrl.isNullOrBlank()) {
            AsyncImage(
                model = remoteUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = initials,
                color = LinksideColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value * 0.35f).sp,
            )
        }
    }
}

private fun avatarColor(name: String): Color {
    val hash = abs(name.hashCode())
    val hues = listOf(
        0xFF5B7FA6,
        0xFF7A5B9E,
        0xFF5B9E7A,
        0xFF9E7A5B,
        0xFF9E5B6A,
        0xFF6A5B9E,
    )
    return Color(hues[hash % hues.size])
}
