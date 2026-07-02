package com.linkside.app.ui.trips

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.linkside.app.data.model.User
import com.linkside.app.ui.components.AccentPrimaryButton
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
    isLoading: Boolean,
    isUploadingPhoto: Boolean,
    onBack: () -> Unit,
    onRsvp: (InviteStatus) -> Unit,
    onToggleDeposit: (paid: Boolean) -> Unit,
    onToggleBalance: (paid: Boolean) -> Unit,
    onOpenChat: () -> Unit,
    onTeeTimeClick: (String) -> Unit,
    onUploadPhoto: (ByteArray, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val myInvite = trip.myInvite(user)
    val isHost = trip.isHost(user)
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
                SectionHeader(title = "GOLFERS")
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    trip.invites.filter { it.inviteStatus == InviteStatus.YES }.forEach { invite ->
                        ProfileAvatarView(
                            name = invite.name,
                            size = 40.dp,
                            modifier = Modifier.offset(x = 0.dp),
                        )
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
                        trip.invites.filter { it.inviteStatus == InviteStatus.YES }.forEach { invite ->
                            DepositRow(
                                invite = invite,
                                isSelf = invite.matchesUser(user),
                                enabled = !isLoading && invite.matchesUser(user) && !isHost,
                                onToggle = onToggleDeposit,
                            )
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

            item {
                AccentPrimaryButton(
                    title = "Add to Calendar",
                    icon = Icons.Default.CalendarMonth,
                    onClick = { /* calendar intent in follow-up */ },
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DepositRow(
    invite: Invite,
    isSelf: Boolean,
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
        )
    }
}
