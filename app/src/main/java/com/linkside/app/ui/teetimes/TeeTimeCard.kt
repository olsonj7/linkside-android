package com.linkside.app.ui.teetimes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.linkside.app.data.api.CoursePhotoUtils
import com.linkside.app.data.model.Invite
import com.linkside.app.data.model.InviteStatus
import com.linkside.app.data.model.TeeTime
import com.linkside.app.ui.components.CoursePhotoThumbnail
import com.linkside.app.ui.components.HostingBadge
import com.linkside.app.ui.components.themeCardShape
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeeTimeCard(
    teeTime: TeeTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHosting: Boolean = false,
) {
    val hasBorder = LinksideColors.CardBorder.alpha > 0f
    val photoUrl = CoursePhotoUtils.photoUrl(teeTime.courseId, teeTime.courseName)
    val isTripTeeTime = !teeTime.tripId.isNullOrBlank()
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .themeCardShape(20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hasBorder) 0.dp else 2.dp),
    ) {
        BoxWithGradientAccent {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        teeTime.roundName?.takeIf { it.isNotBlank() }?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LinksideColors.AccentLabel,
                                modifier = Modifier.padding(bottom = 2.dp),
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = teeTime.courseName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LinksideColors.TextPrimary,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (isHosting) HostingBadge()
                        }
                        Text(
                            text = teeTime.formattedDate(),
                            color = LinksideColors.TextSecondary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = "${teeTime.holesCount ?: 18} holes",
                            style = MaterialTheme.typography.bodySmall,
                            color = LinksideColors.TextSecondary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        CoursePhotoThumbnail(url = photoUrl)
                        Text(
                            text = if (isTripTeeTime) {
                                "${teeTime.invites.size} of ${teeTime.golfersNeeded}"
                            } else {
                                "${teeTime.yesCount} of ${teeTime.golfersNeeded}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = LinksideColors.AccentLabel,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isTripTeeTime) {
                        // Trip tee times are assignments from trip participants — no RSVP status rows.
                        StatusRow(
                            text = "${teeTime.invites.size} Assigned",
                            background = LinksideColors.AccentChipBackground,
                            textColor = LinksideColors.AccentLabel,
                            names = teeTime.invites.map { inviteLabel(it) },
                        )
                    } else {
                        if (teeTime.isFull) {
                            StatusRow(
                                text = "FULL",
                                background = LinksideColors.AccentChipBackground,
                                textColor = LinksideColors.AccentLabel,
                            )
                        }
                        if (teeTime.yesCount > 0) {
                            StatusRow(
                                text = "${teeTime.yesCount} Yes",
                                background = LinksideColors.AccentChipBackground,
                                textColor = LinksideColors.AccentLabel,
                                names = teeTime.invites.filter { it.inviteStatus == InviteStatus.YES }.map { inviteLabel(it) },
                            )
                        }
                        if (teeTime.maybeCount > 0) {
                            StatusRow(
                                text = "${teeTime.maybeCount} Maybe",
                                background = LinksideColors.GoldenBg,
                                textColor = LinksideColors.GoldenText,
                                names = teeTime.invites.filter { it.inviteStatus == InviteStatus.MAYBE }.map { inviteLabel(it) },
                            )
                        }
                        if (teeTime.noCount > 0) {
                            StatusRow(
                                text = "${teeTime.noCount} No",
                                background = LinksideColors.Danger.copy(alpha = 0.15f),
                                textColor = LinksideColors.Danger,
                                names = teeTime.invites.filter { it.inviteStatus == InviteStatus.NO }.map { inviteLabel(it) },
                            )
                        }
                        if (teeTime.waitingCount > 0) {
                            StatusRow(
                                text = "${teeTime.waitingCount} Waiting",
                                background = LinksideColors.Terracotta.copy(alpha = 0.15f),
                                textColor = LinksideColors.Terracotta,
                                names = teeTime.invites.filter { it.inviteStatus == InviteStatus.WAITING }.map { inviteLabel(it) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxWithGradientAccent(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LinksideColors.Accent.copy(alpha = 0.10f),
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = 120f,
                ),
            ),
    ) {
        content()
    }
}

@Composable
private fun StatusRow(
    text: String,
    background: Color,
    textColor: Color,
    names: List<String> = emptyList(),
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(background)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
        if (names.isNotEmpty()) {
            Text(
                text = names.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = LinksideColors.TextPrimary,
                maxLines = 1,
            )
        }
    }
}

private fun inviteLabel(invite: Invite): String =
    if (invite.isHost == true) "${invite.name} (Host)" else invite.name
