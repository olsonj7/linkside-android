package com.linkside.app.ui.teetimes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.linkside.app.data.api.CoursePhotoUtils
import com.linkside.app.data.model.Invite
import com.linkside.app.data.model.InviteStatus
import com.linkside.app.data.model.Photo
import com.linkside.app.data.model.PlayFormat
import com.linkside.app.data.model.ScoreboardRow
import com.linkside.app.data.model.ScoringEngine
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.TeeTimeScorecard
import com.linkside.app.data.model.User
import com.linkside.app.data.model.isWithinCourseConditionsWindow
import com.linkside.app.ui.components.ActionRow
import com.linkside.app.ui.components.CourseConditionsCard
import com.linkside.app.ui.components.CourseHeroPhoto
import com.linkside.app.ui.components.FormatChip
import com.linkside.app.ui.components.FullBadge
import com.linkside.app.ui.components.InviteDetailRow
import com.linkside.app.ui.components.LinkButton
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.ProfileAvatarView
import com.linkside.app.ui.components.RsvpButtonRow
import com.linkside.app.ui.components.SectionHeader
import com.linkside.app.ui.components.StatusPill
import com.linkside.app.ui.components.inviteStatusColors
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TeeTimeDetailScreen(
    teeTime: TeeTime,
    user: User,
    photos: List<Photo>,
    myScore: Int?,
    scorecards: List<TeeTimeScorecard> = emptyList(),
    courseWebsiteUrl: String? = null,
    isLoading: Boolean,
    isUploadingPhoto: Boolean,
    isSavingScore: Boolean,
    onBack: () -> Unit,
    onRsvp: (InviteStatus) -> Unit,
    onOpenChat: () -> Unit,
    onUploadPhoto: (ByteArray, String) -> Unit,
    onSaveScore: (Int) -> Unit,
    onEdit: (() -> Unit)? = null,
    onManageInvitees: (() -> Unit)? = null,
    onSendPendingInvites: (() -> Unit)? = null,
    onViewScorecards: (() -> Unit)? = null,
    onShareRound: (() -> Unit)? = null,
    onCancelTeeTime: (() -> Unit)? = null,
    onBumpInvite: (Invite) -> Unit = {},
    onToggleInviteAccess: (Invite) -> Unit = {},
    onSendLinksideInvite: (Invite) -> Unit = {},
    onRemoveInvite: (Invite) -> Unit = {},
    onManageResponse: (Invite, InviteStatus) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val myInvite = teeTime.myInvite(user)
    val isHost = teeTime.creatorId == user.id
    val isTripTeeTime = !teeTime.tripId.isNullOrBlank()
    val canEdit = isHost && !teeTime.isPast() && onEdit != null
    val canManageInvitees = isHost && onManageInvitees != null && (!teeTime.isPast() || isTripTeeTime)
    // Host can act on each invitee (bump / delegate / re-invite / remove) on standalone tee times.
    val canActOnInvites = isHost && !isTripTeeTime && !teeTime.isPast()
    var showCancelConfirm by remember { mutableStateOf(false) }
    val sortedInvites = teeTime.invites.sortedBy { inviteSortOrder(it.inviteStatus) }
    val photoUrl = CoursePhotoUtils.photoUrl(teeTime.courseId, teeTime.courseName)
    var scoreInput by remember(teeTime.id, myScore) { mutableStateOf(myScore?.toString().orEmpty()) }
    val scoreboardRows = remember(scorecards, teeTime.playFormat, teeTime.holesCount, teeTime.teamName) {
        ScoringEngine.leaderboard(
            scorecards = scorecards,
            playFormat = teeTime.playFormat,
            holes = teeTime.holesCount ?: 18,
            teamName = teeTime.teamName,
        )
    }
    val isTeamFormat = teeTime.playFormat == PlayFormat.SCRAMBLE.raw ||
        teeTime.playFormat == PlayFormat.BEST_BALL.raw
    val scoresTitle = when {
        isTeamFormat -> PlayFormat.entries.firstOrNull { it.raw == teeTime.playFormat }?.displayName ?: "Scores"
        else -> "Round Scores"
    }

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
            LinksideTopAppBar(
                onBack = onBack,
                actions = {
                    if (canEdit) {
                        IconButton(onClick = onEdit!!) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit tee time",
                                tint = LinksideColors.AccentLabel,
                            )
                        }
                    }
                },
            )
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
                    teeTime.roundName?.takeIf { it.isNotBlank() }?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = LinksideColors.AccentLabel,
                        )
                    }
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
                        if (teeTime.isFull) FullBadge()
                    }
                    Text(teeTime.formattedDate(), color = LinksideColors.TextSecondary)
                    val bookingUrl = teeTime.bookingUrl?.takeIf { it.isNotBlank() }
                    if (bookingUrl != null || !courseWebsiteUrl.isNullOrBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            bookingUrl?.let { url ->
                                Row(
                                    modifier = Modifier.clickable {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    },
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Default.EventAvailable,
                                        contentDescription = null,
                                        tint = LinksideColors.AccentLabel,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = "Book This Tee Time",
                                        color = LinksideColors.AccentLabel,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            }
                            courseWebsiteUrl?.takeIf { it.isNotBlank() }?.let { url ->
                                Row(
                                    modifier = Modifier.clickable {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    },
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Default.Language,
                                        contentDescription = null,
                                        tint = LinksideColors.AccentLabel,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = "Course Website",
                                        color = LinksideColors.AccentLabel,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "${teeTime.holesCount ?: 18} holes",
                        color = LinksideColors.TextSecondary,
                    )
                    teeTime.playFormat?.takeIf { it.isNotBlank() }?.let { format ->
                        FormatChip(text = format.replace('_', ' ').replaceFirstChar { it.uppercase() })
                    }
                    teeTime.greenFee?.let { fee ->
                        Text(
                            text = "Green fee: $${fee.toInt()}",
                            color = LinksideColors.TextSecondary,
                        )
                    }
                    Text(
                        text = if (isTripTeeTime) {
                            "${teeTime.invites.size} of ${teeTime.golfersNeeded} assigned"
                        } else {
                            "${teeTime.yesCount} of ${teeTime.golfersNeeded} confirmed"
                        },
                        color = LinksideColors.AccentLabel,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            item { Divider(color = LinksideColors.Muted) }

            if (teeTime.isWithinCourseConditionsWindow()) {
                item {
                    CourseConditionsCard(
                        teeTime = teeTime,
                        isSilver = user.isSilver,
                    )
                }
            }

            // Standalone tee times only — trip tee times are assignment-based, not RSVP.
            if (!isTripTeeTime && !isHost && myInvite != null && myInvite.isHost != true) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("YOUR RSVP", style = MaterialTheme.typography.labelSmall, color = LinksideColors.TextSecondary)
                        RsvpButtonRow(selected = myInvite.inviteStatus, enabled = !isLoading, onSelect = onRsvp)
                    }
                }
            }

            item {
                SectionHeader(
                    title = if (isTripTeeTime) "ASSIGNED" else "INVITES",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (isHost && !isTripTeeTime && !teeTime.isPast() && teeTime.hasPendingInvites && onSendPendingInvites != null) {
                item {
                    ActionRow(
                        title = "Send Invites (${teeTime.pendingInvites.size})",
                        icon = Icons.AutoMirrored.Filled.Send,
                        onClick = onSendPendingInvites,
                    )
                }
            }

            if (canManageInvitees) {
                item {
                    LinkButton(title = "Manage Invitees", onClick = onManageInvitees!!)
                }
            }

            items(sortedInvites, key = { it.phone ?: it.userId ?: it.name }) { invite ->
                if (canActOnInvites && invite.isHost != true) {
                    HostInviteRow(
                        invite = invite,
                        onBump = { onBumpInvite(invite) },
                        onToggleInviteAccess = { onToggleInviteAccess(invite) },
                        onSendLinksideInvite = { onSendLinksideInvite(invite) },
                        onRemove = { onRemoveInvite(invite) },
                    )
                } else {
                    InviteDetailRow(
                        name = invite.name,
                        status = invite.inviteStatus,
                        isHost = invite.isHost == true,
                        showStatusPill = !isTripTeeTime,
                    )
                }
            }

            item {
                ActionRow(title = "Group Chat", icon = Icons.Default.Chat, onClick = onOpenChat)
            }

            // Trip tee times skip personal score entry (standalone tee times only).
            if (!isTripTeeTime) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(title = "MY SCORE")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = scoreInput,
                                onValueChange = { scoreInput = it.filter { ch -> ch.isDigit() } },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Total strokes") },
                                singleLine = true,
                                enabled = !isSavingScore,
                            )
                            TextButton(
                                onClick = {
                                    scoreInput.toIntOrNull()?.let(onSaveScore)
                                },
                                enabled = !isSavingScore && scoreInput.toIntOrNull() != null,
                            ) {
                                Text(if (isSavingScore) "Saving…" else "Save", color = LinksideColors.AccentLabel)
                            }
                        }
                        if (myScore != null) {
                            Text("Saved score: $myScore", color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Shared scorecards from host/teammates (view-only).
            if (scoreboardRows.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoundScoresCard(title = scoresTitle, rows = scoreboardRows)
                        if (onViewScorecards != null) {
                            LinkButton(title = "View Full Scorecards", onClick = onViewScorecards)
                        }
                        if (onShareRound != null) {
                            ActionRow(
                                title = "Share Round / Player of the Day",
                                icon = Icons.Default.EmojiEvents,
                                onClick = onShareRound,
                            )
                        }
                    }
                }
            } else if (onViewScorecards != null && scorecards.any { it.playerName != "_specs_" }) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinkButton(title = "View Full Scorecards", onClick = onViewScorecards)
                        if (onShareRound != null) {
                            ActionRow(
                                title = "Share Round / Player of the Day",
                                icon = Icons.Default.EmojiEvents,
                                onClick = onShareRound,
                            )
                        }
                    }
                }
            } else if (onShareRound != null && teeTime.isPast()) {
                item {
                    ActionRow(
                        title = "Share Your Round",
                        icon = Icons.Default.EmojiEvents,
                        onClick = onShareRound,
                    )
                }
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
                item { Text("No photos yet.", color = LinksideColors.TextSecondary) }
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

            if (isHost && onCancelTeeTime != null && !isTripTeeTime) {
                item {
                    TextButton(
                        onClick = { showCancelConfirm = true },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Cancel Tee Time", color = LinksideColors.Danger, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Host-only: update a player's RSVP if they replied outside the app (mirrors iOS).
            if (isHost && !isTripTeeTime && !teeTime.isPast()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Manage Responses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = LinksideColors.TextPrimary,
                        )
                        Text(
                            "Update a player's status if they let you know outside of text.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LinksideColors.TextSecondary,
                        )
                        sortedInvites.filter { it.isHost != true }.forEach { invite ->
                            ManageResponseRow(
                                invite = invite,
                                enabled = !isLoading,
                                onSelect = { status -> onManageResponse(invite, status) },
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showCancelConfirm && onCancelTeeTime != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Cancel Tee Time?") },
            text = { Text("Invitees who said Yes will be notified. This can’t be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirm = false
                        onCancelTeeTime()
                    },
                ) { Text("Cancel Tee Time", color = LinksideColors.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) {
                    Text("Keep", color = LinksideColors.AccentLabel)
                }
            },
        )
    }
}

private fun inviteSortOrder(status: InviteStatus): Int = when (status) {
    InviteStatus.YES -> 0
    InviteStatus.MAYBE -> 1
    InviteStatus.WAITING -> 2
    InviteStatus.NO -> 3
}

@Composable
private fun ManageResponseRow(
    invite: Invite,
    enabled: Boolean,
    onSelect: (InviteStatus) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LinksideColors.Muted)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            invite.name,
            fontWeight = FontWeight.Medium,
            color = LinksideColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                InviteStatus.YES to "Yes",
                InviteStatus.NO to "No",
                InviteStatus.MAYBE to "Maybe",
            ).forEach { (status, label) ->
                val selected = invite.inviteStatus == status
                val bg = when {
                    !selected -> LinksideColors.Card
                    status == InviteStatus.YES -> LinksideColors.Success
                    status == InviteStatus.NO -> LinksideColors.Danger
                    else -> LinksideColors.GoldenBg
                }
                val fg = if (selected && status != InviteStatus.MAYBE) {
                    androidx.compose.ui.graphics.Color.White
                } else if (selected) {
                    LinksideColors.GoldenText
                } else {
                    LinksideColors.TextPrimary
                }
                Text(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bg)
                        .clickable(enabled = enabled) { onSelect(status) }
                        .padding(vertical = 10.dp),
                    color = fg,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

/**
 * Host-facing invite row: avatar, name, Linkside status, RSVP pill and a "…" menu
 * for bump / invite-delegation / re-invite / remove — mirrors iOS TeeTimeDetailView.
 */
@Composable
private fun HostInviteRow(
    invite: Invite,
    onBump: () -> Unit,
    onToggleInviteAccess: () -> Unit,
    onSendLinksideInvite: () -> Unit,
    onRemove: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf(false) }
    val isLinksideUser = !invite.userId.isNullOrBlank()
    val canInvite = invite.canInvite == true
    val (statusBg, statusFg) = inviteStatusColors(invite.inviteStatus)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Card)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatarView(name = invite.name, size = 40.dp)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(invite.name, fontWeight = FontWeight.Medium, color = LinksideColors.TextPrimary)
                if (canInvite) {
                    StatusPill(
                        text = "CAN INVITE",
                        background = LinksideColors.AccentChipBackground,
                        textColor = LinksideColors.AccentLabel,
                    )
                }
            }
            Text(
                text = if (isLinksideUser) "Linkside User" else "Not on Linkside",
                style = MaterialTheme.typography.labelSmall,
                color = if (isLinksideUser) LinksideColors.AccentLabel else LinksideColors.TextSecondary,
            )
        }
        StatusPill(
            text = invite.inviteStatus.raw.uppercase(),
            background = statusBg,
            textColor = statusFg,
        )
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Golfer options",
                    tint = LinksideColors.TextSecondary,
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Bump for status") },
                    onClick = {
                        menuOpen = false
                        onBump()
                    },
                )
                if (isLinksideUser) {
                    DropdownMenuItem(
                        text = { Text(if (canInvite) "Revoke invite access" else "Allow to invite others") },
                        onClick = {
                            menuOpen = false
                            onToggleInviteAccess()
                        },
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Send invite to Linkside") },
                        onClick = {
                            menuOpen = false
                            onSendLinksideInvite()
                        },
                    )
                }
                DropdownMenuItem(
                    text = { Text("Remove from tee time", color = LinksideColors.Danger) },
                    onClick = {
                        menuOpen = false
                        confirmRemove = true
                    },
                )
            }
        }
    }

    if (confirmRemove) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remove ${invite.name}?") },
            text = { Text("They’ll be notified that they’ve been removed from this tee time.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRemove = false
                        onRemove()
                    },
                ) { Text("Remove", color = LinksideColors.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = false }) {
                    Text("Keep", color = LinksideColors.AccentLabel)
                }
            },
        )
    }
}

@Composable
private fun RoundScoresCard(
    title: String,
    rows: List<ScoreboardRow>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LinksideColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    Divider(
                        color = LinksideColors.Muted,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(rankBackground(index)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LinksideColors.TextPrimary,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            row.name,
                            fontWeight = FontWeight.SemiBold,
                            color = LinksideColors.TextPrimary,
                        )
                        Text(
                            "${row.holesPlayed} holes",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinksideColors.TextSecondary,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${row.total}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LinksideColors.AccentLabel,
                        )
                        row.toParLabel?.let { label ->
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                color = LinksideColors.TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rankBackground(index: Int): Color = when (index) {
    0 -> Color(0xFFD4A017).copy(alpha = 0.25f)
    1 -> Color.Gray.copy(alpha = 0.18f)
    2 -> LinksideColors.Terracotta.copy(alpha = 0.18f)
    else -> LinksideColors.Muted.copy(alpha = 0.4f)
}
