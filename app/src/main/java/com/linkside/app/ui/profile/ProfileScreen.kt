package com.linkside.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.GolfCourse
import com.linkside.app.data.model.User
import com.linkside.app.data.prefs.ProfilePreferences
import com.linkside.app.ui.components.LinksideWordmark
import com.linkside.app.ui.components.ProfileAvatarView
import com.linkside.app.ui.components.SectionHeader
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    profilePreferences: ProfilePreferences,
    courseSearchResults: List<GolfCourse>,
    isSearchingCourses: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onEditProfile: () -> Unit,
    onSearchCourses: (String) -> Unit,
    onAddFavoriteCourse: (GolfCourse) -> Unit,
    onRemoveFavoriteCourse: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showAddFavorite by remember { mutableStateOf(false) }
    var defaultGroupSize by remember { mutableStateOf(profilePreferences.defaultGroupSize) }
    var smsEnabled by remember { mutableStateOf(profilePreferences.smsNotificationsEnabled) }
    var pushEnabled by remember { mutableStateOf(profilePreferences.pushNotificationsEnabled) }
    var darkModeEnabled by remember { mutableStateOf(profilePreferences.prefersDarkMode ?: true) }
    val favorites = user.favoriteCourses.orEmpty()
    val homeAddress = formatHomeAddress(user.address, user.city, user.state, user.zipCode)
    val primary = primaryAuthMethod(user)

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LinksideColors.Primary)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LinksideWordmark(fontSize = 20)
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
                    ProfileAvatarView(name = user.displayName, remoteUrl = user.avatarUrl, size = 64.dp)
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
                    if (primary != PrimaryAuthMethod.EMAIL && !user.email.isNullOrBlank()) {
                        SignInMethodCard(
                            title = "Email & Password linked",
                            subtitle = user.email,
                            icon = Icons.Default.Email,
                            showCheck = false,
                        )
                    }
                    if (primary != PrimaryAuthMethod.GOOGLE && !user.googleId.isNullOrBlank()) {
                        SignInMethodCard(
                            title = "Google Account linked",
                            subtitle = null,
                            icon = Icons.Default.Email,
                            showCheck = false,
                        )
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

            item { Spacer(modifier = Modifier.size(24.dp)) }
        }
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
