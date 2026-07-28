package com.linkside.app.ui.tournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.Tournament
import com.linkside.app.data.model.TournamentParticipant
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentsScreen(
    tournaments: List<Tournament>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = { LinksideTopAppBar(title = "Tournaments", onBack = onBack) },
    ) { padding ->
        if (tournaments.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LinksideColors.AccentLabel)
                Text(
                    "No open tournaments",
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextPrimary,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    "When a tournament opens for registration, it’ll show up here.",
                    color = LinksideColors.TextSecondary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(tournaments, key = { it.id }) { tournament ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LinksideColors.Card)
                            .clickable { onTournamentClick(tournament.id) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LinksideColors.AccentLabel)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(tournament.name, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                            Text(tournament.courseName, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text(tournament.formattedDate(), color = LinksideColors.TextTertiary, style = MaterialTheme.typography.labelSmall)
                            tournament.myParticipantStatus?.let {
                                Text(
                                    it.replaceFirstChar { c -> c.uppercase() },
                                    color = LinksideColors.AccentLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = LinksideColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournament: Tournament,
    participants: List<TournamentParticipant>,
    currentUserId: String,
    isLoading: Boolean,
    isRegistering: Boolean,
    isWithdrawing: Boolean,
    onBack: () -> Unit,
    onRegister: (teamName: String?) -> Unit,
    onWithdraw: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The detail endpoint doesn't include myParticipantStatus, so derive my
    // registration state from the participant list (fall back to the list value).
    val me = participants.firstOrNull { it.userId == currentUserId }
    val myStatus = me?.status ?: tournament.myParticipantStatus
    val isRegistered = myStatus in setOf("registered", "checked_in", "waitlist")
    val isWithdrawn = myStatus == "withdrawn"
    val canRegister = tournament.isOpen && !isRegistered

    // Existing teams (with room) other players have already formed.
    val existingTeams = participants
        .filter { it.status == "registered" || it.status == "checked_in" }
        .filter { !it.teamName.isNullOrBlank() }
        .groupBy { it.teamName!!.trim() }
        .filter { it.value.size < TOURNAMENT_PLAYERS_PER_TEAM }
        .keys
        .sorted()

    var showTeamPicker by remember { mutableStateOf(false) }
    var showWithdrawConfirm by remember { mutableStateOf(false) }

    val startRegister: () -> Unit = {
        if (existingTeams.isNotEmpty()) showTeamPicker = true else onRegister(null)
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = { LinksideTopAppBar(title = tournament.name, onBack = onBack) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(tournament.courseName, color = LinksideColors.TextSecondary)
                    Text(tournament.formattedDate(), fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                    tournament.formatLabel?.let { format ->
                        Text(
                            text = "Format: $format",
                            color = LinksideColors.TextSecondary,
                        )
                    }
                    tournament.handicapLabel?.let { handicap ->
                        Text(
                            text = handicap,
                            color = LinksideColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    tournament.entryFee?.let {
                        Text("Entry $${it.toInt()}", color = LinksideColors.AccentLabel)
                    }
                    tournament.notes?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (isRegistered) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LinksideColors.AccentLabel.copy(alpha = 0.15f))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LinksideColors.AccentLabel,
                        )
                        Text(
                            text = registeredLabel(myStatus),
                            fontWeight = FontWeight.SemiBold,
                            color = LinksideColors.AccentLabel,
                        )
                    }
                }
                if (me != null && me.hasAssignment) {
                    item { AssignmentCard(me) }
                }
                if (tournament.isOpen) {
                    item {
                        OutlinedButton(
                            onClick = { showWithdrawConfirm = true },
                            enabled = !isLoading && !isWithdrawing,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = LinksideColors.Danger,
                            ),
                        ) {
                            Text(if (isWithdrawing) "Withdrawing…" else "Withdraw")
                        }
                    }
                }
            } else if (isWithdrawn) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "You’ve withdrawn from this tournament.",
                            color = LinksideColors.TextSecondary,
                        )
                        if (tournament.isOpen) {
                            PrimaryButton(
                                title = if (isRegistering) "Registering…" else "Register again",
                                onClick = startRegister,
                                enabled = !isLoading && !isRegistering,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            } else if (canRegister) {
                item {
                    PrimaryButton(
                        title = if (isRegistering) "Registering…" else "Register",
                        onClick = startRegister,
                        enabled = !isLoading && !isRegistering,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    if (showTeamPicker) {
        TeamPickerDialog(
            teams = existingTeams,
            onDismiss = { showTeamPicker = false },
            onConfirm = { teamName ->
                showTeamPicker = false
                onRegister(teamName)
            },
        )
    }

    if (showWithdrawConfirm) {
        AlertDialog(
            onDismissRequest = { showWithdrawConfirm = false },
            title = { Text("Are you sure you want to withdraw?") },
            text = {
                Text(
                    if (tournament.isOpen) {
                        "You can re-register later while the tournament is still open."
                    } else {
                        "You’ll be removed from this tournament."
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWithdrawConfirm = false
                        onWithdraw()
                    },
                ) {
                    Text("Withdraw", color = LinksideColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssignmentCard(me: TournamentParticipant) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Card)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "YOUR ASSIGNMENT",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = LinksideColors.TextSecondary,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            me.teamName?.takeIf { it.isNotBlank() }?.let { AssignmentPill("Team", it) }
            me.groupName?.takeIf { it.isNotBlank() }?.let { AssignmentPill("Group", it) }
            me.startingHole?.let { AssignmentPill("Starting hole", "Hole $it") }
            me.cartNumber?.let { AssignmentPill("Cart", "Cart $it") }
            me.tee?.takeIf { it.isNotBlank() }?.let { AssignmentPill("Tee", it) }
        }
    }
}

@Composable
private fun AssignmentPill(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(LinksideColors.AccentLabel.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = LinksideColors.TextSecondary)
        Text(value, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
    }
}

@Composable
private fun TeamPickerDialog(
    teams: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (teamName: String?) -> Unit,
) {
    // null selection = register as an individual.
    var selected by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join a team?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Pick a team to join, or register on your own.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinksideColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                TeamOptionRow(
                    label = "Register as an individual",
                    selected = selected == null,
                    onClick = { selected = null },
                )
                teams.forEach { team ->
                    TeamOptionRow(
                        label = team,
                        selected = selected == team,
                        onClick = { selected = team },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Register") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun TeamOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, color = LinksideColors.TextPrimary)
    }
}

private const val TOURNAMENT_PLAYERS_PER_TEAM = 4

private fun registeredLabel(status: String?): String = when (status) {
    "waitlist" -> "You’re on the waitlist"
    "checked_in" -> "You’re checked in"
    else -> "You’re registered"
}
