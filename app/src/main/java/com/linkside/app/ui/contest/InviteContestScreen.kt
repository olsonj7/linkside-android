package com.linkside.app.ui.contest

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linkside.app.data.model.ContestEntry
import com.linkside.app.data.model.ContestLeaderboard
import com.linkside.app.data.model.ContestWin
import com.linkside.app.data.model.User
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.share.ShareLinks
import com.linkside.app.ui.theme.LinksideColors
import com.linkside.app.viewmodel.ContestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteContestScreen(
    user: User,
    viewModel: ContestViewModel,
    onBack: () -> Unit,
    onClaimPrize: (ContestWin) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(
                title = "Invite Contest",
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = LinksideColors.Gold,
                    modifier = Modifier.size(36.dp),
                )
                Text(
                    "Invite friends, win a golf prize",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LinksideColors.TextPrimary,
                )
                Text(
                    "Share Linkside with your golf buddies. Friends who join this month count toward the monthly Invite Contest.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinksideColors.TextSecondary,
                )
            }

            state.summary?.let { summary ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(value = summary.invitedCount, label = "Invited", modifier = Modifier.weight(1f))
                    StatCard(value = summary.joinedCount, label = "Joined", modifier = Modifier.weight(1f))
                }
            }

            when {
                state.isLoading && state.contest == null -> {
                    CircularProgressIndicator(
                        color = LinksideColors.Accent,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 24.dp),
                    )
                }
                state.errorMessage != null && state.contest == null -> {
                    Text(
                        state.errorMessage.orEmpty(),
                        color = LinksideColors.Danger,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                state.contest != null -> {
                    ContestSection(
                        contest = state.contest!!,
                        win = state.win,
                        onClaim = { state.win?.let(onClaimPrize) },
                    )
                }
            }

            PrimaryButton(
                title = "Share your invite",
                icon = Icons.Default.Share,
                onClick = {
                    val name = user.firstName?.trim().orEmpty()
                    val intro = if (name.isNotEmpty()) "$name invited you to Linkside" else "Join me on Linkside"
                    val text =
                        "⛳ $intro — fill tee times fast, invite friends, and track your rounds. Download free: ${ShareLinks.INSTALL_URL}"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Linkside"))
                },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Card)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "$value",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = LinksideColors.TextPrimary,
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = LinksideColors.TextSecondary)
    }
}

@Composable
private fun ContestSection(
    contest: ContestLeaderboard,
    win: ContestWin?,
    onClaim: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "THIS MONTH'S INVITE CONTEST",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${contest.daysLeft} day${if (contest.daysLeft == 1) "" else "s"} left",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextSecondary,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(LinksideColors.Card)
                .border(1.dp, LinksideColors.Gold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LinksideColors.Gold)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        contest.prize,
                        fontWeight = FontWeight.SemiBold,
                        color = LinksideColors.TextPrimary,
                    )
                    Text(
                        "Bring ${contest.minJoinsToWin}+ friends who join this month to win",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinksideColors.TextSecondary,
                    )
                }
            }

            if (contest.leaderboard.isEmpty()) {
                Text(
                    "No one has invited a friend who joined yet this month — be the first!",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinksideColors.TextSecondary,
                )
            } else {
                contest.leaderboard.forEach { entry ->
                    LeaderboardRow(entry)
                }
                if (contest.myRank == null && contest.myCount == 0) {
                    Text(
                        "Invite a friend to get on the board!",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinksideColors.TextSecondary,
                    )
                } else if (contest.myRank == null && contest.myCount > 0) {
                    Text(
                        "You have ${contest.myCount} join${if (contest.myCount == 1) "" else "s"} this month",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinksideColors.TextSecondary,
                    )
                }
            }

            if (win != null && !win.claimed) {
                PrimaryButton(
                    title = "Claim Your Prize",
                    icon = Icons.Default.EmojiEvents,
                    onClick = onClaim,
                )
            } else if (win != null && win.claimed) {
                Text(
                    if (win.fulfilled) "Prize shipped!" else "Claim submitted — we'll be in touch to ship your prize.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinksideColors.Success,
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: ContestEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (entry.isMe) {
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LinksideColors.Accent.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                } else {
                    Modifier.padding(vertical = 4.dp)
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "#${entry.rank}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (entry.rank == 1) LinksideColors.Gold else LinksideColors.TextSecondary,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            if (entry.isMe) "You" else entry.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (entry.isMe) FontWeight.Bold else FontWeight.Normal,
            color = LinksideColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            "${entry.count}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = LinksideColors.TextSecondary,
        )
    }
}
