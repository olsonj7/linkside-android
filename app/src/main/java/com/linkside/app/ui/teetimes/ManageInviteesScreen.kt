package com.linkside.app.ui.teetimes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.Friend
import com.linkside.app.data.model.FriendGroup
import com.linkside.app.data.model.Invite
import com.linkside.app.data.model.TeeTime
import com.linkside.app.ui.components.InviteDetailRow
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageInviteesScreen(
    teeTime: TeeTime,
    groups: List<FriendGroup>,
    savedGolfers: List<Friend>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRemove: (Invite) -> Unit,
    onAdd: (List<Friend>) -> Unit,
    modifier: Modifier = Modifier,
    /** When set, Add Golfers is limited to this trip roster (no contacts/groups). */
    tripRoster: List<Friend>? = null,
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var inviteToRemove by remember { mutableStateOf<Invite?>(null) }
    var selectedPhones by remember { mutableStateOf(emptySet<String>()) }

    val removable = teeTime.invites.filter { it.isHost != true }
    val tripRosterOnly = tripRoster != null
    val pickerGolfers = tripRoster ?: savedGolfers
    val pickerGroups = if (tripRosterOnly) emptyList() else groups

    if (showAddSheet) {
        InviteGolfersSheet(
            groups = pickerGroups,
            savedGolfers = pickerGolfers,
            selectedPhones = selectedPhones,
            onSelectionChange = { selectedPhones = it },
            tripRosterOnly = tripRosterOnly,
            onDismiss = {
                val existing = teeTime.invites.mapNotNull { it.phone }.toSet()
                val toAdd = pickerGolfers
                    .distinctBy { it.phone }
                    .filter { it.phone in selectedPhones && it.phone !in existing }
                if (toAdd.isNotEmpty()) onAdd(toAdd)
                selectedPhones = emptySet()
                showAddSheet = false
            },
        )
    }

    inviteToRemove?.let { invite ->
        AlertDialog(
            onDismissRequest = { inviteToRemove = null },
            title = { Text("Remove ${invite.name}?") },
            text = { Text("They’ll be removed from this tee time.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove(invite)
                        inviteToRemove = null
                    },
                ) { Text("Remove", color = LinksideColors.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { inviteToRemove = null }) {
                    Text("Cancel", color = LinksideColors.AccentLabel)
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(
                title = "Manage Invitees",
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
        ) {
            PrimaryButton(
                title = "Add Golfers",
                icon = Icons.Default.PersonAdd,
                onClick = {
                    selectedPhones = teeTime.invites.mapNotNull { it.phone }.toSet()
                    showAddSheet = true
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(removable, key = { it.phone ?: it.userId ?: it.name }) { invite ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LinksideColors.Card),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        InviteDetailRow(
                            name = invite.name,
                            status = invite.inviteStatus,
                            isHost = false,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { inviteToRemove = invite },
                            enabled = !isLoading,
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = LinksideColors.Danger)
                        }
                    }
                }
                if (removable.isEmpty()) {
                    item {
                        Text(
                            if (tripRosterOnly) {
                                "No invitees yet. Add golfers from the trip roster."
                            } else {
                                "No invitees yet. Add golfers from your list."
                            },
                            color = LinksideColors.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                }
            }
        }
    }
}
