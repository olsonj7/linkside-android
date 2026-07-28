package com.linkside.app.ui.trips

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.linkside.app.data.api.CoursePhotoUtils
import com.linkside.app.data.model.GolfTrip
import com.linkside.app.data.model.Invite
import com.linkside.app.data.model.InviteStatus
import com.linkside.app.data.model.Photo
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.TripAnnouncement
import com.linkside.app.data.model.User
import com.linkside.app.ui.components.ActionRow
import com.linkside.app.ui.components.CourseHeroPhoto
import com.linkside.app.ui.components.HostingBadge
import com.linkside.app.ui.components.LinkButton
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.ProfileAvatarView
import com.linkside.app.ui.components.RsvpButtonRow
import com.linkside.app.ui.components.SectionHeader
import com.linkside.app.ui.components.StatusPill
import com.linkside.app.ui.teetimes.TeeTimeCard
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TripDetailScreen(
    trip: GolfTrip,
    user: User,
    teeTimes: List<TeeTime>,
    photos: List<Photo>,
    announcements: List<TripAnnouncement>,
    isLoading: Boolean,
    isUploadingPhoto: Boolean,
    isPostingAnnouncement: Boolean,
    onBack: () -> Unit,
    onRsvp: (InviteStatus) -> Unit,
    onToggleDeposit: (invite: Invite, paid: Boolean) -> Unit,
    onOpenChat: () -> Unit,
    onTeeTimeClick: (String) -> Unit,
    onUploadPhoto: (ByteArray, String) -> Unit,
    onPostAnnouncement: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val myInvite = trip.myInvite(user)
    val isHost = trip.isHost(user)
    var showPostAnnouncement by remember { mutableStateOf(false) }
    val photoUrl = CoursePhotoUtils.photoUrl(trip.resortPlaceId, trip.location)
    val progress = if (trip.golfersNeeded > 0) trip.yesCount.toFloat() / trip.golfersNeeded else 0f

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        context.contentResolver.openInputStream(uri)?.use { stream ->
            onUploadPhoto(stream.readBytes(), context.contentResolver.getType(uri) ?: "image/jpeg")
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(title = "Trip Details", onBack = onBack)
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
                CourseHeroPhoto(url = photoUrl, height = 200.dp)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = trip.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = LinksideColors.TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        if (isHost) HostingBadge()
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = LinksideColors.TextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(trip.location, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(trip.formattedDateRange(), color = LinksideColors.TextSecondary)
                    Text(
                        text = "${trip.yesCount} of ${trip.golfersNeeded} confirmed",
                        color = LinksideColors.AccentLabel,
                        fontWeight = FontWeight.SemiBold,
                    )
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(50)),
                        color = LinksideColors.Accent,
                        trackColor = LinksideColors.Muted,
                    )
                }
            }

            trip.formattedCost()?.let { cost ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LinksideColors.Card)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(LinksideColors.GoldenBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = LinksideColors.Gold)
                        }
                        Column {
                            Text("Trip Cost", color = LinksideColors.GoldenText, style = MaterialTheme.typography.labelSmall)
                            Text(cost, fontWeight = FontWeight.Bold, color = LinksideColors.TextPrimary)
                        }
                    }
                }
            }

            item {
                val attending = trip.invites.filter { it.inviteStatus == InviteStatus.YES }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(title = "GOLFERS")
                    if (attending.isEmpty()) {
                        Text(
                            "No golfers confirmed yet.",
                            color = LinksideColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LinksideColors.Card),
                        ) {
                            attending.forEach { invite ->
                                GolferRow(invite = invite, isYou = invite.matchesUser(user))
                            }
                        }
                    }
                }
            }

            if (trip.deposit != null && trip.deposit > 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(title = "DEPOSITS")
                        Text(
                            text = "$${trip.deposit.toInt()} per person",
                            color = LinksideColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (isHost) {
                            // Host manages everyone's deposit status.
                            trip.invites.filter { it.isHost != true }.forEach { invite ->
                                DepositRow(
                                    invite = invite,
                                    enabled = !isLoading,
                                    onToggle = { paid -> onToggleDeposit(invite, paid) },
                                )
                            }
                        } else {
                            // Attendees only get a toggle for their own deposit.
                            myInvite?.takeIf { it.isHost != true }?.let { invite ->
                                SelfDepositRow(
                                    paid = invite.depositPaid == true,
                                    enabled = !isLoading,
                                    onToggle = { paid -> onToggleDeposit(invite, paid) },
                                )
                            }
                        }
                    }
                }
            }

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

            if (announcements.isNotEmpty() || isHost) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionHeader(title = "ANNOUNCEMENTS")
                            if (isHost) {
                                LinkButton(title = "+ Post", onClick = { showPostAnnouncement = true })
                            }
                        }
                        if (announcements.isEmpty()) {
                            Text(
                                text = if (isHost) {
                                    "No announcements yet. Post an update to notify everyone on the trip."
                                } else {
                                    "No announcements yet."
                                },
                                color = LinksideColors.TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                announcements.forEach { announcement ->
                                    AnnouncementRow(announcement = announcement)
                                }
                            }
                        }
                    }
                }
            }

            item {
                ActionRow(title = "Trip Chat", icon = Icons.Default.Chat, onClick = onOpenChat)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Photos", fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                    LinkButton(
                        title = if (isUploadingPhoto) "Uploading…" else "+ Add Photo",
                        onClick = { if (!isUploadingPhoto) photoPicker.launch("image/*") },
                    )
                }
            }

            if (photos.isEmpty()) {
                item {
                    Text("No photos yet.", color = LinksideColors.TextSecondary)
                }
            } else {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        photos.forEach { photo ->
                            AsyncImage(
                                model = photo.url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth(0.48f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }

            if (teeTimes.isNotEmpty()) {
                item {
                    SectionHeader(title = "TRIP TEE TIMES")
                }
                items(teeTimes, key = { it.id }) { teeTime ->
                    TeeTimeCard(teeTime = teeTime, onClick = { onTeeTimeClick(teeTime.id) })
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showPostAnnouncement) {
        PostAnnouncementDialog(
            isPosting = isPostingAnnouncement,
            onDismiss = { showPostAnnouncement = false },
            onPost = { message ->
                onPostAnnouncement(message)
                showPostAnnouncement = false
            },
        )
    }
}

@Composable
private fun AnnouncementRow(announcement: TripAnnouncement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Card)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(LinksideColors.GoldenBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Campaign,
                contentDescription = null,
                tint = LinksideColors.Gold,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(announcement.message, color = LinksideColors.TextPrimary, style = MaterialTheme.typography.bodyMedium)
            Text(
                announcement.formattedDate(),
                color = LinksideColors.TextSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun PostAnnouncementDialog(
    isPosting: Boolean,
    onDismiss: () -> Unit,
    onPost: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!isPosting) onDismiss() },
        title = { Text("New Announcement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "This message is saved to the trip's announcement log and pushed to everyone on the trip.",
                    color = LinksideColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 1000) text = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Share an update…") },
                    minLines = 3,
                    maxLines = 6,
                    enabled = !isPosting,
                )
                Text(
                    "${text.length}/1000",
                    color = LinksideColors.TextTertiary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPost(text.trim()) },
                enabled = text.isNotBlank() && !isPosting,
            ) {
                Text(if (isPosting) "Sending…" else "Send", color = LinksideColors.AccentLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isPosting) {
                Text("Cancel", color = LinksideColors.TextSecondary)
            }
        },
    )
}

@Composable
private fun GolferRow(invite: Invite, isYou: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatarView(name = invite.name, size = 36.dp)
        Text(
            text = if (isYou) "${invite.name} (You)" else invite.name,
            modifier = Modifier.weight(1f),
            color = LinksideColors.TextPrimary,
            fontWeight = FontWeight.Medium,
        )
        if (invite.isHost == true) {
            Text(
                "Host",
                style = MaterialTheme.typography.labelSmall,
                color = LinksideColors.AccentLabel,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SelfDepositRow(
    paid: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LinksideColors.Card)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("My Deposit", modifier = Modifier.weight(1f), color = LinksideColors.TextPrimary)
        StatusPill(
            text = if (paid) "Paid" else "Unpaid",
            background = if (paid) LinksideColors.AccentChipBackground else LinksideColors.Muted,
            textColor = if (paid) LinksideColors.AccentLabel else LinksideColors.TextSecondary,
            modifier = Modifier.clickable(enabled = enabled) { onToggle(!paid) },
        )
    }
}

@Composable
private fun DepositRow(
    invite: Invite,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val paid = invite.depositPaid == true
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LinksideColors.Card)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatarView(name = invite.name, size = 36.dp)
        Text(invite.name, modifier = Modifier.weight(1f), color = LinksideColors.TextPrimary)
        StatusPill(
            text = if (paid) "Paid" else "Unpaid",
            background = if (paid) LinksideColors.AccentChipBackground else LinksideColors.Muted,
            textColor = if (paid) LinksideColors.AccentLabel else LinksideColors.TextSecondary,
            modifier = Modifier.clickable(enabled = enabled) { onToggle(!paid) },
        )
    }
}
