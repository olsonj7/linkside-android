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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linkside.app.data.api.CoursePhotoUtils
import com.linkside.app.data.model.FriendGroup
import com.linkside.app.data.model.GolfTrip
import com.linkside.app.data.model.IdeaThread
import com.linkside.app.data.model.InviteStatus
import com.linkside.app.data.model.PlayFormat
import com.linkside.app.data.model.ScoringEngine
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.TeeTimeScorecard
import com.linkside.app.data.model.Tournament
import com.linkside.app.data.model.User
import com.linkside.app.ui.components.CoursePhotoThumbnail
import com.linkside.app.ui.components.HomeProfileHeader
import com.linkside.app.ui.components.LinksideWordmark
import com.linkside.app.ui.components.themeCardShape
import com.linkside.app.ui.components.CourseConditionsCard
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.components.SectionHeader
import com.linkside.app.ui.teetimes.TeeTimeCard
import com.linkside.app.ui.theme.LinksideColors
import com.linkside.app.ui.trips.TripCard
import com.linkside.app.data.model.isWithinCourseConditionsWindow
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    nextUpTeeTime: TeeTime?,
    currentRounds: List<TeeTime> = emptyList(),
    scorecardsByTeeTime: Map<String, List<TeeTimeScorecard>> = emptyMap(),
    teeTimes: List<TeeTime>,
    trips: List<GolfTrip>,
    groups: List<FriendGroup>,
    ideaThreads: List<IdeaThread>,
    openTournaments: List<Tournament> = emptyList(),
    unreadNotifications: Int,
    isLoading: Boolean,
    isTripsLoading: Boolean,
    onRefresh: () -> Unit,
    onCreateTeeTime: () -> Unit,
    onTeeTimeClick: (String) -> Unit,
    onTripClick: (String) -> Unit,
    onFriendGroups: () -> Unit,
    onEditGroup: (FriendGroup) -> Unit,
    onIdeaThreads: () -> Unit,
    onIdeaThreadClick: (String) -> Unit,
    onTournaments: () -> Unit = {},
    onTournamentClick: (String) -> Unit = {},
    onNotifications: () -> Unit,
    golferCount: Int = 0,
    onAddGolfers: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var dismissedAddGolfersPrompt by rememberSaveable { mutableStateOf(false) }
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
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                LinksideWordmark(
                    fontSize = 20,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                )
                IconButton(
                    onClick = onNotifications,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifications > 0) {
                                Badge { Text(unreadNotifications.coerceAtMost(99).toString()) }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = LinksideColors.TextPrimary,
                        )
                    }
                }
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

                nextUpTeeTime?.let { nextUp ->
                    item {
                        NextUpCard(teeTime = nextUp, onClick = { onTeeTimeClick(nextUp.id) })
                    }
                }

                item {
                    PrimaryButton(
                        title = "Create Tee Time",
                        onClick = onCreateTeeTime,
                        icon = Icons.Default.AddCircle,
                    )
                }

                if (golferCount == 0 && !dismissedAddGolfersPrompt) {
                    item {
                        AddGolfersPromptCard(
                            onAdd = onAddGolfers,
                            onDismiss = { dismissedAddGolfersPrompt = true },
                        )
                    }
                }

                if (openTournaments.isNotEmpty()) {
                    item {
                        tournamentsSection(
                            tournaments = openTournaments.take(3),
                            onSeeAll = onTournaments,
                            onTournamentClick = onTournamentClick,
                        )
                    }
                }

                item {
                    friendGroupsSection(
                        groups = groups,
                        onManage = onFriendGroups,
                        onGroupClick = onEditGroup,
                    )
                }

                if (currentRounds.isNotEmpty()) {
                    item {
                        currentRoundSection(
                            rounds = currentRounds,
                            scorecardsByTeeTime = scorecardsByTeeTime,
                            onTeeTimeClick = onTeeTimeClick,
                        )
                    }
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
                    ideaThreadsSection(
                        threads = ideaThreads,
                        onSeeAll = onIdeaThreads,
                        onThreadClick = onIdeaThreadClick,
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
                teeTimes.firstOrNull { it.isWithinCourseConditionsWindow() }?.let { nextWithWeather ->
                    CourseConditionsCard(
                        teeTime = nextWithWeather,
                        isSilver = user.isSilver,
                    )
                }
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
private fun AddGolfersPromptCard(
    onAdd: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Card)
            .clickable(onClick = onAdd)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(LinksideColors.RainBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = null,
                tint = LinksideColors.RainBlue,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Add your golf crew",
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Add golfers from your contacts so you can invite them in one tap.",
                color = LinksideColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = LinksideColors.TextSecondary,
            )
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

@Composable
private fun NextUpCard(teeTime: TeeTime, onClick: () -> Unit) {
    val photoUrl = CoursePhotoUtils.photoUrl(teeTime.courseId, teeTime.courseName)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LinksideColors.Accent.copy(alpha = 0.15f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("NEXT UP", style = MaterialTheme.typography.labelSmall, color = LinksideColors.AccentLabel)
                Text(teeTime.courseName, fontWeight = FontWeight.Bold, color = LinksideColors.TextPrimary)
                Text(teeTime.formattedDate(), color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            CoursePhotoThumbnail(url = photoUrl)
        }
    }
}

@Composable
private fun ideaThreadsSection(
    threads: List<IdeaThread>,
    onSeeAll: () -> Unit,
    onThreadClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            title = "IDEA THREADS",
            accentColor = LinksideColors.Gold,
            actionLabel = if (threads.isNotEmpty()) "See all" else null,
            onAction = if (threads.isNotEmpty()) onSeeAll else null,
        )
        if (threads.isEmpty()) {
            EmptyStateCard(
                title = "Brainstorm your next round",
                caption = "Start a thread with your crew before committing to a tee time.",
                actionLabel = "Start a Thread",
                onAction = onSeeAll,
            )
        } else {
            threads.forEach { thread ->
                Card(
                    onClick = { onThreadClick(thread.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = LinksideColors.Gold)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(thread.name, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                            Text(
                                "${thread.invitees.size} members",
                                style = MaterialTheme.typography.bodySmall,
                                color = LinksideColors.TextSecondary,
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = LinksideColors.TextTertiary)
                    }
                }
            }
        }
    }
}

@Composable
private fun tournamentsSection(
    tournaments: List<Tournament>,
    onSeeAll: () -> Unit,
    onTournamentClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            title = "TOURNAMENTS",
            accentColor = LinksideColors.Gold,
            actionLabel = "See all",
            onAction = onSeeAll,
        )
        tournaments.forEach { tournament ->
            Card(
                onClick = { onTournamentClick(tournament.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LinksideColors.Gold)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tournament.name, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                        Text(
                            "${tournament.courseName} · ${tournament.formattedDate()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LinksideColors.TextSecondary,
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = LinksideColors.TextTertiary)
                }
            }
        }
    }
}

