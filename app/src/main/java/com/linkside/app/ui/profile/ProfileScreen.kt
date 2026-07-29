package com.linkside.app.ui.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.GolfCourse
import com.linkside.app.data.model.GolfTrip
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.Tournament
import com.linkside.app.data.model.User
import com.linkside.app.data.prefs.ProfilePreferences
import com.linkside.app.ui.components.LinksideWordmark
import com.linkside.app.ui.components.ProfileAvatarView
import com.linkside.app.ui.components.SectionHeader
import com.linkside.app.ui.theme.LinksideColors
import com.linkside.app.util.ImageCompression
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    profilePreferences: ProfilePreferences,
    courseSearchResults: List<GolfCourse>,
    isSearchingCourses: Boolean,
    isUploadingAvatar: Boolean = false,
    declinedTeeTimes: List<TeeTime> = emptyList(),
    declinedTrips: List<GolfTrip> = emptyList(),
    withdrawnTournaments: List<Tournament> = emptyList(),
    previousTeeTimes: List<TeeTime> = emptyList(),
    roundScores: Map<String, Int> = emptyMap(),
    onDarkModeChange: (Boolean) -> Unit,
    onEditProfile: () -> Unit,
    onSearchCourses: (String) -> Unit,
    onAddFavoriteCourse: (GolfCourse) -> Unit,
    onRemoveFavoriteCourse: (String) -> Unit,
    onUploadAvatar: (ByteArray, String) -> Unit,
    onDeleteAvatar: () -> Unit,
    onDeclinedTeeTimeClick: (String) -> Unit = {},
    onDeclinedTripClick: (String) -> Unit = {},
    onWithdrawnTournamentClick: (String) -> Unit = {},
    onPreviousTeeTimeClick: (String) -> Unit = {},
    onInviteContest: () -> Unit = {},
    onLinkEmail: () -> Unit = {},
    onLinkGoogle: () -> Unit = {},
    isLinkingGoogle: Boolean = false,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit = {},
    isDeletingAccount: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showAddFavorite by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeletingLocal by remember { mutableStateOf(false) }
    var defaultGroupSize by remember { mutableStateOf(profilePreferences.defaultGroupSize) }
    var smsEnabled by remember { mutableStateOf(profilePreferences.smsNotificationsEnabled) }
    var pushEnabled by remember { mutableStateOf(profilePreferences.pushNotificationsEnabled) }
    var darkModeEnabled by remember { mutableStateOf(profilePreferences.prefersDarkMode ?: true) }
    val favorites = user.favoriteCourses.orEmpty()
    val homeAddress = formatHomeAddress(user.address, user.city, user.state, user.zipCode)
    val primary = primaryAuthMethod(user)
    val hasAvatar = !user.avatarUrl.isNullOrBlank()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (raw == null) {
            Toast.makeText(context, "Couldn't read that photo.", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        val compressed = ImageCompression.compressForAvatar(raw)
        if (compressed == null) {
            Toast.makeText(context, "Couldn't process that image. Try a different photo.", Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }
        onUploadAvatar(compressed, "image/jpeg")
    }

    if (showAddFavorite) {
        AddFavoriteCourseSheet(
            courses = courseSearchResults,
            isSearching = isSearchingCourses,
            onDismiss = { showAddFavorite = false },
            onSearch = onSearchCourses,
            onSelect = { course ->
                onAddFavoriteCourse(course)
                showAddFavorite = false
            },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LinksideColors.Primary)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                LinksideWordmark(
                    fontSize = 20,
                    textAlign = TextAlign.Center,
                )
            }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ProfilePhotoButton(
                        name = user.displayName,
                        remoteUrl = user.avatarUrl,
                        hasAvatar = hasAvatar,
                        isUploading = isUploadingAvatar,
                        onPickPhoto = { if (!isUploadingAvatar) photoPicker.launch("image/*") },
                        onRemovePhoto = { if (!isUploadingAvatar) onDeleteAvatar() },
                    )
                    Text(
                        text = user.displayName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LinksideColors.TextPrimary,
                    )
                    EditProfileCircleButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit profile", tint = LinksideColors.OnGold, modifier = Modifier.size(18.dp))
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (homeAddress != null) {
                        ProfileInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Home",
                            value = homeAddress,
                        )
                    } else {
                        ProfilePlaceholderRow(
                            icon = Icons.Default.LocationOn,
                            text = "Add your home city",
                            onClick = onEditProfile,
                        )
                    }
                    user.handicap?.let { hcp ->
                        ProfileInfoRow(
                            icon = Icons.Default.GolfCourse,
                            label = "Handicap",
                            value = handicapLabel(hcp),
                        )
                    }
                }
            }

            item {
                InviteContestBanner(onClick = onInviteContest)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Favorite Courses",
                            fontWeight = FontWeight.Bold,
                            color = LinksideColors.TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        ProfileAddChip(label = "+ Add", onClick = { showAddFavorite = true })
                    }
                    if (favorites.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clipProfileCard()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.StarOutline, contentDescription = null, tint = LinksideColors.TextSecondary)
                            Text("No favorite courses yet", color = LinksideColors.TextSecondary)
                        }
                        Text(
                            "Tap Add to search and save your favorite courses.",
                            color = LinksideColors.TextTertiary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        favorites.forEach { course ->
                            FavoriteCourseRow(
                                name = course.name,
                                address = course.address,
                                onRemove = { onRemoveFavoriteCourse(course.placeId) },
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Previous Tee Times",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LinksideColors.TextPrimary,
                    )
                    if (previousTeeTimes.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(LinksideColors.Card)
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = LinksideColors.TextSecondary,
                            )
                            Text(
                                "No previous rounds yet",
                                color = LinksideColors.TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    } else {
                        previousTeeTimes.forEach { teeTime ->
                            PreviousTeeTimeRow(
                                teeTime = teeTime,
                                score = roundScores[teeTime.id],
                                onClick = { onPreviousTeeTimeClick(teeTime.id) },
                            )
                        }
                    }
                }
            }

            if (declinedTeeTimes.isNotEmpty() || declinedTrips.isNotEmpty()) {
                val now = Instant.now()

                item {
                    SectionHeader(title = "DECLINES", accentColor = LinksideColors.Accent)
                }

                if (declinedTeeTimes.isNotEmpty()) {
                    item { DeclinesSectionLabel("Tee Times") }
                    items(declinedTeeTimes, key = { "declined_tt_${it.id}" }) { teeTime ->
                        DeclinedTeeTimeRow(
                            teeTime = teeTime,
                            upcoming = teeTime.parsedInstant()?.isAfter(now) == true,
                            onClick = { onDeclinedTeeTimeClick(teeTime.id) },
                        )
                    }
                }

                if (declinedTrips.isNotEmpty()) {
                    item { DeclinesSectionLabel("Trips") }
                    items(declinedTrips, key = { "declined_trip_${it.id}" }) { trip ->
                        DeclinedTripRow(
                            trip = trip,
                            upcoming = trip.parsedEnd()?.isAfter(now) == true,
                            onClick = { onDeclinedTripClick(trip.id) },
                        )
                    }
                }
            }

            if (withdrawnTournaments.isNotEmpty()) {
                item {
                    SectionHeader(title = "WITHDRAWN TOURNAMENTS", accentColor = LinksideColors.Accent)
                }
                items(withdrawnTournaments, key = { "withdrawn_tourn_${it.id}" }) { tournament ->
                    WithdrawnTournamentRow(
                        tournament = tournament,
                        onClick = { onWithdrawnTournamentClick(tournament.id) },
                    )
                }
            }

            item {
                SectionHeader(title = "SIGN-IN METHOD", accentColor = LinksideColors.Accent)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SignInMethodCard(
                        title = primaryAuthTitle(primary),
                        subtitle = "Primary sign-in method",
                        icon = primaryAuthIcon(primary),
                        showCheck = true,
                    )
                    if (primary != PrimaryAuthMethod.EMAIL) {
                        if (!user.email.isNullOrBlank()) {
                            SignInMethodCard(
                                title = "Email & Password linked",
                                subtitle = user.email,
                                icon = Icons.Default.Email,
                                showCheck = false,
                            )
                        } else {
                            ProfilePlaceholderRow(
                                icon = Icons.Default.Email,
                                text = "Link Email & Password",
                                onClick = onLinkEmail,
                            )
                        }
                    }
                    if (primary != PrimaryAuthMethod.GOOGLE) {
                        if (!user.googleId.isNullOrBlank()) {
                            SignInMethodCard(
                                title = "Google Account linked",
                                subtitle = null,
                                icon = Icons.Default.Email,
                                showCheck = false,
                            )
                        } else {
                            ProfilePlaceholderRow(
                                icon = Icons.Default.Email,
                                text = if (isLinkingGoogle) "Linking Google…" else "Link Google Account",
                                onClick = { if (!isLinkingGoogle) onLinkGoogle() },
                            )
                        }
                    }
                    if (primary != PrimaryAuthMethod.PHONE && !user.phone.isNullOrBlank()) {
                        SignInMethodCard(
                            title = user.phone,
                            subtitle = "Phone linked",
                            icon = Icons.Default.Phone,
                            showCheck = false,
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "SETTINGS", accentColor = LinksideColors.Accent)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsStepperRow(
                        label = "Default Group Size",
                        icon = Icons.Default.Group,
                        value = defaultGroupSize,
                        onDecrement = {
                            if (defaultGroupSize > 2) {
                                defaultGroupSize -= 1
                                profilePreferences.defaultGroupSize = defaultGroupSize
                            }
                        },
                        onIncrement = {
                            if (defaultGroupSize < 8) {
                                defaultGroupSize += 1
                                profilePreferences.defaultGroupSize = defaultGroupSize
                            }
                        },
                    )
                    SettingsToggleRow(
                        label = "Dark Mode",
                        icon = Icons.Default.DarkMode,
                        checked = darkModeEnabled,
                        onCheckedChange = {
                            darkModeEnabled = it
                            profilePreferences.prefersDarkMode = it
                            onDarkModeChange(it)
                        },
                    )
                    SettingsToggleRow(
                        label = "SMS Updates",
                        icon = Icons.Default.Message,
                        checked = smsEnabled,
                        onCheckedChange = {
                            smsEnabled = it
                            profilePreferences.smsNotificationsEnabled = it
                        },
                    )
                    SettingsToggleRow(
                        label = "Push Notifications",
                        icon = Icons.Default.Notifications,
                        checked = pushEnabled,
                        onCheckedChange = {
                            pushEnabled = it
                            profilePreferences.pushNotificationsEnabled = it
                        },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clipProfileCard()
                            .clickable {
                                context.startActivity(
                                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@getlinkside.com")),
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = LinksideColors.TextPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Help & Support",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp),
                            color = LinksideColors.TextPrimary,
                        )
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = LinksideColors.TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            item {
                ProfileDestructiveRow(
                    label = "Sign Out",
                    icon = Icons.Default.ExitToApp,
                    onClick = onSignOut,
                )
            }

            item {
                ProfileDestructiveRow(
                    label = if (isDeletingAccount || isDeletingLocal) "Deleting…" else "Delete Account",
                    icon = Icons.Default.Delete,
                    onClick = { if (!isDeletingAccount && !isDeletingLocal) showDeleteConfirm = true },
                )
            }

            item { Spacer(modifier = Modifier.size(24.dp)) }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingLocal) showDeleteConfirm = false },
            title = { Text("Delete Account") },
            text = {
                Text("This will permanently delete your account and all associated data. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        isDeletingLocal = true
                        onDeleteAccount()
                    },
                    enabled = !isDeletingLocal,
                ) {
                    Text("Delete Account", color = LinksideColors.Danger)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    enabled = !isDeletingLocal,
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun InviteContestBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LinksideColors.Gold.copy(alpha = 0.18f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(LinksideColors.Gold.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = LinksideColors.Gold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Invite Contest",
                fontWeight = FontWeight.Bold,
                color = LinksideColors.TextPrimary,
            )
            Text(
                "Invite friends who join this month — win a golf prize.",
                style = MaterialTheme.typography.bodySmall,
                color = LinksideColors.TextSecondary,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LinksideColors.TextSecondary,
        )
    }
}

@Composable
private fun ProfilePhotoButton(
    name: String,
    remoteUrl: String?,
    hasAvatar: Boolean,
    isUploading: Boolean,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
    Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        ProfileAvatarView(
            name = name,
            remoteUrl = remoteUrl,
            size = 64.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .semantics { contentDescription = "Choose profile photo" }
                .clickable(enabled = !isUploading, onClick = onPickPhoto),
        )

        when {
            isUploading -> {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(LinksideColors.Card),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = LinksideColors.AccentLabel,
                    )
                }
            }
            hasAvatar -> {
                IconButton(
                    onClick = onRemovePhoto,
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove profile photo",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(LinksideColors.Primary)
                        .clickable(onClick = onPickPhoto),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviousTeeTimeRow(
    teeTime: TeeTime,
    score: Int?,
    onClick: () -> Unit,
) {
    val badgeLabel = if (score != null) "Score $score" else "View Round"
    val badgeColor = if (score != null) LinksideColors.Success else LinksideColors.AccentLabel

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LinksideColors.Card)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.SportsGolf,
            contentDescription = null,
            tint = LinksideColors.AccentLabel,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                teeTime.courseName,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                teeTime.formattedDate(),
                color = LinksideColors.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Text(
            text = badgeLabel,
            fontWeight = FontWeight.SemiBold,
            color = badgeColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(badgeColor.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LinksideColors.TextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun WithdrawnTournamentRow(
    tournament: Tournament,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(LinksideColors.Card)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = LinksideColors.AccentLabel,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                tournament.name,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                "${tournament.courseName} · ${tournament.formattedDate()}",
                color = LinksideColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            "Withdrawn",
            color = LinksideColors.Danger,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                .background(LinksideColors.Danger.copy(alpha = 0.14f))
                .padding(horizontal = 8.dp, vertical = 5.dp),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LinksideColors.TextSecondary,
        )
    }
}

@Composable
private fun Modifier.clipProfileCard(): Modifier =
    this
        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
        .background(LinksideColors.Card)

private fun primaryAuthTitle(method: PrimaryAuthMethod): String = when (method) {
    PrimaryAuthMethod.APPLE -> "Apple"
    PrimaryAuthMethod.GOOGLE -> "Google"
    PrimaryAuthMethod.EMAIL -> "Email & Password"
    PrimaryAuthMethod.PHONE -> "Phone"
}

private fun primaryAuthIcon(method: PrimaryAuthMethod) = when (method) {
    PrimaryAuthMethod.APPLE -> Icons.Default.Email
    PrimaryAuthMethod.GOOGLE -> Icons.Default.Email
    PrimaryAuthMethod.EMAIL -> Icons.Default.Email
    PrimaryAuthMethod.PHONE -> Icons.Default.Phone
}
