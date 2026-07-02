package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.InviteStatus
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun RsvpButtonRow(
    selected: InviteStatus?,
    enabled: Boolean,
    onSelect: (InviteStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LinksideColors.Card)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            InviteStatus.YES to "In",
            InviteStatus.MAYBE to "Maybe",
            InviteStatus.NO to "Out",
        ).forEach { (status, label) ->
            val isSelected = selected == status
            Text(
                text = label,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) LinksideColors.AccentChipBackground else LinksideColors.Muted,
                    )
                    .clickable(enabled = enabled) { onSelect(status) }
                    .padding(vertical = 12.dp),
                color = if (isSelected) LinksideColors.AccentLabel else LinksideColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    background: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = textColor,
        fontWeight = FontWeight.SemiBold,
        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
    )
}

@Composable
fun inviteStatusColors(status: InviteStatus): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> =
    when (status) {
        InviteStatus.YES -> LinksideColors.AccentChipBackground to LinksideColors.AccentLabel
        InviteStatus.MAYBE -> LinksideColors.GoldenBg to LinksideColors.GoldenText
        InviteStatus.WAITING -> LinksideColors.Terracotta.copy(alpha = 0.15f) to LinksideColors.Terracotta
        InviteStatus.NO -> LinksideColors.Danger.copy(alpha = 0.15f) to LinksideColors.Danger
    }

@Composable
fun InviteDetailRow(
    name: String,
    status: InviteStatus,
    isHost: Boolean,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
) {
    val (bg, fg) = inviteStatusColors(status)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Card)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatarView(name = name, remoteUrl = avatarUrl, size = 40.dp)
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontWeight = FontWeight.Medium, color = LinksideColors.TextPrimary)
                if (isHost) {
                    StatusPill(
                        text = "HOST",
                        background = LinksideColors.AccentChipBackground,
                        textColor = LinksideColors.AccentLabel,
                    )
                }
            }
        }
        StatusPill(
            text = status.raw.uppercase(),
            background = bg,
            textColor = fg,
        )
    }
}