@Composable
private fun currentRoundSection(
    rounds: List<TeeTime>,
    scorecardsByTeeTime: Map<String, List<TeeTimeScorecard>>,
    onTeeTimeClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "CURRENT ROUND", accentColor = Color(0xFF34C759))
        rounds.forEach { teeTime ->
            val rows = ScoringEngine.leaderboard(
                scorecards = scorecardsByTeeTime[teeTime.id].orEmpty(),
                playFormat = teeTime.playFormat,
                holes = teeTime.holesCount ?: 18,
                teamName = teeTime.teamName,
            )
            val formatLabel = PlayFormat.entries.firstOrNull { it.raw == teeTime.playFormat }?.displayName
            Card(
                onClick = { onTeeTimeClick(teeTime.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(teeTime.courseName, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(teeTime.formattedDate(), style = MaterialTheme.typography.bodySmall, color = LinksideColors.TextSecondary)
                        if (formatLabel != null) {
                            Text("·", color = LinksideColors.TextSecondary)
                            Text(formatLabel, style = MaterialTheme.typography.labelSmall, color = LinksideColors.AccentLabel, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (rows.isEmpty()) {
                        Text("Scores will appear as they’re entered", style = MaterialTheme.typography.bodySmall, color = LinksideColors.TextTertiary)
                    } else {
                        rows.take(4).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(row.name, color = LinksideColors.TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    if (row.total > 0) row.total.toString() else "—",
                                    fontWeight = FontWeight.SemiBold,
                                    color = LinksideColors.TextPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
