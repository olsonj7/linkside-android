package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkside.app.data.model.User
import com.linkside.app.ui.theme.LinksideColors
import java.util.Calendar

@Composable
fun HomeProfileHeader(
    user: User,
    roundsThisYear: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0E3526), Color(0xFF184D3B)),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 200.dp, y = (-50).dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 250.dp, y = 60.dp)
                .background(Color.White.copy(alpha = 0.04f), CircleShape),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeOfDayGreeting(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                )
                user.tier?.let { tierBadge(it) }
                Spacer(modifier = Modifier.size(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    headerStat(
                        value = formatHandicap(user.handicap),
                        label = "HCP",
                    )
                    if (roundsThisYear > 0) {
                        val yearSuffix = Calendar.getInstance().get(Calendar.YEAR).toString().takeLast(2)
                        headerStat(value = roundsThisYear.toString(), label = "Rounds '$yearSuffix")
                    }
                }
            }
            ProfileAvatarView(
                name = user.displayName,
                remoteUrl = user.avatarUrl,
                size = 56.dp,
            )
        }
    }
}

@Composable
private fun tierBadge(tier: String) {
    // Bronze is the default first-launch tier, so it gets no badge.
    val (label, color, icon) = when (tier) {
        "gold" -> Triple("Linkside Events", Color(0xFFFFD700), Icons.Default.EmojiEvents)
        "silver" -> Triple("Linkside Plus", Color(0xFFC0C0C0), Icons.Default.Star)
        else -> return
    }
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun headerStat(value: String, label: String) {
    Column(
        modifier = Modifier
            .defaultMinSize(minWidth = 52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

private fun formatHandicap(handicap: Double?): String {
    if (handicap == null) return "—"
    if (handicap == 0.0) return "0.0"
    return String.format("%.1f", handicap)
}

private fun timeOfDayGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning ⛳"
        in 12..16 -> "Good afternoon ⛳"
        else -> "Good evening ⛳"
    }
}
