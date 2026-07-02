package com.linkside.app.ui.teetimes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.api.CoursePhotoUtils
import com.linkside.app.data.model.Invite
import com.linkside.app.data.model.InviteStatus
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.User
import com.linkside.app.ui.components.ActionRow
import com.linkside.app.ui.components.CourseHeroPhoto
import com.linkside.app.ui.components.FormatChip
import com.linkside.app.ui.components.FullBadge
import com.linkside.app.ui.components.InviteDetailRow
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.RsvpButtonRow
import com.linkside.app.ui.components.SectionHeader
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeeTimeDetailScreen(
    teeTime: TeeTime,
    user: User,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRsvp: (InviteStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val myInvite = teeTime.myInvite(user)
    val isHost = teeTime.creatorId == user.id
    val sortedInvites = teeTime.invites.sortedBy { inviteSortOrder(it.inviteStatus) }
    val photoUrl = CoursePhotoUtils.photoUrl(teeTime.courseId, teeTime.courseName)

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(onBack = onBack)
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                CourseHeroPhoto(url = photoUrl)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = teeTime.courseName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = LinksideColors.TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        if (teeTime.isFull) {
                            FullBadge()
                        }
                    }
                    Text(teeTime.formattedDate(), color = LinksideColors.TextSecondary)
                    teeTime.playFormat?.takeIf { it.isNotBlank() }?.let { format ->
                        FormatChip(text = format.replaceFirstChar { it.uppercase() })
                    }
                    Text(
                        text = "${teeTime.yesCount} of ${teeTime.golfersNeeded} confirmed",
                        color = LinksideColors.AccentLabel,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            item { Divider(color = LinksideColors.Muted) }

            if (!isHost && myInvite != null && myInvite.isHost != true) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("YOUR RSVP", style = MaterialTheme.typography.labelSmall, color = LinksideColors.TextSecondary)
                        RsvpButtonRow(
                            selected = myInvite.inviteStatus,
                            enabled = !isLoading,
                            onSelect = onRsvp,
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionHeader(title = "INVITES", modifier = Modifier.weight(1f))
                }
            }

            items(sortedInvites, key = { it.phone ?: it.userId ?: it.name }) { invite ->
                InviteDetailRow(
                    name = invite.name,
                    status = invite.inviteStatus,
                    isHost = invite.isHost == true,
                )
            }

            item {
                ActionRow(
                    title = "Group Chat",
                    icon = Icons.Default.Chat,
                    onClick = { /* Week 2+ */ },
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

private fun inviteSortOrder(status: InviteStatus): Int = when (status) {
    InviteStatus.YES -> 0
    InviteStatus.MAYBE -> 1
    InviteStatus.WAITING -> 2
    InviteStatus.NO -> 3
}
