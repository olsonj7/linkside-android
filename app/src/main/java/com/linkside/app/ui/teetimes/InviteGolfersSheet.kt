package com.linkside.app.ui.teetimes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.linkside.app.ui.components.LinkButton
import com.linkside.app.ui.components.ProfileAvatarView
import com.linkside.app.ui.golfers.ContactPickerSheet
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteGolfersSheet(
    groups: List<FriendGroup>,
    savedGolfers: List<Friend>,
    selectedPhones: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showContactPicker by remember { mutableStateOf(false) }

    val allGolferPhones = remember(savedGolfers) { savedGolfers.map { it.phone }.toSet() }
    val allSelected = allGolferPhones.isNotEmpty() && allGolferPhones.all { it in selectedPhones }

    val selectedInvitees = remember(groups, savedGolfers, selectedPhones) {
        val seen = mutableSetOf<String>()
        val combined = mutableListOf<Friend>()
        (groups.flatMap { it.members } + savedGolfers).forEach { friend ->
            if (seen.add(friend.phone)) combined.add(friend)
        }
        combined
            .filter { it.phone in selectedPhones }
            .sortedBy { it.fullName.lowercase() }
    }

    if (showContactPicker) {
        ContactPickerSheet(
            contacts = savedGolfers,
            selectedPhones = selectedPhones,
            onToggle = { phone ->
                onSelectionChange(
                    if (phone in selectedPhones) selectedPhones - phone else selectedPhones + phone,
                )
            },
            onDone = { showContactPicker = false },
            onDismiss = { showContactPicker = false },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LinksideColors.Primary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (allGolferPhones.isNotEmpty()) {
                    LinkButton(
                        title = if (allSelected) "Deselect All" else "Select All",
                        onClick = {
                            onSelectionChange(
                                if (allSelected) {
                                    selectedPhones - allGolferPhones
                                } else {
                                    selectedPhones + allGolferPhones
                                },
                            )
                        },
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    text = "Invite Golfers",
                    modifier = Modifier.weight(2f),
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                LinkButton(
                    title = "Done (${selectedPhones.size})",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (groups.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Groups",
                            fontWeight = FontWeight.SemiBold,
                            color = LinksideColors.TextPrimary,
                        )
                        groups.forEach { group ->
                            GroupInviteRow(
                                group = group,
                                selectedPhones = selectedPhones,
                                onToggle = { phones, allSelectedInGroup ->
                                    onSelectionChange(
                                        if (allSelectedInGroup) {
                                            selectedPhones - phones
                                        } else {
                                            selectedPhones + phones
                                        },
                                    )
                                },
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Invited Golfers",
                            fontWeight = FontWeight.SemiBold,
                            color = LinksideColors.TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        LinkButton(title = "Pick from Golfers", onClick = { showContactPicker = true })
                    }

                    if (selectedInvitees.isEmpty()) {
                        Text(
                            text = "Tap \"Pick from Golfers\" or select a group to add invited golfers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LinksideColors.TextSecondary,
                        )
                    } else {
                        selectedInvitees.forEach { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ProfileAvatarView(name = friend.fullName, size = 36.dp)
                                Text(
                                    text = friend.fullName,
                                    modifier = Modifier.weight(1f),
                                    color = LinksideColors.TextPrimary,
                                )
                                IconButton(onClick = { onSelectionChange(selectedPhones - friend.phone) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = LinksideColors.TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupInviteRow(
    group: FriendGroup,
    selectedPhones: Set<String>,
    onToggle: (Set<String>, Boolean) -> Unit,
) {
    val groupPhones = remember(group) { group.members.map { it.phone }.toSet() }
    val allSelected = groupPhones.isNotEmpty() && groupPhones.all { it in selectedPhones }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Card)
            .clickable { onToggle(groupPhones, allSelected) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (allSelected) LinksideColors.AccentLabel else LinksideColors.Muted),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Groups,
                contentDescription = null,
                tint = if (allSelected) LinksideColors.OnGold else LinksideColors.TextPrimary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(group.name, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
            Text(
                text = "${group.members.size} member${if (group.members.size == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = LinksideColors.TextSecondary,
            )
        }
        Icon(
            imageVector = if (allSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (allSelected) LinksideColors.AccentLabel else LinksideColors.TextSecondary,
        )
    }
}
