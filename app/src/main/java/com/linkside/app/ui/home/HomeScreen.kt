package com.linkside.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.FriendGroup
import com.linkside.app.data.model.GolfTrip
import com.linkside.app.data.model.InviteStatus
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.User
import com.linkside.app.ui.components.HomeProfileHeader
import com.linkside.app.ui.components.LinksideWordmark
import com.linkside.app.ui.components.themeCardShape
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.components.SectionHeader
import com.linkside.app.ui.teetimes.TeeTimeCard
import com.linkside.app.ui.theme.LinksideColors
import com.linkside.app.ui.trips.TripCard
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    teeTimes: List<TeeTime>,
    trips: List<GolfTrip>,
    groups: List<FriendGroup>,
    isLoading: Boolean,
    isTripsLoading: Boolean,
    onRefresh: () -> Unit,
    onCreateTeeTime: () -> Unit,
    onTeeTimeClick: (String) -> Unit,
    onTripClick: (String) -> Unit,
    onFriendGroups: () -> Unit,
    onEditGroup: (FriendGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    val upcomingTeeTimes = teeTimes
        .filter { tt ->
            val instant = tt.parsedInstant()
            (instant == null || instant.isAfter(Instant.now())) &&
                !tt.isDeclinedBy(user)
        }
        .sortedBy { it.parsedInstant() ?: Instant.EPOCH }

    val roundsThisYear = roundsThisYear(teeTimes, user)

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LinksideColors.Primary)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                LinksideWordmark(fontSize = 20)
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = LinksideColors.TextPrimary,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading || isTripsLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item {
                    HomeProfileHeader(user = user, roundsThisYear = roundsThisYear)
                }

                item {
                    PrimaryButton(
                        title = "Create Tee Time",
                        onClick = onCreateTeeTime,
                        icon = Icons.Default.AddCircle,
                    )
                }

                item {
                    friendGroupsSection(
                        groups = groups,
                        onManage = onFriendGroups,
                        onGroupClick = onEditGroup,
                    )
                }

                item {
                    upcomingTeeTimesSection(
                        teeTimes = upcomingTeeTimes,
                        user = user,
                        isLoading = isLoading,
                        onCreateTeeTime = onCreateTeeTime,
                        onTeeTimeClick = onTeeTimeClick,
                    )
                }

                item {
                    golfTripsSection(
                        trips = trips,
                        isLoading = isTripsLoading,
                        onTripClick = onTripClick,
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun friendGroupsSection(
    groups: List<FriendGroup>,
    onManage: () -> Unit,
    onGroupClick: (FriendGroup) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            title = "FRIEND GROUPS",
            accentColor = LinksideColors.Gold,
            actionLabel = "Manage",
            onAction = onManage,
        )
        if (groups.isEmpty()) {
            Card(
                onClick = onManage,
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardShape(14.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Default.People, contentDescription = null, tint = LinksideColors.AccentLabel)
                    Text(
                        text = "Create your first friend group",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LinksideColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LinksideColors.TextTertiary,
                    )
                }
            }
        } else {
            groups.forEach { group ->
                FriendGroupRowCard(group = group, onClick = { onGroupClick(group) })
            }
        }
    }
}

@Composable
private fun FriendGroupRowCard(
    group: FriendGroup,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .themeCardShape(14.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LinksideColors.Accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    tint = LinksideColors.OnGold,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, fontWeight = FontWeight.Medium, color = LinksideColors.TextPrimary)
                val memberLabel = if (group.members.size == 1) "1 member" else "${group.members.size} members"
                Text(memberLabel, style = MaterialTheme.typography.bodySmall, color = LinksideColors.TextSecondary)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LinksideColors.TextTertiary,
            )
        }
    }
}

@Composable
private fun upcomingTeeTimesSection(
    teeTimes: List<TeeTime>,
    user: User,
    isLoading: Boolean,
    onCreateTeeTime: () -> Unit,
    onTeeTimeClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "UPCOMING TEE TIMES", accentColor = LinksideColors.Accent)
        when {
            isLoading && teeTimes.isEmpty() -> {
                CircularProgressIndicator(color = LinksideColors.Accent, modifier = Modifier.padding(8.dp))
            }
            teeTimes.isEmpty() -> {
                EmptyStateCard(
                    title = "No upcoming tee times",
                    caption = "Create one to schedule your next round.",
                    actionLabel = "Create Tee Time",
                    onAction = onCreateTeeTime,
                )
            }
            else -> {
                teeTimes.forEach { teeTime ->
                    TeeTimeCard(
                        teeTime = teeTime,
                        isHosting = teeTime.creatorId == user.id,
                        onClick = { onTeeTimeClick(teeTime.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun golfTripsSection(
    trips: List<GolfTrip>,
    isLoading: Boolean,
    onTripClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "GOLF TRIPS", accentColor = LinksideColors.RainBlue)
        when {
            isLoading && trips.isEmpty() -> {
                CircularProgressIndicator(color = LinksideColors.Accent, modifier = Modifier.padding(8.dp))
            }
            trips.isEmpty() -> {
                EmptyStateCard(
                    title = "No trips planned",
                    caption = "When someone invites you to a golf trip, it will show up here.",
                )
            }
            else -> {
                trips.forEach { trip ->
                    TripCard(trip = trip, onClick = { onTripClick(trip.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    caption: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .themeCardShape(14.dp)
            .background(LinksideColors.Card)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
        Text(caption, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                color = LinksideColors.OnGold,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(LinksideColors.Accent)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}

private fun roundsThisYear(teeTimes: List<TeeTime>, user: User?): Int {
    val year = java.time.Year.now().value
    val now = Instant.now()
    return teeTimes.count { tt ->
        val instant = tt.parsedInstant() ?: return@count false
        instant.isBefore(now) &&
            instant.atZone(ZoneId.systemDefault()).year == year &&
            tt.myInvite(user)?.inviteStatus == InviteStatus.YES
    }
}
